package com.datn.ticket.model.dto;

import lombok.Getter;

@Getter
public class UsersDTO {
    private String name, address, phone;
    private int age, point;

    public UsersDTO() {
    }

    public UsersDTO(String name, String address, String phone, int age, int point) {
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.age = age;
        this.point = point;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public void setPoint(int point) {
        this.point = point;
    }
}
