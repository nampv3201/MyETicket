package com.datn.ticket.controller;

import com.datn.ticket.model.Merchants;
import com.datn.ticket.model.dto.request.MURequest;
import com.datn.ticket.model.dto.response.MerchantsResponse;
import com.datn.ticket.model.dto.response.ApiResponse;
import com.datn.ticket.model.mapper.MerchantMapper;
import com.datn.ticket.service.MerchantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;

@RestController
@RequestMapping("/merchant")
@Tag(name = "Merchant Controller")
@Slf4j
public class MerchantController {
    private final MerchantService merchantService;

    @Autowired
    public MerchantController(MerchantService merchantService) {
        this.merchantService = merchantService;
    }

    @Operation(summary = "Lấy thông tin của merchant đang đăng nhập")
    @GetMapping("/myInfor")
    public ApiResponse<MerchantsResponse> getMyInfor(){
        return ApiResponse.<MerchantsResponse>builder()
                .result(MerchantMapper.INSTANCE.merchantsDTO(merchantService.myInfor()))
                .build();
    }

    @Operation(summary = "Cập nhật thông tin cá nhân của merchant")
    @PostMapping("/infor/update")
    public ApiResponse<?> updateMerchantInfor(@RequestBody MURequest muRequest){
        try{
            Merchants m = merchantService.myInfor();
            m.setName(muRequest.getName());
            m.setAddress(muRequest.getAddress());
            m.setDescription(muRequest.getDescription());
            m.setPhone(muRequest.getPhone());
            merchantService.updateMerchant(m);
            return ApiResponse.builder()
                    .result("abc")
                    .build();
        }catch (Exception e){
//            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
            return ApiResponse.<MerchantsResponse>builder().message(e.getMessage()).build();
        }
    }

    @Operation(summary = "Thống kê sự kiện theo merchant")
    @GetMapping("/statistics")
    public ResponseEntity<Object> getStatistics() throws ParseException {
        return ResponseEntity.ok().body(merchantService.getStatistics());
    }

    @Operation(summary = "Thống kê chi tiết của 1 sự kiện")
    @GetMapping("/statistics/event/{id}")
    public ResponseEntity<Object> getStatisticByEvent(@PathVariable("id") int id) throws ParseException {
        return ResponseEntity.ok().body(merchantService.getStatisticsByEvent(id));
    }
}
