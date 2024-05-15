package com.datn.ticket.model.dto;

import lombok.Getter;

@Getter
public class EventStatisticDTO {
    private int eventId;
    private String eventName;
    private int totalTicket, soldTicket;
    private String categories, status;
    private double totalRevenue;


    public EventStatisticDTO() {
    }

    public EventStatisticDTO(int eventId, String eventName, int totalTicket, int soldTicket, String categories, String status, double totalRevenue) {
        this.eventId = eventId;
        this.eventName = eventName;
        this.totalTicket = totalTicket;
        this.soldTicket = soldTicket;
        this.categories = categories;
        this.status = status;
        this.totalRevenue = totalRevenue;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public void setTotalTicket(int totalTicket) {
        this.totalTicket = totalTicket;
    }

    public void setSoldTicket(int soldTicket) {
        this.soldTicket = soldTicket;
    }

    public void setTotalRevenue(double totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public void setCategories(String categories) {
        this.categories = categories;
    }
}
