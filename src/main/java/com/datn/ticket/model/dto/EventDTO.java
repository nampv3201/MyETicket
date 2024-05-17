package com.datn.ticket.model.dto;

import com.datn.ticket.model.dto.response.CreateTicketsResponse;
import lombok.Getter;

import java.util.Date;
import java.util.List;

@Getter
public class EventDTO {
    private int id;

    private String name;
    private String description;
    private String location;
    private Date start_time;
    private Date end_time;
    private Date start_booking;
    private Date end_booking;
    private int max_limit;
    private String banner;
    private int merchantId;
    private String merchantName;
    private List<String> categories;

    private List<CreateTicketsResponse> createTicketsResponseList;

    public EventDTO(){

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

    public void setMerchantId(int merchantId) {
        this.merchantId = merchantId;
    }

    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    public void setCreateTicketsResponseList(List<CreateTicketsResponse> createTicketsResponseList) {
        this.createTicketsResponseList = createTicketsResponseList;
    }
}
