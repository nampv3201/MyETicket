package com.datn.ticket.controller;

import com.datn.ticket.exception.AppException;
import com.datn.ticket.exception.ErrorCode;
import com.datn.ticket.model.Categories;
import com.datn.ticket.model.CreateTickets;
import com.datn.ticket.model.Events;
import com.datn.ticket.model.Merchants;
import com.datn.ticket.model.dto.EventFirstUpdate;
import com.datn.ticket.model.dto.response.CreateTicketsResponse;
import com.datn.ticket.model.dto.EventDTO;
import com.datn.ticket.model.dto.EventSecondUpdate;
import com.datn.ticket.model.dto.request.EAFRequest;
import com.datn.ticket.model.dto.request.EAUSRequest;
import com.datn.ticket.model.dto.request.EUFRequest;
import com.datn.ticket.model.dto.request.TicketTypeRequest;
import com.datn.ticket.model.dto.response.ApiResponse;
import com.datn.ticket.model.dto.response.TicketTypeResponse;
import com.datn.ticket.model.mapper.CreateTicketMapper;
import com.datn.ticket.model.mapper.EFUMapper;
import com.datn.ticket.model.mapper.ESUMapper;
import com.datn.ticket.model.mapper.EventMapperNew;
import com.datn.ticket.service.EventService;
import com.datn.ticket.service.MerchantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/home")
@Tag(name = "Event Controller")
@Slf4j
public class EventController {

    private final EventService eventService;
    private final MerchantService merchantService;

    @Autowired
    public EventController(EventService eventService, MerchantService merchantService) {
        this.eventService = eventService;
        this.merchantService = merchantService;
    }

    @Operation(summary = "Tìm kiếm event")
    @GetMapping("/search")
    public ResponseEntity<Object> findEventByName(@RequestParam("name") String name){
        return eventService.findEventByName(name);
    }

    @Operation(summary = "Lấy thông tin chi tiết của event")
    @GetMapping("/{id}")
    public ApiResponse<EventDTO> getEvent(@PathVariable("id") int id){
        return eventService.getEvent(id);
    }

    @Operation(summary = "Lọc Event")
    @GetMapping("/event")
    public ResponseEntity<Object> getByFilter(@RequestParam(value = "MerchantId", required = false) Integer MerchantId,
                                              @RequestParam(value = "CategoryId",required = false) List<Integer> CategoryId,
                                              @RequestParam(value = "time",required = false) String time,
                                              @RequestParam(value = "city", required = false) String city){
        return eventService.getEventByFilter(MerchantId, CategoryId, time, city);
    }

    @Operation(summary = "Lấy danh sách vé của 1 event")
    @GetMapping("/booking/{eventId}")
    ApiResponse<List<TicketTypeResponse>> getTicketType(@PathVariable("eventId") Integer eventId){
        return ApiResponse.<List<TicketTypeResponse>>builder()
                .result(EventMapperNew.INSTANCE.toTicketTypeResponseList(eventService.getTicketTypeByEvent(eventId)))
                .build();
    }

    @Operation(summary = "Lấy danh sách categories")
    @GetMapping("/categories")
    public ResponseEntity<Object> getCategories(){
        return ResponseEntity.ok(eventService.getAllCategories());
    }

}
