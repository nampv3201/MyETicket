package com.datn.ticket.controller;

import com.datn.ticket.dto.response.*;
import com.datn.ticket.exception.AppException;
import com.datn.ticket.exception.ErrorCode;
import com.datn.ticket.model.Cart;
import com.datn.ticket.model.Users;
import com.datn.ticket.dto.request.AddToCartRequest;
import com.datn.ticket.dto.request.RemoveFromCartRequest;
import com.datn.ticket.dto.request.UpdateCartRequest;
import com.datn.ticket.dto.request.UserUpdateRequest;
import com.datn.ticket.model.mapper.UsersMapper;
import com.datn.ticket.service.EventService;
import com.datn.ticket.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
//@RequestMapping("/user")
@Tag(name = "User Controller")
public class UserController {

    private final UserService userService;
    private final EventService eventService;

    @Autowired
    public UserController(UserService userService, EventService eventService) {
        this.userService = userService;
        this.eventService = eventService;
    }

    @Operation(summary = "Lấy thông tin chi tiết của 1 người dùng")
    @GetMapping("/user/profile")
    public ApiResponse<UserInforResponse> getUserInfor(){
        try{
            Users user = userService.myInfor();
            return ApiResponse.<UserInforResponse>builder()
                    .result(UsersMapper.INSTANCE.toUserDto(user))
                    .build();
        }catch (Exception e){
            throw new AppException(ErrorCode.USER_NOT_EXISTED);
        }
    }


    @Operation(summary = "Cập nhật thông tin chi tiết của 1 người dùng")
    @PostMapping("/user/update")
    public ApiResponse<UserInforResponse> updateUserInfor(@RequestBody UserUpdateRequest request){
        try{
            Users u = userService.myInfor();
            u.setName(request.getName());
            u.setAddress(request.getAddress());
            u.setPhone(request.getPhone());
            u.setAge(request.getAge());
            userService.updateUser(u);
            return ApiResponse.<UserInforResponse>builder()
                    .result(UsersMapper.INSTANCE.toUserDto(u))
                    .build();
        }catch (Exception e){
            throw new AppException(ErrorCode.USER_NOT_EXISTED);
        }
    }

    @Operation(summary = "Thêm sản phẩm vào giỏ hàng")
    @PostMapping("/cart/add")
    public ApiResponse addToCart(@RequestBody List<AddToCartRequest> requests){
        List<Cart> carts = new ArrayList<>();

        for(AddToCartRequest request : requests){
            Cart cart = new Cart();
            cart.setCreateTickets(eventService.getTicketType(request.getCreateTicketId()));
            cart.setQuantity(request.getQuantity());
            carts.add(cart);
        }
        try{
            userService.addToCart(carts);
            return ApiResponse.builder()
                   .message("Thêm vào giỏ hàng thành công")
                   .build();
        }catch (Exception e){
            return ApiResponse.builder()
                    .message(e.getMessage())
                    .build();
        }
    }

    @Operation(summary = "Lấy thông tin giỏ hàng")
    @GetMapping("/my-cart")
    public ApiResponse<List<CartResponse>> getMyCart(){
        try{
            return ApiResponse.<List<CartResponse>>builder()
                    .result(userService.myCart())
                    .build();
        }catch (Exception e){
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    @Operation(summary = "Xóa sản phẩm khỏi giỏ hàng")
    @DeleteMapping("/cart/delete")
    public ApiResponse deleteCart(@RequestBody RemoveFromCartRequest request){
        try{
            userService.removeFromCart(request.getCartIds());
            return ApiResponse.builder()
                    .message("Đã xóa sản phẩm khỏi giỏ hàng")
                    .build();
        }catch (Exception e){
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    @Operation(summary = "Thay đổi số lượng sản phẩm")
    @PostMapping("/cart/update")
    public ApiResponse updateCart(@RequestBody UpdateCartRequest request){
        try{
            userService.updateCart(request);
            return ApiResponse.builder()
                    .build();
        }catch (Exception e){
            return ApiResponse.builder()
                    .message(e.getMessage())
                    .build();
        }
    }

    @Operation(summary = "Lấy lịch sử mua hàng")
    @GetMapping("/history")
    public ApiResponse<List<HistoryResponse>> getHistory(){
        try{
            return ApiResponse.<List<HistoryResponse>>builder()
                    .result(userService.myHistory())
                    .build();
        }catch (Exception e){
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    @Operation(summary = "Lấy chi tiết lịch sử mua hàng")
    @GetMapping("/history/{id}")
    public ApiResponse<?> getHistoryDetail(@PathVariable("id") String id){
        try{
            return ApiResponse.<HistoryResponseDetail>builder().result(userService.getHistoryResponseDetail(id)).build();
        }catch (Exception e){
            log.error(e.getMessage());
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }
}
