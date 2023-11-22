# docker build -f vertx-web-postgresql.dockerfile -t points/vertx-web-postgresql .
# docker run -it --name=points-service -p 18080:8080 --rm --net points points/vertx-web-postgresql

FROM openjdk:8-jdk-alpine

COPY target/points-service-vertx-1.0.0-SNAPSHOT-fat.jar points-service-vertx-1.0.0-SNAPSHOT-fat.jar

ENV TZ="Asia/Shanghai"

EXPOSE 8080

CMD java \
      -server \
      -Xms2G \
      -Xmx2G \
      -Ddb.type=postgresql -Ddb.port=5432 -Ddb.host=points-postgresql -Ddb.database=points -Ddb.user=postgres -Ddb.password=postgres  -Ddb.max.size=16 \
      -jar                                              \
      points-service-vertx-1.0.0-SNAPSHOT-fat.jar       \
