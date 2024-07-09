package com.datn.ticket.service;

import com.datn.ticket.model.Categories;
import com.datn.ticket.model.CreateTickets;
import com.datn.ticket.model.Events;
import com.datn.ticket.model.Merchants;
import com.datn.ticket.dto.EventStatisticDTO;
import com.datn.ticket.dto.StatisticsDetail;
import com.datn.ticket.dto.response.ApiResponse;

import java.text.ParseException;
import java.util.List;

public interface MerchantService {
    void updateMerchant(Merchants merchants);
    List<EventStatisticDTO> getStatistics() throws ParseException;
    List<StatisticsDetail> getStatisticsByEvent(int eventId) throws ParseException;
    Events getEventUpdate(int eventId);
    void addEvent(Events events, List<CreateTickets> ticketsList, List<Categories> categories);
    // Service for update task
    ApiResponse<?> UpdateEvent(Events events, List<CreateTickets> updateTickets,
                               List<CreateTickets> newTickets, List<Categories> newCategories, List<Categories> removeCategories);
    ApiResponse<?> myEvents(Integer status, List<Integer> CategoryId, String time, String city);
    String deEvents(int eventId);
    Merchants myInfor();

    ApiResponse<?> eventBookingHistory(Integer eventId);

    int revockTicket(String id);
}
