package com.points.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Table
public class PointsTransaction {

    @Id
    private Long id;

    private Long accountId;

    private Integer type;

    private Long amount;

    private Long previousBalance;

    private Long afterBalance;

    private LocalDateTime createTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
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

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
