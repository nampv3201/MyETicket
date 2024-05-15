package com.datn.ticket.service;

import com.datn.ticket.model.Categories;

import java.util.List;

public interface AdminService {
    List<Categories> getAllCategories();
    Categories getSingleCategory(int catId);
}
