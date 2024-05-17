package com.datn.ticket.service;

import com.datn.ticket.model.Categories;
import com.datn.ticket.model.Merchants;

import java.util.List;

public interface AdminService {
    List<Categories> getAllCategories();
    Categories getSingleCategory(int catId);
    Merchants getMerchantInfor(Integer id);
    List<Merchants> getListMerchants();
}
