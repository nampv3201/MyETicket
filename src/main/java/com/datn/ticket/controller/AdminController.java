package com.datn.ticket.controller;

import com.datn.ticket.model.Merchants;
import com.datn.ticket.model.dto.response.MerchantsResponse;
import com.datn.ticket.model.mapper.MerchantMapper;
import com.datn.ticket.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;

@RestController
@RequestMapping("/admin")
@Tag(name = "Admin Controller")
public class AdminController {
    @Autowired
    AdminService adminService;
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
}
