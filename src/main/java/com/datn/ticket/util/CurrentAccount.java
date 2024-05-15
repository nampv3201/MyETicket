package com.datn.ticket.util;

import com.datn.ticket.model.Accounts;
import com.datn.ticket.model.Roles;
import lombok.Getter;
import org.springframework.stereotype.Component;

@Getter
@Component
public class CurrentAccount{
    private Accounts accounts;
    private String username;
    private int status;

    private static CurrentAccount instance;

    private CurrentAccount()
    {

    }
    public static synchronized CurrentAccount getInstance() {
        if (instance == null) {
            instance = new CurrentAccount();
        }
        return instance;
    }

    public void setAccounts(Accounts accounts) {
        this.accounts = accounts;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public static void setInstance(CurrentAccount instance) {
        CurrentAccount.instance = instance;
    }
}
