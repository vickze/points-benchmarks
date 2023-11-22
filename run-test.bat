@echo off

setlocal enabledelayedexpansion

rem 校验参数
if "%~1"=="" (
    echo Invalid arguments. Usage: run-test.bat ^<database^> ^<service^>
    goto :end
)

if "%~2"=="" (
    echo Invalid arguments. Usage: run-test.bat ^<database^> ^<service^>
    goto :end
)


set database=%1
set service=%2

rem 创建名为 points 的 Docker 网络（如果不存在）
docker network inspect points > nul 2>&1
if %errorlevel% equ 1 (
    docker network create points
) else (
    echo points network already exists
)

rem 构建并运行数据库容器
if "%database%"=="mysql" (
    cd toolset\databases\mysql
    docker build -t points/mysql -f mysql.dockerfile .
    docker run -d --name points-mysql --network points -p 13306:3306 points/mysql
    cd ..\..\..
) else if "%database%"=="postgresql" (
    cd toolset\databases\postgres
    docker build -t points/postgres-image -f postgresql.dockerfile .
    docker run -d --name points-postgresql --network points -p 15432:5432 points/postgresql
    cd ..\..\..
) else (
    echo Invalid database option. Supported options: mysql, postgresql
    goto :end
)

rem 暂停10秒等待容器启动
timeout /t 20

rem 构建并运行服务容器
if "%service%"=="mvc" (
    cd points-service-mvc
    if "%database%"=="mysql" (
        docker build -t points/spring-mvc-mysql -f spring-mvc-mysql.dockerfile .
        docker run -d --name points-service --network points -p 18080:8080 points/spring-mvc-mysql
    ) else if "%database%"=="postgresql" (
        docker build -t points/spring-mvc-postgresql -f spring-mvc-postgresql.dockerfile .
        docker run -d --name points-service --network points points/spring-mvc-postgresql
    ) else (
        echo Invalid database option. Supported options: mysql, postgresql
        goto :end
    )
    cd ..
) else if "%service%"=="vertx" (
    cd points-service-vertx
    if "%database%"=="mysql" (
        docker build -t points/vertx-web-mysql -f vertx-web-mysql.dockerfile .
        docker run -d --name points-service --network points points/vertx-web-mysql
    ) else if "%database%"=="postgresql" (
        docker build -t points/vertx-web-postgresql -f vertx-web-postgresql.dockerfile .
        docker run -d --name points-service --network points -p 18080:8080 points/vertx-web-postgresql
    ) else (
        echo Invalid database option. Supported options: mysql, postgresql
        goto :end
    )
    cd ..
) else (
    echo Invalid service option. Supported options: mvc, vertx
    goto :end
)

rem 暂停10秒等待容器启动
timeout /t 10

rem 在 wrk 目录中构建并运行容器
cd toolset\wrk
docker build -t points/wrk -f wrk.dockerfile .

rem 定义数组
set normallist=plainText;json

rem 循环遍历数组
for %%n in (%normallist%) do (
    echo Test %%n start...
	echo wrk -t4 -c32 -d1s http://points-service:8080/%%n
	docker run --rm --name points-wrk --network points points/wrk -t4 -c16 -d1s http://points-service:8080/%%n

	echo wrk -t8 -c128 -d1s http://points-service:8080/%%n
	docker run --rm --name points-wrk --network points points/wrk -t8 -c128 -d1s http://points-service:8080/%%n

	echo wrk -t16 -c256 -d1s http://points-service:8080/%%n
	docker run --rm --name points-wrk --network points points/wrk -t16 -c256 -d1s http://points-service:8080/%%n

	echo wrk -t16 -c512 -d1s http://points-service:8080/%%n
	docker run --rm --name points-wrk --network points points/wrk -t16 -c512 -d1s http://points-service:8080/%%n

	echo wrk -t32 -c1024 -d1s http://points-service:8080/%%n
	docker run --rm --name points-wrk --network points points/wrk -t32 -c1024 -d1s http://points-service:8080/%%n
)

rem 定义数组
set lualist=createAccount;updateAccount;getAccount;transaction;getAccountAndTransaction

for %%l in (%lualist%) do (
    echo Test %%l start...
	echo wrk -t4 -c32 -d1s -s %%l.lua http://points-service:8080
	docker run --rm --name points-wrk --network points points/wrk -t4 -c16 -d1s -s %%l.lua http://points-service:8080

	echo wrk -t8 -c128 -d1s -s %%l.lua http://points-service:8080
	docker run --rm --name points-wrk --network points points/wrk -t8 -c128 -d1s -s %%l.lua http://points-service:8080

	echo wrk -t16 -c256 -d1s -s %%l.lua http://points-service:8080
	docker run --rm --name points-wrk --network points points/wrk -t16 -c256 -d1s -s %%l.lua http://points-service:8080

	echo wrk -t16 -c512 -d1s -s %%l.lua http://points-service:8080
	docker run --rm --name points-wrk --network points points/wrk -t16 -c512 -d1s -s %%l.lua http://points-service:8080

	echo wrk -t32 -c1024 -d1s -s %%l.lua http://points-service:8080
	docker run --rm --name points-wrk --network points points/wrk -t32 -c1024 -d1s -s %%l.lua http://points-service:8080
)

cd ..\..

echo Test successfully!

echo Cleaning up...

rem 删除容器
if "%database%"=="mysql" (
	docker stop points-mysql
	docker rm points-mysql
) else if "%database%"=="postgresql" (
	docker stop points-postgresql
	docker rm points-postgresql
)

docker stop points-service
docker rm points-service

echo Cleanup completed!

:end