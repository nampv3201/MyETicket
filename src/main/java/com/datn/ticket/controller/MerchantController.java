package com.datn.ticket.controller;

import com.datn.ticket.model.Merchants;
import com.datn.ticket.model.Users;
import com.datn.ticket.model.dto.MerchantsDTO;
import com.datn.ticket.model.mapper.MerchantMapper;
import com.datn.ticket.model.mapper.UsersMapper;
import com.datn.ticket.service.MerchantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/merchant")
@Tag(name = "Merchant Controller")
public class MerchantController {
    private final MerchantService merchantService;

    @Autowired
    public MerchantController(MerchantService merchantService) {
        this.merchantService = merchantService;
    }

    @Operation(summary = "Lấy danh sách tất cả merchant trong hệ thống")
    @GetMapping("/infor")
    public ResponseEntity<Object> getMerchants(){
        try{
            ArrayList<MerchantsDTO> merchantsDTOS = new ArrayList<>();
            for(Merchants m : merchantService.getListMerchants()){
                merchantsDTOS.add(MerchantMapper.merchantsDTO(m));
            }
            return ResponseEntity.ok(merchantsDTOS);
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @Operation(summary = "Lấy thông tin của 1 merchant")
    @GetMapping("/infor/{id}")
    public ResponseEntity<Object> getMerchantInfor(@PathVariable("id") int id){
        try{
            Merchants m = merchantService.getMerchantInfor(id);
            return ResponseEntity.ok(MerchantMapper.merchantsDTO(m));
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @Operation(summary = "Cập nhật thông tin cá nhân của merchant")
    @PutMapping("/infor/update/{id}")
    public ResponseEntity<Object> updateMerchantInfor(@PathVariable("id") int id, @RequestBody Map<String, Object> body){
        try{
            merchantService.updateMerchant(id, body);
            return ResponseEntity.ok(getMerchantInfor(id).getBody());
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @Operation(summary = "Thống kê sự kiện theo merchant")
    @GetMapping("/statistics/{id}")
    public ResponseEntity<Object> getStatistics(@PathVariable("id") int id) throws ParseException {
        return ResponseEntity.ok().body(merchantService.getStatistics(id));
    }

    @Operation(summary = "Thống kê chi tiết của 1 sự kiện")
    @GetMapping("/statistics/event/{id}")
    public ResponseEntity<Object> getStatisticByEvent(@PathVariable("id") int id) throws ParseException {
        return ResponseEntity.ok().body(merchantService.getStatisticsByEvent(id));
    }
}
