package com.datn.ticket.model.dto;

import lombok.Getter;

import java.time.LocalDate;

@Getter
public class EventHome {
    private int id;
    private String name;
    private String banner;
    private String city;
    private String location;
    private LocalDate startDate;
    private Double minPrice;

    public EventHome(int id, String name, String banner, String city, String location, LocalDate startDate, Double minPrice) {
        this.id = id;
        this.name = name;
        this.banner = banner;
        this.city = city;
        this.location = location;
        this.startDate = startDate;
        this.minPrice = minPrice;
    }

    public EventHome() {
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCity(String city){
        this.city = city;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public void setMinPrice(Double minPrice) {
        this.minPrice = minPrice;
    }

    public void setBanner(String banner) {
        this.banner = banner;
    }
}
