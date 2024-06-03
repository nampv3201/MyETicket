package com.datn.ticket.dto;

import lombok.Getter;

@Getter
public class StatisticsDetail {
    private int eventId;
    private String eventName;
    private String categories;
    private String status;
    private int ticketTypeId;
    private String ticketTypeName;
    private int totalTicket;
    private int soldTicket;
    private double typeRevenue;

    public StatisticsDetail() {
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public void setCategories(String categories) {
        this.categories = categories;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setTicketTypeId(int ticketTypeId) {
        this.ticketTypeId = ticketTypeId;
    }

    public void setTicketTypeName(String ticketTypeName) {
        this.ticketTypeName = ticketTypeName;
    }

    public void setTotalTicket(int totalTicket) {
        this.totalTicket = totalTicket;
    }

    public void setSoldTicket(int soldTicket) {
        this.soldTicket = soldTicket;
    }

    public void setTypeRevenue(double typeRevenue) {
        this.typeRevenue = typeRevenue;
    }
}
