package com.val.money.transfer.model;

import java.math.BigDecimal;

public class Account {
    private long id;
    private long userId;
    private BigDecimal balance;
    private String currency;

    public Account() {
    }

    public Account(long id, long userId, BigDecimal balance, String currency) {
        this.id = id;
        this.userId = userId;
        this.balance = balance;
        this.currency = currency;
    }

    public long getId() {
        return id;
    }

    public long getUserId() {
        return userId;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public String getCurrency() {
        return currency;
    }
}
