package com.datn.ticket.model;

import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
@Table(name = "paymentmethod")
public class PaymentMethod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "method_name")
    private String method_name;

    public PaymentMethod() {
    }

    public PaymentMethod(String method_name) {
        this.method_name = method_name;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setMethod_name(String method_name) {
        this.method_name = method_name;
    }
}
