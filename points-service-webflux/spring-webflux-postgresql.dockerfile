# docker build -f spring-webflux-postgresql.dockerfile -t points/spring-webflux-postgresql .
# docker run -it --name=points-service -p 18080:8080 --rm --net points points/spring-webflux-postgresql

FROM openjdk:8-jdk-alpine

COPY target/points-service-webflux-0.0.1-SNAPSHOT.jar points-service-webflux-0.0.1-SNAPSHOT.jar

ENV TZ="Asia/Shanghai"

EXPOSE 8080

CMD java \
      -server \
      -Xms2G \
      -Xmx2G \
      -Ddb.type=postgres -Ddb.port=5432 -Ddb.host=points-postgresql -Ddb.database=points -Ddb.user=postgres -Ddb.password=postgres  -Ddb.max.size=16 \
      -jar                                              \
      points-service-webflux-0.0.1-SNAPSHOT.jar       \