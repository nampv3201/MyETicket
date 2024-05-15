package com.datn.ticket.model;

import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
@Table(name = "invoice")
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "Cart_id", referencedColumnName = "id")
    private Cart cart;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "Payment_id", referencedColumnName = "id")
    private Payment payment;

    public Invoice() {
    }

    public Invoice(Cart cart, Payment payment) {
        this.cart = cart;
        this.payment = payment;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setCart(Cart cart) {
        this.cart = cart;
    }

    public void setPayment(Payment payment) {
        this.payment = payment;
    }
}
