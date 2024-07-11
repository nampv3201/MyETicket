package com.datn.ticket.controller;

import com.datn.ticket.dto.response.*;
import com.datn.ticket.exception.AppException;
import com.datn.ticket.exception.ErrorCode;
import com.datn.ticket.model.Categories;
import com.datn.ticket.model.Merchants;
import com.datn.ticket.model.PaymentGateway;
import com.datn.ticket.dto.request.AddCatRequest;
import com.datn.ticket.dto.request.DEAccountRequest;
import com.datn.ticket.dto.request.NewGWRequest;
import com.datn.ticket.model.Users;
import com.datn.ticket.model.mapper.MerchantMapper;
import com.datn.ticket.model.mapper.UsersMapper;
import com.datn.ticket.service.AdminService;
import com.datn.ticket.service.EventService;
import com.datn.ticket.service.MerchantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@Slf4j
@Tag(name = "Admin Controller")
public class AdminController {
    @Autowired
    AdminService adminService;

    @Autowired
    MerchantService merchantService;

    @Autowired
    EventService eventService;

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Lấy danh sách tất cả các tài khoản")
    @GetMapping("/getAccount")
    public ApiResponse<List<AccountResponse>> getAccount(){
        return ApiResponse.<List<AccountResponse>>builder().result(adminService.getAccount()).build();
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Lấy 1 tài khoản cụ thể")
    @GetMapping("/account/{id}")
    public ApiResponse<AccountResponse> getById(@PathVariable("id") Integer id){
        log.info(SecurityContextHolder.getContext().getAuthentication().getName());
        for(GrantedAuthority text : SecurityContextHolder.getContext().getAuthentication().getAuthorities()){
            log.info(text.getAuthority());
        }
        return ApiResponse.<AccountResponse>builder()
                .result(adminService.getByID(id))
                .build();
    }
    @Operation(summary = "Lấy danh sách tất cả merchant trong hệ thống")
    @GetMapping("/merchantInfor")
    public ApiResponse<?> getMerchants(){
        try{
            return ApiResponse.<List<AMerchantResponse>>builder()
                    .result(adminService.getListMerchants())
                    .build();
        }catch (AppException e){
            throw new AppException(e.getErrorCode());
        }
    }

    @Operation(summary = "Lấy thông tin của 1 merchant")
    @GetMapping("/merchantInfor/{id}")
    public ApiResponse<?> getMerchantInfor(@PathVariable("id") int id){
        try{
            Merchants m = adminService.getMerchantInfor(id);
            return ApiResponse.builder()
                    .result(MerchantMapper.INSTANCE.merchantsDTO(m))
                    .build();
        }catch (AppException e){
            throw new AppException(e.getErrorCode());
        }
    }

    @Operation(summary = "Lấy danh sách tất cả user trong hệ thống")
    @GetMapping("/userInfor")
    public ApiResponse<?> getUsers(){
        try{
            return ApiResponse.<List<AUserResponse>>builder()
                    .result(adminService.getListUsers())
                    .build();
        }catch (AppException e){
            throw new AppException(e.getErrorCode());
        }
    }

    @Operation(summary = "Lấy thông tin của 1 user")
    @GetMapping("/userInfor/{id}")
    public ApiResponse<?> getUserInfor(@PathVariable("id") int id){
        try{
            Users u = adminService.getUserInfor(id);
            return ApiResponse.builder()
                    .result(UsersMapper.INSTANCE.toUserDto(u))
                    .build();
        }catch (AppException e){
            throw new AppException(e.getErrorCode());
        }
    }

    @Operation(summary = "Danh sách sự kiện phê duyệt")
    @GetMapping("/event/pending")
    public ApiResponse<?> getPendingEvent(){
        return ApiResponse.<List<AdminEventResponse>>builder()
                .result(adminService.getEventPending())
                .build();
    }

    @Operation(summary = "Lấy danh sách sự kiện")
    @GetMapping("/event")
    public ApiResponse<?> getByFilter(@RequestParam(value = "offset", defaultValue = "0") int offset,
                                      @RequestParam(value = "size", defaultValue = "10") int size,
                                      @RequestParam(value = "merchantName", required = false) String merchantName,
                                      @RequestParam(value = "categoriesId",required = false) List<Integer> categoriesId,
                                      @RequestParam(value = "time",required = false) String time,
                                      @RequestParam(value = "city", required = false) List<String> city,
                                      @RequestParam(value = "fromTime", required = false, defaultValue = "2020-01-01") String fromTime,
                                      @RequestParam(value = "toTime", required = false, defaultValue = "2999-01-01") String toTime,
                                      @RequestParam(value = "minPrice",required = false, defaultValue = "0.0") Double minPrice,
                                      @RequestParam(value = "maxPrice",required = false, defaultValue = "100000000.0") Double maxPrice,
                                      @RequestParam(value = "status", required = false) String status){
        return adminService.allEvents(offset, size, merchantName, categoriesId, time, city, fromTime, toTime, minPrice, maxPrice, status);
    }

    @Operation(summary = "Lấy sự kiện cụ thể")
    @GetMapping("/event/{id}")
    public ApiResponse<?> getEvent(@PathVariable("id") int id){
        return eventService.getEvent(id);
    }

    @Operation(summary = "Đổi trạng thái event")
    @PostMapping("/eventMgmt/change-status/{id}")
    public ApiResponse<?> changeEventStatus(@PathVariable("id") int eventId,
                                            @RequestBody(required = false) Map<String, String> requestMap){
        try{
            if(requestMap == null){
                adminService.changeEventStatus(eventId, null);
            }
            else{
                adminService.changeEventStatus(eventId, requestMap.get("status"));
            }
            return ApiResponse.builder()
                    .message("Thành công")
                    .build();
        }catch (Exception e){
            throw new AppException(ErrorCode.ITEM_NOT_FOUND);
        }
    }

    @Operation(summary = "Thống kê sự kiện theo merchant")
    @GetMapping("/statistics/{merchantId}")
    public ResponseEntity<Object> getStatistics(@PathVariable("merchantId") int merchantId) throws ParseException {
        return ResponseEntity.ok().body(adminService.getStatistics(merchantId));
    }

    @Operation(summary = "Thống kê chi tiết của 1 sự kiện")
    @GetMapping("/statistics/event/{id}")
    public ResponseEntity<Object> getStatisticByEvent(@PathVariable("id") int id) throws ParseException {
        return ResponseEntity.ok().body(merchantService.getStatisticsByEvent(id));
    }

    @Operation(summary = "Lấy danh sách categories")
    @GetMapping("/allCategories")
    public ResponseEntity<Object> getCategories(){
        return ResponseEntity.ok(adminService.getAllCategories());
    }

    @Operation(summary = "Thêm mới category")
    @PostMapping("/categories/add")
    public ApiResponse<?> addNewCategories(@RequestBody AddCatRequest request){
        try{
            Categories c = new Categories();
            log.info(c.getCategory_name());
            c.setCategory_name(request.getCatName());
            adminService.addNewCategory(c);
            return ApiResponse.builder()
                    .message("Thêm thành công")
                    .build();
        }catch (Exception e){
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    @Operation(summary = "Xóa 1 category")
    @PostMapping("/categories/delete/{id}")
    public ApiResponse<?> deleteCategories(@PathVariable("id") int id){
        try{
            adminService.removeCategory(id);
            return ApiResponse.builder()
                    .message("Xóa thành công")
                    .build();
        }catch (Exception e){
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    @Operation(summary = "Thêm mới payment gateway")
    @PostMapping("/gateway/add")
    public ApiResponse<?> addGateway(@RequestBody NewGWRequest request){
        try{
            PaymentGateway gateway = new PaymentGateway();
            gateway.setGateway_name(request.getGatewayName());
            adminService.addNewPaymentGateway(gateway);
            return ApiResponse.builder()
                    .message("Thêm thành công")
                    .build();
        }catch (Exception e){
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    @Operation(summary = "Đổi trạng thái tài khoản")
    @PostMapping("/account/change-status")
    public ApiResponse<?> changeAccountStatus(@RequestBody DEAccountRequest request){
        try{
            adminService.changeAccountStatus(request.getUsername(), request.getRoleName());
            log.info(request.getUsername() + " " + request.getRoleName());
            return ApiResponse.builder()
                    .message("Thành công")
                    .build();
        }catch (Exception e){
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    @Operation(summary = "Lấy thông tin payment")
    @GetMapping("/payment-history")
    public ApiResponse<?> pHistory(@RequestParam(value = "paymentDate", required = false) String pDate,
                                   @RequestParam(value = "status", required = false) String status,
                                   @RequestParam(value = "uId", required = false) Integer uId){
        try{
            return adminService.getPaymentHistory(pDate, status, uId);
        }catch (Exception e){
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    @Operation(summary = "Payment Details")
    @GetMapping("/payment-history/{id}")
    public ApiResponse<?> pHistoryDetail(@PathVariable("id") String id){
        return adminService.getPaymentHistoryDetail(id);
    }
}
