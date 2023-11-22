# docker build -f spring-mvc-mysql.dockerfile -t points/spring-mvc-mysql .
# docker run -it --name=points-service -p 18080:8080 --rm --net points points/spring-mvc-mysql

FROM openjdk:8-jdk-alpine

COPY target/points-service-mvc-0.0.1-SNAPSHOT.jar points-service-mvc-0.0.1-SNAPSHOT.jar

ENV TZ="Asia/Shanghai"

EXPOSE 8080

CMD java \
      -server \
      -Xms2G \
      -Xmx2G \
      -Ddb.type=mysql -Ddb.port=3306 -Ddb.host=points-mysql -Ddb.database=points -Ddb.user=root -Ddb.password=root -Ddb.max.size=16 \
      -jar                                              \
      points-service-mvc-0.0.1-SNAPSHOT.jar          \