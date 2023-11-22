# docker build -f mysql.dockerfile -t points/mysql .
# docker run -it --name=points-mysql -p 13306:3306 --rm --net points points/mysql
FROM mysql:8.0

ENV MYSQL_ROOT_USER=root
ENV MYSQL_ROOT_PASSWORD=root
ENV MYSQL_DATABASE=points

ENV TZ="Asia/Shanghai"

COPY my.cnf /etc/mysql/
COPY create.sql /docker-entrypoint-initdb.d/

RUN chmod 755 /etc/mysql/my.cnf

COPY 60-database-shm.conf /etc/sysctl.d/60-database-shm.conf