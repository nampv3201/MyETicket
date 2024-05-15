package com.datn.ticket.service;

import com.datn.ticket.model.Categories;
import com.datn.ticket.model.CreateTickets;
import com.datn.ticket.model.Events;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface EventService {
    ResponseEntity<Object> getEvent(int id);
    ResponseEntity<Object> findEventByName(String name);
    ResponseEntity<Object> getEventByFilter(Integer MerchantId, List<Integer> CategoryId, Integer allTime);
    List<Categories> getAllCategories();
    void addEvent(Events events, List<CreateTickets> ticketsList, List<Categories> categories);

    // userService.addToCart
    CreateTickets getTicketType(Integer ticketTypeID);


    // Service for update task
    List<Categories> getCategories(List<Integer> ids);
    ResponseEntity<Object> UpdateEvent(Events events, List<CreateTickets> updateTickets,
                                       List<CreateTickets> newTickets, List<Categories> newCategories, List<Categories> removeCategories);
    Events getEventUpdate(int eventId);

    // eventService.getTicketUpdate, userService.getAllTicketType
    List<CreateTickets> getTicketTypeByEvent(int eventId);
    CreateTickets getTicketTypeUpdate(int typeId);
    List<Categories> getCatByEvent(int eventId);
    ResponseEntity<Object> DeleteEvent(int id);
    Categories getSingleCategory(int catId);
}
