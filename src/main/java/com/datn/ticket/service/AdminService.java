package com.datn.ticket.service;

import com.datn.ticket.model.Categories;
import com.datn.ticket.model.Merchants;
import com.datn.ticket.model.PaymentGateway;
import com.datn.ticket.model.dto.EventStatisticDTO;
import com.datn.ticket.model.dto.response.AccountResponse;
import com.datn.ticket.model.dto.response.ApiResponse;
import com.datn.ticket.model.dto.response.PaymentHistoryResponse;
import com.datn.ticket.model.dto.response.PaymentHistoryResponseDetail;

import java.text.ParseException;
import java.util.List;

public interface AdminService {
    List<AccountResponse> getAccount();
    AccountResponse getByID(Integer id);
    Merchants getMerchantInfor(Integer id);
    List<Merchants> getListMerchants();
    List<Categories> getAllCategories();
    void addNewCategory(Categories categories);
    void removeCategory(int catId);
    void addNewPaymentGateway(PaymentGateway gateway);
    void disableEvent(int eventId);
    void enableEvent(int eventId);
    void disaleAccount(int accountId, int roleId);
    void enableAccount(int accountId, int roleId);
    ApiResponse<?> allEvents(Integer MerchantId, List<Integer> CategoryId, Integer allTime, String city);
    List<EventStatisticDTO> getStatistics(int merchantId) throws ParseException;

    ApiResponse<?> getPaymentHistory(String paymentDate, String status, Integer uId);
    ApiResponse<?> getPaymentHistoryDetail(String paymentId);

}
