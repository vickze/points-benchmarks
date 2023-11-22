# docker build -f spring-mvc-postgresql.dockerfile -t points/spring-mvc-postgresql .
# docker run -it --name=points-service -p 18080:8080 --rm --net points points/spring-mvc-postgresql

FROM openjdk:8-jdk-alpine

COPY target/points-service-mvc-0.0.1-SNAPSHOT.jar points-service-mvc-0.0.1-SNAPSHOT.jar

ENV TZ="Asia/Shanghai"

EXPOSE 8080

CMD java \
      -server \
      -Xms2G \
      -Xmx2G \
      -Ddb.type=postgresql -Ddb.port=5432 -Ddb.host=points-postgresql -Ddb.database=points -Ddb.user=postgres -Ddb.password=postgres  -Ddb.max.size=16 \
      -jar                                              \
      points-service-mvc-0.0.1-SNAPSHOT.jar       \