package com.points.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransactionResponse {

    private Long id;

    private Integer type;

    private Long amount;

    private Long previousBalance;

    private Long afterBalance;

    private Long transactionId;

    private String status;

    private String msg;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public Long getPreviousBalance() {
        return previousBalance;
    }

    public void setPreviousBalance(Long previousBalance) {
        this.previousBalance = previousBalance;
    }

    public Long getAfterBalance() {
        return afterBalance;
    }

    public void setAfterBalance(Long afterBalance) {
        this.afterBalance = afterBalance;
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
