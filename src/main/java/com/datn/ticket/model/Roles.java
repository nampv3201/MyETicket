package com.datn.ticket.model;

import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
@Table(name = "role")
public class Roles {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "role_name")
    private String role_name;

    public Roles() {
    }

    public Roles(int id, String role_name) {
        this.id = id;
        this.role_name = role_name;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setRole_name(String role_name) {
        this.role_name = role_name;
    }
}
