package com.datn.ticket.repository;

import com.datn.ticket.model.Categories;
import com.datn.ticket.service.AdminService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class AdminServiceImpl implements AdminService {
    private final EntityManager manager;

    @Autowired
    public AdminServiceImpl(EntityManager manager) {
        this.manager = manager;
    }

    @Override
    public List<Categories> getAllCategories() {
        TypedQuery<Categories> query = manager.createQuery("select c from Categories c", Categories.class);
        return query.getResultList();
    }

    @Override
    public Categories getSingleCategory(int catId) {
        TypedQuery<Categories> query = manager.createQuery("Select c from Categories c where c.id  = :id", Categories.class);
        query.setParameter("id", catId);
        return query.getSingleResult();
    }
}
