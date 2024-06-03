package com.datn.ticket.model;

import jakarta.persistence.*;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Date;

@Getter
@Entity
@Table(name = "account")
public class Accounts {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "username")
    private String username;

    @Column(name = "password")
    private String password;

    @Column(name = "create_at")
    private Date createAt;

    @Transient
    private ArrayList<Integer> roles;

    public Accounts() {
    }

    public Accounts(String username, String password, Date createAt) {
        this.username = username;
        this.password = password;
        this.createAt = createAt;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setCreateAt(Date status) {
        this.createAt = status;
    }

    public void setRoles(ArrayList<Integer> roles) {
        this.roles = roles;
    }
}
