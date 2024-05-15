package com.datn.ticket.model.dto;

import lombok.Getter;

@Getter
public class CreateTicketsDTO {

    private int id;
    private String type_name;
    private double cost;
    private int available;

    public CreateTicketsDTO() {
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setType_name(String type_name) {
        this.type_name = type_name;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public void setAvailable(int available) {
        this.available = available;
    }
}
