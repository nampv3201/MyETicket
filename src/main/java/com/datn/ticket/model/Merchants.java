package com.datn.ticket.model;


import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
@Table(name = "merchants")
public class Merchants {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "name")
    private String name;
    @Column(name = "address")
    private String address;
    @Column(name = "phone")
    private String phone;
    @Column(name = "license")
    private String license;
    @Column(name = "description")
    private String description;
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "Account_id", referencedColumnName = "id")
    private Accounts accounts;

    public Merchants() {
    }

    public Merchants(int id, String name, String address, String phone, String license, String description, Accounts accounts) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.license = license;
        this.description = description;
        this.accounts = accounts;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setLicense(String license) {
        this.license = license;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setAccounts(Accounts accounts) {
        this.accounts = accounts;
    }
}
