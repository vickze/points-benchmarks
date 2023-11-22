# docker build -f postgresql.dockerfile -t points/postgresql .
# docker run -it --name=points-postgresql -p 15432:5432 --rm --net points points/postgresql

FROM postgres:14

ENV POSTGRES_USER=postgres
ENV POSTGRES_PASSWORD=postgres
ENV POSTGRES_DB=points

ENV POSTGRES_HOST_AUTH_METHOD=md5
ENV POSTGRES_INITDB_ARGS=--auth-host=md5
ENV PGDATA=/ssd/postgresql

ENV TZ="Asia/Shanghai"

COPY postgresql-min.conf /tmp/postgresql.conf

COPY create-postgres.sql /docker-entrypoint-initdb.d/
COPY config.sh /docker-entrypoint-initdb.d/

COPY 60-postgresql-shm.conf /etc/sysctl.d/60-postgresql-shm.conf
