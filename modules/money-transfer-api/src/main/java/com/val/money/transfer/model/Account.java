package com.val.money.transfer.model;

import java.math.BigDecimal;

public class Account {
    private String accountNumber;
    private long userId;
    private BigDecimal balance;
    private String currency;

    public Account(String accountNumber, long userId, BigDecimal balance, String currency) {
        this.accountNumber = accountNumber;
        this.userId = userId;
        this.balance = balance;
        this.currency = currency;
    }

    public String getAccountNumber() {
        return accountNumber;
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
