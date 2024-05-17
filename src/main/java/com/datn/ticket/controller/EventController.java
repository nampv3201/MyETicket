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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/event")
@Tag(name = "Event Controller")
public class EventController {

    private final EventService eventService;
    private final MerchantService merchantService;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    public EventController(EventService eventService, MerchantService merchantService, HttpSession session) {
        this.eventService = eventService;
        this.merchantService = merchantService;
    }

    @Operation(summary = "Tìm kiếm event")
    @GetMapping("/search")
    public ResponseEntity<Object> findEventByName(@RequestParam("name") String name){
        return eventService.findEventByName(name);
    }

    @Operation(summary = "Lấy thông tin chi tiết của event")
    @GetMapping("/get/{id}")
    public ApiResponse<EventDTO> getEvent(@PathVariable("id") int id){
        return eventService.getEvent(id);
    }

    @Operation(summary = "Lọc Event")
    @GetMapping("/get")
    public ResponseEntity<Object> getByFilter(@RequestParam(value = "MerchantId", required = false) Integer MerchantId,
                                              @RequestParam(value = "CategoryId",required = false) List<Integer> CategoryId,
                                              @RequestParam(value = "allTime",required = false) Integer allTime){
        return eventService.getEventByFilter(MerchantId, CategoryId, allTime);
    }

    @Operation(summary = "Lấy danh sách categories")
    @GetMapping("/categories")
    public ResponseEntity<Object> getCategories(){
        return ResponseEntity.ok(eventService.getAllCategories());
    }

    @Operation(summary = "Tạo mới event - Bước 1")
    @PostMapping("/add-event")
    public ApiResponse addEvent(@RequestBody EAFRequest eafRequest) throws ParseException{
        HttpSession session = request.getSession(true);
        Events newEvent = new Events();

        try{
            newEvent.setName(eafRequest.getEventName());
            newEvent.setDescription(eafRequest.getEventDescription());
            newEvent.setLocation(eafRequest.getEventLocation());
            newEvent.setBanner(eafRequest.getEventBanner());
            newEvent.setMax_limit(eafRequest.getEventLimit());
            newEvent.setStatus(1);


            session.setAttribute("tempEvent", newEvent);
            session.setAttribute("tempCategories", eafRequest.getCategories());
            return ApiResponse.builder().message("Tiếp tục").build();
        }catch(Exception e){
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }

    }

    @Operation(summary = "Tạo mới event - Bước 2")
    @PostMapping("/add-event-ticket")
    public ApiResponse addEventTicket(@RequestBody EAUSRequest eausRequest) throws ParseException{
        HttpSession session = request.getSession(false);
        if (session == null) {
            throw new AppException(ErrorCode.SESSION_EXPIRED);
        }

        Merchants m = new Merchants();
        Events tEvent = (Events) session.getAttribute("tempEvent");
        List<Integer> tempC = (List<Integer>) session.getAttribute("tempCategories");


        List<CreateTickets> createTickets = new ArrayList<>();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        // Event information
        m = merchantService.myInfor();
        tEvent.setStart_time(format.parse(eausRequest.getStart_time()));
        tEvent.setEnd_time(format.parse(eausRequest.getEnd_time()));
        tEvent.setStart_booking(format.parse(eausRequest.getStart_booking()));
        tEvent.setEnd_booking(format.parse(eausRequest.getEnd_booking()));
        tEvent.setMerchants(m);

        // Xử lý mảng "attributes" nếu cần
        for (TicketTypeRequest tRequest : eausRequest.getTicketTypeRequests()) {
            CreateTickets tickets = new CreateTickets();

            tickets.setType_name(tRequest.getTypeName());
            tickets.setPrice(tRequest.getPrice());
            tickets.setCount(tRequest.getQuantity());
            tickets.setAvailable(tRequest.getQuantity());
            tickets.setMerchants(m);

            createTickets.add(tickets);
        }

        try{
            eventService.addEvent(tEvent, createTickets, eventService.getCategories(tempC));
            session.removeAttribute("tempEvent");
            session.removeAttribute("tempCategories");
            session.invalidate();
            return ApiResponse.builder().message("Tạo mới thành công").build();
        }catch (Exception e){
            session.invalidate();
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }

    }


    @Operation(summary = "Lấy thông tin tổng quan của event - Cập nhật")
    @GetMapping("/update/first-step/{id}")
    public ApiResponse<EventFirstUpdate> getEventUpdate(@PathVariable("id") int eventId){
        HttpSession session = request.getSession(true);
        session.setAttribute("tempTicketAdd", new ArrayList<CreateTickets>());
        session.setAttribute("tempTicketUpdate", new ArrayList<CreateTickets>());
        session.setAttribute("tempCategoriesAdd", new ArrayList<Categories>());
        session.setAttribute("tempCategoriesRemove", new ArrayList<Categories>());

        Events event = eventService.getEventUpdate(eventId);
        List<Categories> categories = eventService.getCatByEvent(eventId);
        return ApiResponse.<EventFirstUpdate>builder()
                .result(EFUMapper.cast(event, categories))
                .build();
    }

    @Operation(summary = "Xóa category cho event")
    @PostMapping("/delete/category/{id}")
    public ApiResponse deleteCategory(@PathVariable("id") int id){
        HttpSession session = request.getSession(false);
        if (session == null) {
            throw new AppException(ErrorCode.SESSION_EXPIRED);
        }

        try{
            ArrayList<Categories> createCategories = (ArrayList<Categories>) session.getAttribute("tempCategoriesRemove");
            Categories c = eventService.getSingleCategory(id);
            for(Categories cat : createCategories){
                if(cat.getId() == c.getId()){
                    createCategories.remove(cat);
                    break;
                }
            }
            createCategories.add(c);
            return ApiResponse.builder().message("Deleted").build();
        }catch (Exception e){
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    @Operation(summary = "Thêm category cho event")
    @PostMapping("/add/category/{id}")
    public ApiResponse addCategory(@PathVariable("id") int id){
        HttpSession session = request.getSession(false);
        if (session == null) {
            throw new AppException(ErrorCode.SESSION_EXPIRED);
        }

        try{
            ArrayList<Categories> createCategories = (ArrayList<Categories>) session.getAttribute("tempCategoriesAdd");
            Categories c = eventService.getSingleCategory(id);
            for(Categories cat : createCategories){
                if(cat.getId() == c.getId()){
                    createCategories.remove(cat);
                    break;
                }
            }
            createCategories.add(c);
            return ApiResponse.builder().message("Added").build();
        }catch(Exception e){
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    @Operation(summary = "Cập nhật event - Bước 1")
    @PostMapping("/update/first-step/{id}")
    public ApiResponse update(@PathVariable("id") int eventId, @RequestBody EUFRequest eufRequest){
        HttpSession session = request.getSession(false);
        if (session == null) {
            throw new AppException(ErrorCode.SESSION_EXPIRED);
        }

        try {
            Events event = eventService.getEventUpdate(eventId);

            event.setName(eufRequest.getEventName());
            event.setDescription(eufRequest.getEventDescription());
            event.setLocation(eufRequest.getEventLocation());
            event.setBanner(eufRequest.getEventBanner());
            event.setMax_limit(eufRequest.getEventLimit());

            session.setAttribute("tempEvent", event);
            return ApiResponse.builder().message("true").build();
        }catch(Exception ex){
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    @Operation(summary = "Lấy thông tin về thơi gian và vé - Cập nhật")
    @GetMapping("/update/second-step/{id}")
    public ApiResponse<EventSecondUpdate> getUpdateTicket(@PathVariable("id") int eventId){
        Events event = eventService.getEventUpdate(eventId);
        List<CreateTicketsResponse> tickets = new ArrayList<>();
        for(CreateTickets c : eventService.getTicketTypeByEvent(eventId)){
            tickets.add(CreateTicketMapper.createTicketsDTO(c));
        }
        return ApiResponse.<EventSecondUpdate>builder()
                .result(ESUMapper.cast(event, tickets)).build();
    }

    @Operation(summary = "Cập nhật thông tin loại vé")
    @PostMapping("/update/ticket/{id}")
    public ApiResponse updateTicketTemp(@PathVariable("id") int id, @RequestBody TicketTypeRequest tRequest){
        HttpSession session = request.getSession(false);
        if (session == null) {
            throw new AppException(ErrorCode.SESSION_EXPIRED);
        }

        try{
            ArrayList<CreateTickets> createTickets = (ArrayList<CreateTickets>) session.getAttribute("tempTicketUpdate");
            CreateTickets c = eventService.getTicketTypeUpdate(id);
            int oldQuantity = c.getCount();
            c.setCount(tRequest.getQuantity());
            c.setAvailable(c.getAvailable() + oldQuantity - tRequest.getQuantity());
            c.setType_name(tRequest.getTypeName());
            c.setPrice(c.getPrice());

            for(CreateTickets tickets : createTickets){
                if(tickets.getId() == c.getId()){
                    createTickets.remove(tickets);
                    break;
                }
            }

            createTickets.add(c);
            return ApiResponse.builder().message("true").build();
        }catch (Exception e){
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    @Operation(summary = "Cập nhật event - Bước 2")
    @PostMapping("/update/second-step/{id}")
    public ApiResponse updateTicket(@PathVariable("id") int eventId, @RequestBody EAUSRequest eausRequest) throws ParseException {
        HttpSession session = request.getSession(false);
        if (session == null) {
            throw new AppException(ErrorCode.SESSION_EXPIRED);
        }

        Events e = (Events) session.getAttribute("tempEvent");
        List<CreateTickets> addTickets = new ArrayList<>();

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        e.setStart_time(format.parse(eausRequest.getStart_time()));
        e.setEnd_time(format.parse(eausRequest.getEnd_time()));
        e.setStart_booking(format.parse(eausRequest.getStart_booking()));
        e.setEnd_booking(format.parse(eausRequest.getEnd_booking()));
        for(TicketTypeRequest typeRequest : eausRequest.getTicketTypeRequests()) {
            CreateTickets tickets = new CreateTickets();
            tickets.setType_name(typeRequest.getTypeName());
            tickets.setPrice(typeRequest.getPrice());
            tickets.setCount(typeRequest.getQuantity());
            tickets.setAvailable(typeRequest.getQuantity());
            addTickets.add(tickets);
        }

        List<CreateTickets> updateTicket = (List<CreateTickets>) session.getAttribute("tempTicketUpdate");
        List<Categories> addCat = new ArrayList<>();
        List<Categories> removeCat = new ArrayList<>();

        try{
            String status = eventService.UpdateEvent(e, updateTicket, addTickets, addCat, removeCat).getBody().toString();
            session.invalidate();
            return ApiResponse.builder().message(status).build();
        }catch(Exception ex){
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }

    }

    @Operation(summary = "Lấy danh sách vé của 1 event")
    @GetMapping("/ticketType/{eventId}")
    ApiResponse<List<TicketTypeResponse>> getTicketType(@PathVariable("eventId") Integer eventId){
        return ApiResponse.<List<TicketTypeResponse>>builder()
                .result(EventMapperNew.INSTANCE.toTicketTypeResponseList(eventService.getTicketTypeByEvent(eventId)))
                .build();
    }

}
