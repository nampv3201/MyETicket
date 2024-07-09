package com.datn.ticket.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
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

    @Column(name = "city")
    private String city;

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

    @Column(name="status")
    private String status;

    @Column(name="deleted")
    private int deleted;


    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "Merchants_id", referencedColumnName = "id")
    private Merchants merchants;
}
