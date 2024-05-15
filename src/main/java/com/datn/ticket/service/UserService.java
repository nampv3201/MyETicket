package com.datn.ticket.service;

import com.datn.ticket.model.Cart;
import com.datn.ticket.model.Users;
import com.datn.ticket.model.dto.request.UpdateCartRequest;
import com.datn.ticket.model.dto.response.CartResponse;
import com.datn.ticket.model.dto.response.PaymentResponse;

import java.util.List;

public interface UserService {

    void updateUser(Users user);
    Users myInfor();

    void addToCart(List<Cart> carts);
    List<CartResponse> myCart();
    void removeFromCart(List<Integer> cartId);
    void updateCart(UpdateCartRequest request);
    void payment(PaymentResponse response);
//    public List<History> myHistory();
}
