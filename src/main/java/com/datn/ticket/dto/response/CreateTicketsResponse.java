package com.datn.ticket.dto.response;

import lombok.Getter;

@Getter
public class CreateTicketsResponse {

    private int id;
    private String type_name;
    private double price;
    private int available;

    public CreateTicketsResponse() {
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

    public void setAvailable(int available) {
        this.available = available;
    }
}
