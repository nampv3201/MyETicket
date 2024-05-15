package com.datn.ticket.model.dto;

import lombok.Getter;

@Getter
public class AccountsDTO {
    private String username, password;
    int role;

    public AccountsDTO() {
    }

    public AccountsDTO(String username, String password, int role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setRole(int role) {
        this.role = role;
    }
}
