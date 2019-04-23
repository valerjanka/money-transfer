package com.val.money.transfer.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public class Transaction {
    @JsonProperty(required = true)
    private String fromAccountNumber;
    @JsonProperty(required = true)
    private String toAccountNumber;
    @JsonProperty(required = true)
    private BigDecimal transferAmount;

    public Transaction() {
    }

    public Transaction(String fromAccountNumber, String toAccountNumber, BigDecimal transferAmount) {
        this.fromAccountNumber = fromAccountNumber;
        this.toAccountNumber = toAccountNumber;
        this.transferAmount = transferAmount;
    }

    public String getFromAccountNumber() {
        return fromAccountNumber;
    }

    public String getToAccountNumber() {
        return toAccountNumber;
    }

    public BigDecimal getTransferAmount() {
        return transferAmount;
    }
}
