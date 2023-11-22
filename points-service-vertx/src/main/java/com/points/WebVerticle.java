package com.points;

import com.points.model.Message;
import com.points.handler.PointsHandler;
import io.vertx.core.*;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.impl.cpu.CpuCoreSensor;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.LoggerHandler;
import io.vertx.ext.web.handler.ResponseTimeHandler;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgConnection;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;


public class WebVerticle extends AbstractVerticle {

  private final Logger LOG = LoggerFactory.getLogger(this.getClass());

  private final Pool pool;

  public WebVerticle(Pool pool) {
    this.pool = pool;
  }

  private Future<Void> startHttpServer() {
    Router router = Router.router(vertx);

    router.route().handler(BodyHandler.create());

    PointsHandler pointsHandler = new PointsHandler(pool);

    router.get("/plainText").handler(ctx -> {
      ctx.response()
        .putHeader(HttpHeaders.CONTENT_TYPE, "text/plain")
        .end("Hello, World!");
    });

    router.get("/json").handler(ctx -> {
      ctx.response()
        .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
        .end(Json.encodeToBuffer(new Message("Hello, World!")));
    });


    router.post("/createAccount").handler(pointsHandler::createAccount);
    router.post("/updateAccount").handler(pointsHandler::updateAccount);
    router.get("/getAccount/:id").handler(pointsHandler::getAccount);
    router.post("/transaction").handler(pointsHandler::transaction);
    router.get("/getAccountAndTransaction/:id").handler(pointsHandler::getAccountAndTransaction);

    return vertx.createHttpServer().requestHandler(router).listen(8080).mapEmpty();
  }

  @Override
  public void start(Promise<Void> promise) {
    try {
      startHttpServer().onComplete(promise);
    } catch (Exception e) {
      LOG.error(e.getMessage(), e);
    }
  }

}

