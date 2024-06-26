package com.datn.ticket.service;

import com.datn.ticket.dto.EventHome;
import com.datn.ticket.dto.response.EventHomeResponse;
import com.datn.ticket.model.Categories;
import com.datn.ticket.model.CreateTickets;
import com.datn.ticket.dto.EventDTO;
import com.datn.ticket.dto.response.ApiResponse;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface EventService {
    ApiResponse<EventDTO> getEvent(int id);
    ResponseEntity<Object> findEventByName(String name);
    EventHomeResponse getEventByFilterWithPage(Integer offset, Integer size, Integer MerchantId, List<Integer> CategoryId, String time, List<String> city,
                                               String fromTime, String toTime, Double minPrice, Double maxPrice);
    ApiResponse<?> getEventByCategory(String category);
    CreateTickets getTicketType(Integer ticketTypeID);
    List<Categories> getAllCategories();
    List<Categories> getCategories(List<Integer> ids);
    List<CreateTickets> getTicketTypeByEvent(int eventId);
    CreateTickets getTicketTypeUpdate(int typeId);
    List<Categories> getCatByEvent(int eventId);
    Categories getSingleCategory(int catId);
    List<EventHome> getEventByCatIdWithLimit(int catId, int limit);
}
