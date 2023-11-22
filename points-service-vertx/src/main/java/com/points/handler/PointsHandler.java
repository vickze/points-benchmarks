
package com.points.handler;

import io.vertx.core.Future;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PropertyKind;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.templates.SqlTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class PointsHandler {

  private final Logger LOG = LoggerFactory.getLogger(this.getClass());

  private Pool pool;

  public PointsHandler(Pool pool) {
    this.pool = pool;
  }


  public void createAccount(RoutingContext ctx) {
    JsonObject body = ctx.body().asJsonObject();
    LOG.info("createAccount request: {}", body);

    String insertSql = "INSERT INTO account(name, balance, create_time, update_time) VALUES (#{name}, #{balance}, #{createTime}, #{updateTime})";
    if (pool instanceof PgPool) {
      insertSql = insertSql + " RETURNING id";
    }
    body.put("createTime", LocalDateTime.now());
    body.put("updateTime", LocalDateTime.now());

    SqlTemplate
      .forQuery(pool, insertSql)
      .execute(body.getMap())
      .onSuccess(v -> {
        if (pool instanceof PgPool) {
          body.put("id", v.iterator().next().getLong("id"));
        } else {
          body.put("id", v.property(PropertyKind.create("last-inserted-id", Long.class)));
        }

        LOG.info("createAccount response: {}", body);

        ctx.response()
          .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
          .end(Json.encodeToBuffer(body));
      })
      .onFailure(err -> {
        LOG.error(err.getMessage(), err);
        ctx.response()
          .setStatusCode(500)
          .end(err.getMessage());
      });
  }


  public void updateAccount(RoutingContext ctx) {
    JsonObject body = ctx.body().asJsonObject();

    LOG.info("updateAccount request: {}", body);

    Map<String, Object> map = new LinkedHashMap<>(body.getMap());
    map.put("updateTime", LocalDateTime.now());
    SqlTemplate
      .forUpdate(pool, "UPDATE account SET name = #{name}, update_time = #{updateTime} WHERE id = #{id}")
      .execute(map)
      .onSuccess(v -> {
        if (v.rowCount() == 0) {
          ctx.response()
            .setStatusCode(500)
            .end("update failed");
        }
        LOG.info("updateAccount response: {}", body);

        ctx.response()
          .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
          .end(Json.encodeToBuffer(body));
      })
      .onFailure(err -> {
        LOG.error(err.getMessage(), err);
        ctx.response()
          .setStatusCode(500)
          .end(err.getMessage());
      });
  }


  public void getAccount(RoutingContext ctx) {
    Long id = Long.valueOf(ctx.request().getParam("id"));

    SqlTemplate
      .forQuery(pool, "SELECT * FROM account WHERE id = #{id}")
      .mapTo(Row::toJson)
      .execute(Collections.singletonMap("id", id))
      .onSuccess(v -> {
        JsonObject response = v.iterator().next();

        ctx.response()
          .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
          .end(Json.encodeToBuffer(response));
      })
      .onFailure(err -> {
        LOG.error(err.getMessage(), err);
        ctx.response()
          .setStatusCode(500)
          .end(err.getMessage());
      });
  }


  public void transaction(RoutingContext ctx) {
    JsonObject body = ctx.body().asJsonObject();
    LOG.info("transaction request: {}", body);

    JsonObject response = new JsonObject();
    String sql;
    if (body.getValue("type").equals(1)) {
      sql = "UPDATE account SET balance = balance + #{amount} WHERE id = #{id}";
    } else if (body.getValue("type").equals(2)) {
      sql = "UPDATE account SET balance = balance - #{amount} WHERE id = #{id} AND balance >= #{amount}";
    } else {
      response.put("status", "f");
      response.put("msg", "invalid type");
      ctx.response()
        .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
        .end(Json.encodeToBuffer(response));
      return;
    }

    Map<String, Object> map = body.getMap();
    pool.withTransaction(client -> SqlTemplate
        .forUpdate(client, sql)
        .execute(map)
        .compose(res1 -> {
          if (res1.rowCount() == 0) {
            response.put("status", "f");
            response.put("msg", "insufficient balance");
            return Future.succeededFuture();
          }
          return SqlTemplate.forQuery(client, "SELECT balance FROM account WHERE id = #{id}")
            .execute(map)
            .compose(res2 -> {
              long afterBalance = res2.iterator().next().getLong("balance");
              long previousBalance;

              if (body.getValue("type").equals(1)) {
                previousBalance = afterBalance - body.getLong("amount");
              } else {
                previousBalance = afterBalance + body.getLong("amount");
              }

              response.put("id", body.getValue("id"));
              response.put("type", body.getValue("type"));
              response.put("amount", body.getValue("amount"));
              response.put("previousBalance", previousBalance);
              response.put("afterBalance", afterBalance);

              String insertSql = "INSERT INTO points_transaction(account_id, type, amount, previous_balance, after_balance, create_time) VALUES(#{id}, #{type}, #{amount}, #{previousBalance}, #{afterBalance}, #{createTime})";
              if (pool instanceof PgPool) {
                insertSql = insertSql + " RETURNING id";
              }

              Map<String, Object> insertMap = new HashMap<>(response.getMap());
              insertMap.put("createTime", LocalDateTime.now());
              return SqlTemplate.forQuery(client, insertSql)
                .execute(insertMap)
                .compose(res3 -> {
                  if (res3.rowCount() == 0) {
                    response.put("status", "f");
                  } else {
                    if (pool instanceof PgPool) {
                      response.put("transactionId", res3.iterator().next().getLong("id"));
                    } else {
                      response.put("transactionId", res3.property(PropertyKind.create("last-inserted-id", Long.class)));
                    }
                    response.put("status", "s");
                  }
                  return Future.succeededFuture();
                });
            });
        }))
      .onSuccess(v -> {
        LOG.info("transaction response: {}", response);
        ctx.response()
          .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
          .end(Json.encodeToBuffer(response));
      })
      .onFailure(err -> {
        LOG.error(err.getMessage(), err);
        ctx.response()
          .setStatusCode(500)
          .end(err.getMessage());
      });
  }


  public void getAccountAndTransaction(RoutingContext ctx) {
    Long id = Long.valueOf(ctx.request().getParam("id"));

    LOG.info("getAccountAndTransaction id: {}", id);

    JsonObject response = new JsonObject();
    SqlTemplate
      .forQuery(pool, "SELECT * FROM account WHERE id = #{id}")
      .mapTo(Row::toJson)
      .execute(Collections.singletonMap("id", id))
      .compose(v -> {
        if (v.size() == 0) {
          return Future.succeededFuture();
        }
        JsonObject result = v.iterator().next();
        response.put("id", result.getValue("id"));
        response.put("name", result.getValue("name"));
        response.put("balance", result.getValue("balance"));

        return SqlTemplate
          .forQuery(pool, "SELECT * FROM points_transaction WHERE account_id = #{id}")
          .mapTo(Row::toJson)
          .execute(Collections.singletonMap("id", id));
      })
      .onSuccess(vv -> {
        if (vv != null) {
          JsonArray array = new JsonArray();
          vv.forEach(array::add);
          response.put("transactions", array);
        }

        LOG.info("getAccountAndTransaction response: {}", response);
        ctx.response()
          .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
          .end(Json.encodeToBuffer(response));
      })
      .onFailure(err -> {
        LOG.error(err.getMessage(), err);
        ctx.response()
          .setStatusCode(500)
          .end(err.getMessage());
      });
  }

}
