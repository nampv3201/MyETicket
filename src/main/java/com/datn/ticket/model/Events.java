package com.datn.ticket.model;

import jakarta.persistence.*;
import lombok.Getter;

import java.util.Date;

@Getter
@Entity
@Table(name = "events")
public class Events {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "name")
    private String name;
    @Column(name = "description")
    private String description;
    @Column(name = "location")
    private String location;
    @Column(name = "start_time")
    private Date start_time;
    @Column(name = "end_time")
    private Date end_time;
    @Column(name = "start_booking")
    private Date start_booking;
    @Column(name = "end_booking")
    private Date end_booking;
    @Column(name = "max_limit")
    private int max_limit;
    @Column(name = "banner")
    private String banner;
    @Getter
    @Column(name="status")
    private int status;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "Merchants_id", referencedColumnName = "id")
    private Merchants merchants;

    public Events(String name, String description, String location, Date start_time, Date end_time, Date start_booking, Date endBooking, int max_limit, String banner, int status, Merchants merchants) {
        this.name = name;
        this.description = description;
        this.location = location;
        this.start_time = start_time;
        this.end_time = end_time;
        this.start_booking = start_booking;
        this.end_booking = endBooking;
        this.max_limit = max_limit;
        this.banner = banner;
        this.status = status;
        this.merchants = merchants;
    }

    public Events(){

    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setStart_time(Date start_time) {
        this.start_time = start_time;
    }

    public void setEnd_time(Date end_time) {
        this.end_time = end_time;
    }

    public void setStart_booking(Date start_booking) {
        this.start_booking = start_booking;
    }

    public void setEnd_booking(Date end_booking) {
        this.end_booking = end_booking;
    }

    public void setMax_limit(int max_limit) {
        this.max_limit = max_limit;
    }

    public void setBanner(String banner) {
        this.banner = banner;
    }

    public void setMerchants(Merchants merchants) {
        this.merchants = merchants;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
