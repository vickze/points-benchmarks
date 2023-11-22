package com.points.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.points.model.PointsTransaction;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class GetAccountAndTransactionResponse {

    private Long id;

    private String name;

    private Long balance;

    private List<PointsTransaction> transactions;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getBalance() {
        return balance;
    }

    public void setBalance(Long balance) {
        this.balance = balance;
    }

    public List<PointsTransaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<PointsTransaction> transactions) {
        this.transactions = transactions;
    }
}
