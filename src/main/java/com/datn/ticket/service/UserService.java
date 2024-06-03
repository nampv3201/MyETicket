package com.datn.ticket.service;

import com.datn.ticket.dto.response.*;
import com.datn.ticket.model.Cart;
import com.datn.ticket.model.Users;
import com.datn.ticket.dto.request.UpdateCartRequest;

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
    void payment(PaymentResponse response);
    List<HistoryResponse> myHistory();
    HistoryResponseDetail getHistoryResponseDetail(String id);
}
