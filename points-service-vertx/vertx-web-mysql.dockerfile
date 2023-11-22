# docker build -f vertx-web-mysql.dockerfile -t points/vertx-web-mysql .
# docker run -it --name=points-service -p 18080:8080 --rm --net points points/vertx-web-mysql

FROM openjdk:8-jdk-alpine

COPY target/points-service-vertx-1.0.0-SNAPSHOT-fat.jar points-service-vertx-1.0.0-SNAPSHOT-fat.jar

ENV TZ="Asia/Shanghai"

EXPOSE 8080

CMD java \
      -server \
      -Xms2G \
      -Xmx2G \
      -Ddb.type=mysql -Ddb.port=3306 -Ddb.host=points-mysql -Ddb.name=points -Ddb.user=root -Ddb.password=root -Ddb.max.size=16 \
      -jar                                              \
      points-service-vertx-1.0.0-SNAPSHOT-fat.jar       \
