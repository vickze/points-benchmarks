package com.points;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.impl.cpu.CpuCoreSensor;
import io.vertx.core.json.jackson.DatabindCodec;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.SqlConnectOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class VertxApplication {
  private static final Logger LOG = LoggerFactory.getLogger(VertxApplication.class);

  public static void main(String[] args) {
    DatabindCodec.mapper().registerModule(new JavaTimeModule());
    DatabindCodec.mapper().configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

    DatabindCodec.prettyMapper().registerModule(new JavaTimeModule());
    DatabindCodec.prettyMapper().configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

    Vertx vertx = Vertx.vertx();

    String dbType = Optional.ofNullable(System.getProperty("db.type")).orElse("mysql");
    String dbPort = Optional.ofNullable(System.getProperty("db.port")).orElse("3306");
    String dbHost = Optional.ofNullable(System.getProperty("db.host")).orElse("localhost");
    String dbDatabase = Optional.ofNullable(System.getProperty("db.database")).orElse("points");
    String dbUser = Optional.ofNullable(System.getProperty("db.user")).orElse("root");
    String dbPassword = Optional.ofNullable(System.getProperty("db.password")).orElse("root");
    String dbMaxSize = Optional.ofNullable(System.getProperty("db.max.size")).orElse(String.valueOf(2 * CpuCoreSensor.availableProcessors()));

    PoolOptions poolOptions = new PoolOptions()
      .setMaxSize(Integer.parseInt(dbMaxSize));

    PgConnectOptions pgConnectOptions;
    Pool pool;
    switch (dbType) {
      case "postgresql":
         pgConnectOptions = new PgConnectOptions()
           .setCachePreparedStatements(true)
          .setPort(Integer.parseInt(dbPort))
          .setHost(dbHost)
          .setDatabase(dbDatabase)
          .setUser(dbUser)
          .setPassword(dbPassword);


        // Connection pool
        pool = PgPool.pool(vertx, pgConnectOptions, poolOptions);
        break;
      case "mysql":
        MySQLConnectOptions mySQLConnectOptions = new MySQLConnectOptions()
          .setCachePreparedStatements(true)
          .setPort(Integer.parseInt(dbPort))
          .setHost(dbHost)
          .setDatabase(dbDatabase)
          .setUser(dbUser)
          .setPassword(dbPassword);

        pgConnectOptions = null;
        // Connection pool
        pool = MySQLPool.pool(vertx, mySQLConnectOptions, poolOptions);
        break;
      default:
        throw new IllegalArgumentException();
    }


    vertx.deployVerticle(()-> new WebVerticle(pool), new DeploymentOptions().setInstances(2 * CpuCoreSensor.availableProcessors()))
      .onSuccess(s -> LOG.info("Web started on port(s): 8080 (http) with context path ''"))
      .onFailure(e -> {
        LOG.error(e.getMessage(), e);
      });
  }
}
