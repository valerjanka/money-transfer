package com.val.money.transfer.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public class UserTransaction {
    @JsonProperty(required = true)
    private Long fromAccountId;
    @JsonProperty(required = true)
    private Long toAccountId;
    @JsonProperty(required = true)
    private BigDecimal transferAmount;

    public UserTransaction() {
    }

    public UserTransaction(long fromAccountId, long toAccountId, BigDecimal transferAmount) {
        this.fromAccountId = fromAccountId;
        this.toAccountId = toAccountId;
        this.transferAmount = transferAmount;
    }

    public long getFromAccountId() {
        return fromAccountId;
    }

    public long getToAccountId() {
        return toAccountId;
    }

    public BigDecimal getTransferAmount() {
        return transferAmount;
    }

    public void setFromAccountId(long fromAccountId) {
        this.fromAccountId = fromAccountId;
    }

    public void setToAccountId(long toAccountId) {
        this.toAccountId = toAccountId;
    }

    public void setTransferAmount(BigDecimal transferAmount) {
        this.transferAmount = transferAmount;
    }
}
