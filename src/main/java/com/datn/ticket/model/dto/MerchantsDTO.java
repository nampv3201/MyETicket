package com.datn.ticket.model.dto;

import com.datn.ticket.model.Merchants;
import lombok.Getter;

@Getter
public class MerchantsDTO {
    private String name, address, phone, license, description;

    public MerchantsDTO() {
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

    public void setLicense(String license) {
        this.license = license;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
