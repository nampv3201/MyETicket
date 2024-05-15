package com.datn.ticket.model;

import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
@Table(name = "cart")
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "quantity")
    private int quantity;
    @Column(name = "cost")
    private double cost;
    @Column(name = "status")
    private int status;
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "Users_id", referencedColumnName = "id")
    private Users user;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "CreateTicket_id", referencedColumnName = "id")
    private CreateTickets createTickets;

    public Cart() {
    }

    public Cart(int quantity, double cost, int status, Users user, CreateTickets createTickets) {
        this.quantity = quantity;
        this.cost = cost;
        this.status = status;
        this.user = user;
        this.createTickets = createTickets;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setUser(Users user) {
        this.user = user;
    }

    public void setCreateTickets(CreateTickets createTickets) {
        this.createTickets = createTickets;
    }
}
