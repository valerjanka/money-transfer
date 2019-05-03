package com.val.money.transfer.controller.flow;

import com.val.money.transfer.model.Account;

public class AccountPair {
    private Account fromAccount;
    private Account toAccount;

    public AccountPair(Account fromAccount, Account toAccount) {
        this.fromAccount = fromAccount;
        this.toAccount = toAccount;
    }

    public Account getFromAccount() {
        return fromAccount;
    }

    public Account getToAccount() {
        return toAccount;
    }
}
