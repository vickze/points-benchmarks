
package com.points.handler;

import io.vertx.core.Future;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.pgclient.PgConnection;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PropertyKind;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.templates.SqlTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class Points1Handler {

  private final Logger LOG = LoggerFactory.getLogger(this.getClass());

  private SqlConnection connection;

  public Points1Handler(SqlConnection connection) {
    this.connection = connection;
  }


  public void createAccount(RoutingContext ctx) {
    JsonObject body = ctx.body().asJsonObject();

    String insertSql = "INSERT INTO account(name, balance, create_time, update_time) VALUES (#{name}, #{balance}, #{createTime}, #{updateTime})";
    if (connection instanceof PgConnection) {
      insertSql = insertSql + " RETURNING id";
    }
    body.put("createTime", LocalDateTime.now());
    body.put("updateTime", LocalDateTime.now());

    SqlTemplate
      .forQuery(connection, insertSql)
      .execute(body.getMap())
      .onSuccess(v -> {
        if (connection instanceof PgConnection) {
          body.put("id", v.iterator().next().getLong("id"));
        } else {
          body.put("id", v.property(PropertyKind.create("last-inserted-id", Long.class)));
        }

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
      .forUpdate(connection, "UPDATE account SET name = #{name}, update_time = #{updateTime} WHERE id = #{id}")
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
      .forQuery(connection, "SELECT * FROM account WHERE id = #{id}")
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



  public void getAccountAndTransaction(RoutingContext ctx) {
    Long id = Long.valueOf(ctx.request().getParam("id"));

    LOG.info("getAccountAndTransaction id: {}", id);

    JsonObject response = new JsonObject();
    SqlTemplate
      .forQuery(connection, "SELECT * FROM account WHERE id = #{id}")
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
          .forQuery(connection, "SELECT * FROM points_transaction WHERE account_id = #{id}")
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
