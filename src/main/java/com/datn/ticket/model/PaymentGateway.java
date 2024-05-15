package com.datn.ticket.model;

import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
@Table(name = "paymentgateway")
public class PaymentGateway {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "gateway_name")
    private String gateway_name;

    public PaymentGateway() {
    }

    public PaymentGateway(String gateway_name) {
        this.gateway_name = gateway_name;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setGateway_name(String gateway_name) {
        this.gateway_name = gateway_name;
    }
}
