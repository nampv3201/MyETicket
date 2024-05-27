package com.datn.ticket.controller;

import com.datn.ticket.exception.AppException;
import com.datn.ticket.exception.ErrorCode;
import com.datn.ticket.model.Categories;
import com.datn.ticket.model.Merchants;
import com.datn.ticket.model.PaymentGateway;
import com.datn.ticket.model.dto.request.AddCatRequest;
import com.datn.ticket.model.dto.request.DEAccountRequest;
import com.datn.ticket.model.dto.request.NewGWRequest;
import com.datn.ticket.model.dto.response.AccountResponse;
import com.datn.ticket.model.dto.response.ApiResponse;
import com.datn.ticket.model.dto.response.MerchantsResponse;
import com.datn.ticket.model.mapper.MerchantMapper;
import com.datn.ticket.service.AdminService;
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

@RestController
@RequestMapping("/admin")
@Slf4j
@Tag(name = "Admin Controller")
public class AdminController {
    @Autowired
    AdminService adminService;

    @Autowired
    MerchantService merchantService;

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
    public ResponseEntity<Object> getMerchants(){
        try{
            ArrayList<MerchantsResponse> merchantsResponses = new ArrayList<>();
            for(Merchants m : adminService.getListMerchants()){
                merchantsResponses.add(MerchantMapper.INSTANCE.merchantsDTO(m));
            }
            return ResponseEntity.ok(merchantsResponses);
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @Operation(summary = "Lấy thông tin của 1 merchant")
    @GetMapping("/merchantInfor/{id}")
    public ResponseEntity<Object> getMerchantInfor(@PathVariable("id") int id){
        try{
            Merchants m = adminService.getMerchantInfor(id);
            return ResponseEntity.ok(MerchantMapper.INSTANCE.merchantsDTO(m));
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @Operation(summary = "Lấy danh sách sự kiện")
    @GetMapping("/event")
    public ApiResponse<?> getByFilter(@RequestParam(value = "MerchantId", required = false) Integer MerchantId,
                                              @RequestParam(value = "CategoryId",required = false) List<Integer> CategoryId,
                                              @RequestParam(value = "allTime",required = false) Integer allTime,
                                              @RequestParam(value = "city", required = false) String city){
        log.info("{}", city);
        return adminService.allEvents(MerchantId, CategoryId, allTime, city);
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
    @GetMapping("/categories/delete/{id}")
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
    @GetMapping("/gateway/add")
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

    @Operation(summary = "Vô hiệu hóa event")
    @PostMapping("/eventMgmt/disable/{id}")
    public ApiResponse<?> disableEvent(@PathVariable("id") int id){
        try{
            adminService.disableEvent(id);
            return ApiResponse.builder()
                    .message("Vô hiệu hóa thành công")
                    .build();
        }catch (Exception e){
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    @Operation(summary = "Kích hoạt event")
    @PostMapping("/eventMgmt/enable/{id}")
    public ApiResponse<?> enableEvent(@PathVariable("id") int id){
        try{
            adminService.enableEvent(id);
            return ApiResponse.builder()
                    .message("Kích hoạt thành công")
                    .build();
        }catch (Exception e){
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    @Operation(summary = "Vô hiệu hóa tài khoản")
    @PostMapping("/eventMgmt/disable")
    public ApiResponse<?> disableAccount(@RequestBody DEAccountRequest request){
        try{
            adminService.disaleAccount(request.getAccountId(), request.getRoleId());
            return ApiResponse.builder()
                    .message("Vô hiệu hóa thành công")
                    .build();
        }catch (Exception e){
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    @Operation(summary = "Kích hoạt tài khoản")
    @PostMapping("/eventMgmt/enable")
    public ApiResponse<?> enableAccount(@RequestBody DEAccountRequest request){
        try{
            adminService.enableAccount(request.getAccountId(), request.getRoleId());
            return ApiResponse.builder()
                    .message("Kích hoạt thành công")
                    .build();
        }catch (Exception e){
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }


}
