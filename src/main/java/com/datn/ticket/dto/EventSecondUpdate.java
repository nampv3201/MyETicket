package com.datn.ticket.dto;

import com.datn.ticket.dto.response.CreateTicketsResponse;
import lombok.Getter;

import java.util.Date;
import java.util.List;

@Getter
public class EventSecondUpdate {
    private Date start_time;
    private Date end_time;
    private Date start_booking;
    private Date end_booking;

    private List<CreateTicketsResponse> createTickets;

    public EventSecondUpdate() {
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

    public void setCreateTickets(List<CreateTicketsResponse> createTickets) {
        this.createTickets = createTickets;
    }
}
