package com.datn.ticket.service;

import com.datn.ticket.dto.response.*;
import com.datn.ticket.model.Categories;
import com.datn.ticket.model.Merchants;
import com.datn.ticket.model.PaymentGateway;
import com.datn.ticket.dto.EventStatisticDTO;
import com.datn.ticket.model.Users;

import java.text.ParseException;
import java.util.List;

public interface AdminService {
    List<AccountResponse> getAccount();
    AccountResponse getByID(Integer id);

    Merchants getMerchantInfor(Integer id);
    List<AMerchantResponse> getListMerchants();

    Users getUserInfor(Integer id);
    List<AUserResponse> getListUsers();

    List<Categories> getAllCategories();
    void addNewCategory(Categories categories);
    void removeCategory(int catId);
    void addNewPaymentGateway(PaymentGateway gateway);
    void changeEventStatus(int eventId, String status);
    void changeAccountStatus(String username, String rolerName);
    ApiResponse<?> allEvents(Integer offset, Integer size, String merchantName, List<Integer> CategoryId, String time,
                             List<String> city, String fromTime, String toTime, Double minPrice, Double maxPrice, String status);
    List<EventStatisticDTO> getStatistics(int merchantId) throws ParseException;

    ApiResponse<?> getPaymentHistory(String paymentDate, String status, Integer uId);
    ApiResponse<?> getPaymentHistoryDetail(String paymentId);
    List<AdminEventResponse> getEventPending();

}
