package com.datn.ticket.service;

import com.datn.ticket.dto.request.CreatePaymentRequest;
import com.datn.ticket.dto.request.SignUpMerchantInApp;
import com.datn.ticket.dto.response.*;
import com.datn.ticket.model.Cart;
import com.datn.ticket.model.Users;
import com.datn.ticket.dto.request.UpdateCartRequest;
import com.nimbusds.jose.JOSEException;

import java.text.ParseException;
import java.util.List;

public interface UserService {

    void updateUser(Users user);
    Users myInfor();

    void addToCart(List<Cart> carts);
    List<CartResponse> myCart();
    Cart getSingleCart(int cartId);
    List<Integer> directOrder(List<Cart> carts);
    void removeFromCart(List<Integer> cartId);
    void updateCart(UpdateCartRequest request);
    ApiResponse checkQuantity(int cartId);
    ApiResponse payment(PaymentResponse response);

    void createPayment(CreatePaymentRequest request);

    int checkPayment(String id);

    UserTicketResponse myHistory();
    List<HistoryResponseDetail> getHistoryResponseDetail(String pId, String eId);
    ApiResponse<?> signUpMerchant(SignUpMerchantInApp signUpRequest) throws ParseException, JOSEException;
}
