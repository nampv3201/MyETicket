package com.datn.ticket.model;

import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
@Table(name = "createticket")
public class CreateTickets {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(name = "type_name")
    private String type_name;
    @Column(name = "price")
    private double price;
    @Column(name = "count")
    private int count;
    @Column(name = "available")
    private int available;
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "Merchants_id", referencedColumnName = "id")
    private Merchants merchants;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "Events_id", referencedColumnName = "id")
    private Events events;

    public CreateTickets(){

    }

    public CreateTickets(String type_name, double price, int count, int available, Merchants merchants, Events events) {
        this.type_name = type_name;
        this.price = price;
        this.count = count;
        this.available = available;
        this.merchants = merchants;
        this.events = events;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setType_name(String type_name) {
        this.type_name = type_name;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void setAvailable(int available) {
        this.available = available;
    }

    public void setMerchants(Merchants merchants) {
        this.merchants = merchants;
    }

    public void setEvents(Events events) {
        this.events = events;
    }
}
