package com.datn.ticket.model;

import jakarta.persistence.*;
import lombok.Getter;

import java.util.Date;

@Getter
@Entity
@Table(name = "payment")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "payment_status")
    private String payment_status;
    @Column(name = "payment_time")
    private Date payment_time;
    @Column(name = "payment_amount")
    private Double payment_amount;
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "PaymentMethod_id", referencedColumnName = "id")
    private PaymentMethod method;
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "Users_id", referencedColumnName = "id")
    private Users users;

    public Payment() {
    }

    public Payment(int id, String payment_status, Date payment_time, Double payment_amount, PaymentMethod method, Users users) {
        this.id = id;
        this.payment_status = payment_status;
        this.payment_time = payment_time;
        this.payment_amount = payment_amount;
        this.method = method;
        this.users = users;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setPayment_status(String payment_status) {
        this.payment_status = payment_status;
    }

    public void setPayment_time(Date payment_time) {
        this.payment_time = payment_time;
    }

    public void setPayment_amount(Double payment_amount) {
        this.payment_amount = payment_amount;
    }

    public void setMethod(PaymentMethod method) {
        this.method = method;
    }

    public void setUsers(Users users) {
        this.users = users;
    }
}
