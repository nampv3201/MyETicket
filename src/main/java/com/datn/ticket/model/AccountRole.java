package com.datn.ticket.model;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "account_has_role")
public class AccountRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Getter
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "Account_id", referencedColumnName = "id")
    private Accounts accounts;

    @Getter
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "role_id", referencedColumnName = "id")
    private Roles roles;

    public AccountRole() {
    }

    public AccountRole(Accounts accounts, Roles roles) {
        this.accounts = accounts;
        this.roles = roles;
    }

    public void setAccounts(Accounts accounts) {
        this.accounts = accounts;
    }

    public void setRoles(Roles roles) {
        this.roles = roles;
    }
}
