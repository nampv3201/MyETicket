package com.datn.ticket.controller;

import com.datn.ticket.dto.EventDTO;
import com.datn.ticket.dto.EventHome;
import com.datn.ticket.dto.response.ApiResponse;
import com.datn.ticket.dto.response.EventHomeResponse;
import com.datn.ticket.dto.response.TicketTypeResponse;
import com.datn.ticket.model.mapper.EventMapperNew;
import com.datn.ticket.service.EventService;
import com.datn.ticket.service.MerchantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
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

    @Operation(summary = "Phân trang")
    @GetMapping("/event")
    public ApiResponse<EventHomeResponse> getEventPaging(@RequestParam(value = "offset", defaultValue = "0") int offset,
                                                         @RequestParam(value = "size", defaultValue = "10") int size,
                                                         @RequestParam(value = "merchantId", required = false) Integer MerchantId,
                                                         @RequestParam(value = "categoryId",required = false) List<Integer> CategoryId,
                                                         @RequestParam(value = "time",required = false) String time,
                                                         @RequestParam(value = "city", required = false) List<String> city,
                                                         @RequestParam(value = "fromTime", required = false, defaultValue = "2020-01-01") String fromTime,
                                                         @RequestParam(value = "toTime", required = false, defaultValue = "2999-01-01") String toTime,
                                                         @RequestParam(value = "minPrice",required = false, defaultValue = "0.0") Double minPrice,
                                                         @RequestParam(value = "maxPrice",required = false, defaultValue = "100000000.0") Double maxPrice){
        return ApiResponse.<EventHomeResponse>builder()
                .result(eventService.getEventByFilterWithPage(offset, size, MerchantId, CategoryId, time, city, fromTime, toTime, minPrice, maxPrice))
                .build();
    }
    @Operation(summary = "Lấy theo danh mục")
    @GetMapping("search/{categoryName}")
    public ApiResponse<?> getEventByCategory(@PathVariable("categoryName") String categoryName){
        return eventService.getEventByCategory(categoryName);
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

    @Operation(summary = "Lấy event ngẫu nhiên theo categories")
    @GetMapping("/events-categories")
    public ApiResponse<?> getEventsByCategoriesLimit(@RequestParam(value = "categoryId") int categoryId,
                                                     @RequestParam(value = "limit") int limit){
        return ApiResponse.<List<EventHome>>builder().result(eventService.getEventByCatIdWithLimit(categoryId, limit)).build();
    }

}
