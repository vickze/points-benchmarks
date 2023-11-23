# docker build -f wrk.dockerfile -t points/wrk .
# docker run -it --rm --name=points-wrk --net points points/wrk -t8 -c512 -d15s -s transaction.lua http://points-service:8080

FROM williamyeh/wrk:4.0.2

# Required scripts for benchmarking
COPY createAccount.lua updateAccount.lua getAccount.lua transaction.lua getAccountAndTransaction.lua ./

