package com.datn.ticket.service;

import com.datn.ticket.dto.response.AMerchantResponse;
import com.datn.ticket.dto.response.AUserResponse;
import com.datn.ticket.model.Categories;
import com.datn.ticket.model.Merchants;
import com.datn.ticket.model.PaymentGateway;
import com.datn.ticket.dto.EventStatisticDTO;
import com.datn.ticket.dto.response.AccountResponse;
import com.datn.ticket.dto.response.ApiResponse;
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
    void changeEventStatus(int eventId);
    void changeAccountStatus(String username, String rolerName);
    ApiResponse<?> allEvents(Integer MerchantId, List<Integer> CategoryId, Integer allTime, String city);
    List<EventStatisticDTO> getStatistics(int merchantId) throws ParseException;

    ApiResponse<?> getPaymentHistory(String paymentDate, String status, Integer uId);
    ApiResponse<?> getPaymentHistoryDetail(String paymentId);

}
