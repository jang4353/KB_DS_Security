package com.android.insecurebankv2;

public class Account_item {
    private String account_number;
    private String balance;
    private String username;

    public Account_item(String account_number, String balance, String username) {
        this.account_number = account_number;
        this.balance = balance;
        this.username = username;
    }

    public String getAccount_number() {
        return account_number;
    }

    public void setAccount_number(String account_number) {
        this.account_number = account_number;
    }

    public String getBalance() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
