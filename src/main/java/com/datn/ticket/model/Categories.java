package com.datn.ticket.model;

import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
@Table(name = "categories")
public class Categories {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "category_name")
    private String category_name;

    public Categories() {
    }

    public Categories(int id, String category_name) {
        this.id = id;
        this.category_name = category_name;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setCategory_name(String category_name) {
        this.category_name = category_name;
    }
}
