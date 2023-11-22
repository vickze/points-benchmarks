#!/bin/bash

# 校验参数
if [ -z "$1" ] || [ -z "$2" ]; then
    echo "Invalid arguments. Usage: run-test.sh <database> <service>"
    exit 1
fi

database=$1
service=$2

# 创建名为 points 的 Docker 网络（如果不存在）
docker network inspect points > /dev/null 2>&1
if [ $? -eq 1 ]; then
    docker network create points
else
    echo "points network already exists"
fi

# 构建并运行数据库容器
if [ "$database" == "mysql" ]; then
    cd toolset/databases/mysql
    docker build -t points/mysql -f mysql.dockerfile .
    docker run -d --name points-mysql --network points -p 13306:3306 points/mysql
    cd ../../..
elif [ "$database" == "postgresql" ]; then
    cd toolset/databases/postgres
    docker build -t points/postgres-image -f postgresql.dockerfile .
    docker run -d --name points-postgresql --network points -p 15432:5432 points/postgres-image
    cd ../../..
else
    echo "Invalid database option. Supported options: mysql, postgresql"
    exit 1
fi

# 暂停10秒等待容器启动
sleep 10

# 构建并运行服务容器
if [ "$service" == "mvc" ]; then
    cd points-service-mvc
    if [ "$database" == "mysql" ]; then
        docker build -t points/spring-mvc-mysql -f spring-mvc-mysql.dockerfile .
        docker run -d --name points-service --network points -p 18080:8080 points/spring-mvc-mysql
    elif [ "$database" == "postgresql" ]; then
        docker build -t points/spring-mvc-postgresql -f spring-mvc-postgresql.dockerfile .
        docker run -d --name points-service --network points points/spring-mvc-postgresql
    else
        echo "Invalid database option. Supported options: mysql, postgresql"
        exit 1
    fi
    cd ..
elif [ "$service" == "vertx" ]; then
    cd points-service-vertx
    if [ "$database" == "mysql" ]; then
        docker build -t points/vertx-web-mysql -f vertx-web-mysql.dockerfile .
        docker run -d --name points-service --network points points/vertx-web-mysql
    elif [ "$database" == "postgresql" ]; then
        docker build -t points/vertx-web-postgresql -f vertx-web-postgresql.dockerfile .
        docker run -d --name points-service --network points -p 18080:8080 points/vertx-web-postgresql
    else
        echo "Invalid database option. Supported options: mysql, postgresql"
        exit 1
    fi
    cd ..
else
    echo "Invalid service option. Supported options: mvc, vertx"
    exit 1
fi

# 暂停10秒等待容器启动
sleep 10

# 在 wrk 目录中构建并运行容器
cd toolset/wrk
docker build -t points/wrk -f wrk.dockerfile .

# 定义数组
normallist="plainText json"

# 循环遍历数组
for n in $normallist; do
    echo "Test $n start..."
    echo "wrk -t4 -c32 -d1s http://points-service:8080/$n"
    docker run --rm --name points-wrk --network points points/wrk -t4 -c16 -d1s http://points-service:8080/$n

    echo "wrk -t8 -c128 -d1s http://points-service:8080/$n"
    docker run --rm --name points-wrk --network points points/wrk -t8 -c128 -d1s http://points-service:8080/$n

    echo "wrk -t16 -c256 -d1s http://points-service:8080/$n"
    docker run --rm --name points-wrk --network points points/wrk -t16 -c256 -d1s http://points-service:8080/$n

    echo "wrk -t16 -c512 -d1s http://points-service:8080/$n"
    docker run --rm --name points-wrk --network points points/wrk -t16 -c512 -d1s http://points-service:8080/$n

    echo "wrk -t32 -c1024 -d1s http://points-service:8080/$n"
    docker run --rm --name points-wrk --network points points/wrk -t32 -c1024 -d1s http://points-service:8080/$n
done

# 定义数组
lualist="createAccount updateAccount getAccount transaction getAccountAndTransaction"

for l in $lualist; do
    echo "Test $l start..."
    echo "wrk -t4 -c32 -d1s -s $l.lua http://points-service:8080"
    docker run --rm --name points-wrk --network points points/wrk -t4 -c16 -d1s -s $l.lua http://points-service:8080

    echo "wrk -t8 -c128 -d1s -s $l.lua http://points-service:8080"
    docker run --rm --name points-wrk --network points points/wrk -t8 -c128 -d1s -s $l.lua http://points-service:8080

    echo "wrk -t16 -c256 -d1s -s $l.lua http://points-service:8080"
    docker run --rm --name points-wrk --network points points/wrk -t16 -c256 -d1s -s $l.lua http://points-service:8080

    echo "wrk -t16 -c512 -d1s -s $l.lua http://points-service:8080"
    docker run --rm --name points-wrk --network points points/wrk -t16 -c512 -d1s -s $l.lua http://points-service:8080

    echo "wrk -t32 -c1024 -d1s -s $l.lua http://points-service:8080"
    docker run --rm --name points-wrk --network points points/wrk -t32 -c1024 -d1s -s $l.lua http://points-service:8080
done

cd ..

echo "Test successfully!"

echo "Cleaning up..."

# 删除容器
if [ "$database" == "mysql" ]; then
    docker stop points-mysql
    docker rm points-mysql
elif [ "$database" == "postgresql" ]; then
    docker stop points-postgresql
    docker rm points-postgresql
fi

docker stop points-service
docker rm points-service

echo "Cleanup completed!"
