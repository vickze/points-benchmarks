package com.points;

import com.points.handler.Points1Handler;
import com.points.handler.PointsHandler;
import com.points.model.Message;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgConnection;
import io.vertx.sqlclient.Pool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Web1Verticle extends AbstractVerticle {

  private final Logger LOG = LoggerFactory.getLogger(this.getClass());

  private final PgConnectOptions pgConnectOptions;

  public Web1Verticle(PgConnectOptions pgConnectOptions) {
    this.pgConnectOptions = pgConnectOptions;
  }


  @Override
  public void start(Promise<Void> promise) {
    LOG.info("wev");
    Router router = Router.router(vertx);

    router.route().handler(BodyHandler.create());

    PgConnection.connect(vertx, pgConnectOptions).onComplete(pgConnection -> {
      Points1Handler pointsHandler = new Points1Handler(pgConnection.result());

      router.get("/plaintext").handler(ctx -> {
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
      router.get("/getAccountAndTransaction/:id").handler(pointsHandler::getAccountAndTransaction);

      vertx.createHttpServer().requestHandler(router).listen(8080).mapEmpty();
    });
  }

}

