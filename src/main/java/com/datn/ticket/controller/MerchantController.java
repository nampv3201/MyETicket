package com.datn.ticket.controller;

import com.datn.ticket.dto.request.*;
import com.datn.ticket.exception.AppException;
import com.datn.ticket.exception.ErrorCode;
import com.datn.ticket.model.Categories;
import com.datn.ticket.model.CreateTickets;
import com.datn.ticket.model.Events;
import com.datn.ticket.model.Merchants;
import com.datn.ticket.dto.EventDTO;
import com.datn.ticket.dto.EventFirstUpdate;
import com.datn.ticket.dto.EventSecondUpdate;
import com.datn.ticket.dto.response.CreateTicketsResponse;
import com.datn.ticket.dto.response.MerchantsResponse;
import com.datn.ticket.dto.response.ApiResponse;
import com.datn.ticket.model.mapper.CreateTicketMapper;
import com.datn.ticket.model.mapper.EFUMapper;
import com.datn.ticket.model.mapper.ESUMapper;
import com.datn.ticket.model.mapper.MerchantMapper;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@RestController
@RequestMapping("/merchant")
@Tag(name = "Merchant Controller")
@Slf4j
public class MerchantController {
    private final MerchantService merchantService;
    @Autowired
    private EventService eventService;
    @Autowired
    private HttpServletRequest request;

    @Autowired
    public MerchantController(MerchantService merchantService) {
        this.merchantService = merchantService;
    }

    @Operation(summary = "Lấy thông tin của merchant đang đăng nhập")
    @GetMapping("/profile")
    public ApiResponse<MerchantsResponse> getMyInfor() {
        return ApiResponse.<MerchantsResponse>builder()
                .result(MerchantMapper.INSTANCE.merchantsDTO(merchantService.myInfor()))
                .build();
    }

    @Operation(summary = "Cập nhật thông tin cá nhân của merchant")
    @PostMapping("/profile/update")
    public ApiResponse<?> updateMerchantInfor(@RequestBody MURequest muRequest) {
        Merchants m = merchantService.myInfor();
        m.setName(muRequest.getName());
        m.setAddress(muRequest.getAddress());
        m.setDescription(muRequest.getDescription());
        m.setPhone(muRequest.getPhone());
        merchantService.updateMerchant(m);
        return ApiResponse.builder()
                .result("Cập nhật thành công")
                .build();
    }

    @Operation(summary = "Tạo mới event - Bước 1")
    @PostMapping("/add-event")
    public ApiResponse addEvent(@RequestBody EAFRequest eafRequest) throws ParseException, IOException {
        HttpSession session = request.getSession(true);
        session.setMaxInactiveInterval(30 * 60);
        Events newEvent = new Events();

//        try{
        newEvent.setName(eafRequest.getEventName());
        newEvent.setDescription(eafRequest.getEventDescription());
        newEvent.setCity(eafRequest.getEventCity());
        newEvent.setLocation(eafRequest.getEventLocation());
        newEvent.setMax_limit(eafRequest.getEventLimit());
        newEvent.setStatus("pending");
        newEvent.setDeleted(0);

        session.setAttribute("tempEvent", newEvent);
        session.setAttribute("tempCategories", eafRequest.getCategories());
        return ApiResponse.builder().message("Tiếp tục").build();
//        }catch(Exception e){
//            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
//        }

    }

    @Operation(summary = "Upload Image")
    @PostMapping("/eventImg")
    public ApiResponse uploadImage(@RequestParam("eventBanner") MultipartFile file) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            throw new AppException(ErrorCode.SESSION_EXPIRED);
        }
        try {
            Events tEvent = (Events) session.getAttribute("tempEvent");
            tEvent.setBanner(Base64.getEncoder().encodeToString(file.getBytes()));
            return ApiResponse.builder().message("Upload thành công").result("Upload thành công").build();
        } catch (Exception e) {
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    @Operation(summary = "Tạo mới event - Bước 2")
    @PostMapping("/add-event-ticket")
    public ApiResponse addEventTicket(@RequestBody EAUSRequest eausRequest) throws ParseException {
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

//        try{
        merchantService.addEvent(tEvent, createTickets, eventService.getCategories(tempC));
        session.removeAttribute("tempEvent");
        session.removeAttribute("tempCategories");
        session.invalidate();
        return ApiResponse.builder().message("Sự kiện đã được thêm, vui lòng chờ phê duyệt").build();
//        }catch (Exception e){
//            session.invalidate();
//            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
//        }

    }


    @Operation(summary = "Lấy thông tin tổng quan của event - Cập nhật")
    @GetMapping("/update/first-step/{id}")
    public ApiResponse<EventFirstUpdate> getEventUpdate(@PathVariable("id") int eventId) {
        HttpSession session = request.getSession(true);
        session.setMaxInactiveInterval(30 * 60);
        session.setAttribute("tempTicketUpdate", new ArrayList<CreateTickets>());
        session.setAttribute("tempCategoriesAdd", new ArrayList<Categories>());
        session.setAttribute("tempCategoriesRemove", new ArrayList<Categories>());

        Events event = merchantService.getEventUpdate(eventId);
        List<Categories> categories = eventService.getCatByEvent(eventId);
        return ApiResponse.<EventFirstUpdate>builder()
                .result(EFUMapper.cast(event, categories))
                .build();
    }

    @Operation(summary = "Xóa category cho event")
    @PostMapping("/delete/category/{id}")
    public ApiResponse deleteCategory(@PathVariable("id") int id) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            throw new AppException(ErrorCode.SESSION_EXPIRED);
        }

        try {
            ArrayList<Categories> createCategories = (ArrayList<Categories>) session.getAttribute("tempCategoriesRemove");
            Categories c = eventService.getSingleCategory(id);
            for (Categories cat : createCategories) {
                if (cat.getId() == c.getId()) {
                    createCategories.remove(cat);
                    break;
                }
            }
            createCategories.add(c);


            return ApiResponse.builder().message("Deleted").build();
        } catch (Exception e) {
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    @Operation(summary = "Thêm category cho event")
    @PostMapping("/add/category/{id}")
    public ApiResponse addCategory(@PathVariable("id") int id) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            throw new AppException(ErrorCode.SESSION_EXPIRED);
        }

        try {
            ArrayList<Categories> createCategories = (ArrayList<Categories>) session.getAttribute("tempCategoriesAdd");
            Categories c = eventService.getSingleCategory(id);
            for (Categories cat : createCategories) {
                if (cat.getId() == c.getId()) {
                    createCategories.remove(cat);
                    break;
                }
            }
            createCategories.add(c);
            return ApiResponse.builder().message("Added").build();
        } catch (Exception e) {
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    @Operation(summary = "Cập nhật event - Bước 1")
    @PostMapping("/update/first-step/{id}")
    public ApiResponse update(@PathVariable("id") int eventId, @RequestBody EUFRequest eufRequest) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            throw new AppException(ErrorCode.SESSION_EXPIRED);
        }

        try {
            Events event = merchantService.getEventUpdate(eventId);

            event.setName(eufRequest.getEventName());
            event.setDescription(eufRequest.getEventDescription());
            event.setCity(eufRequest.getEventCity());
            event.setLocation(eufRequest.getEventLocation());
            event.setBanner(eufRequest.getEventBanner());
            event.setMax_limit(eufRequest.getEventLimit());

            session.setAttribute("tempEvent", event);
            return ApiResponse.builder().message("true").build();
        } catch (Exception ex) {
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    @Operation(summary = "Lấy thông tin về thơi gian và vé - Cập nhật")
    @GetMapping("/update/second-step/{id}")
    public ApiResponse<EventSecondUpdate> getUpdateTicket(@PathVariable("id") int eventId) {
        Events event = merchantService.getEventUpdate(eventId);
        List<CreateTicketsResponse> tickets = new ArrayList<>();
        for (CreateTickets c : eventService.getTicketTypeByEvent(eventId)) {
            tickets.add(CreateTicketMapper.createTicketsDTO(c));
        }
        return ApiResponse.<EventSecondUpdate>builder()
                .result(ESUMapper.cast(event, tickets)).build();
    }

    @Operation(summary = "Cập nhật thông tin loại vé")
    @PostMapping("/update/ticket/{id}")
    public ApiResponse updateTicketTemp(@PathVariable("id") int id, @RequestBody TicketTypeRequest tRequest) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            throw new AppException(ErrorCode.SESSION_EXPIRED);
        }

        try {
            ArrayList<CreateTickets> createTickets = (ArrayList<CreateTickets>) session.getAttribute("tempTicketUpdate");
            CreateTickets c = eventService.getTicketTypeUpdate(id);
            int oldQuantity = c.getCount();
            c.setCount(tRequest.getQuantity());
            c.setAvailable(c.getAvailable() + oldQuantity - tRequest.getQuantity());
            c.setType_name(tRequest.getTypeName());
            c.setPrice(tRequest.getPrice());

            for (CreateTickets tickets : createTickets) {
                if (tickets.getId() == c.getId()) {
                    createTickets.remove(tickets);
                    break;
                }
            }

            createTickets.add(c);

            return ApiResponse.builder().message("true").build();
        } catch (Exception e) {
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
        if (eausRequest.getTicketTypeRequests() != null) {
            for (TicketTypeRequest typeRequest : eausRequest.getTicketTypeRequests()) {
                CreateTickets tickets = new CreateTickets();
                tickets.setType_name(typeRequest.getTypeName());
                tickets.setPrice(typeRequest.getPrice());
                tickets.setCount(typeRequest.getQuantity());
                tickets.setAvailable(typeRequest.getQuantity());
                addTickets.add(tickets);
            }
        }

        List<CreateTickets> updateTicket = (List<CreateTickets>) session.getAttribute("tempTicketUpdate");
        List<Categories> addCat = (List<Categories>) session.getAttribute("tempCategoriesAdd");
        List<Categories> removeCat = (List<Categories>) session.getAttribute("tempCategoriesRemove");

//        try {
            String status = merchantService.UpdateEvent(e, updateTicket, addTickets, addCat, removeCat).getMessage();
            session.invalidate();
            return ApiResponse.builder().result(status).build();
//        } catch (Exception ex) {
//            return ApiResponse.builder().message(ex.getMessage()).build();
//        }

    }

    @Operation(summary = "Danh sách event của merchant")
    @GetMapping("/myEvent")
    public ApiResponse<?> getMyEvent(@RequestParam(value = "status", required = false) Integer status,
                                     @RequestParam(value = "categoryId", required = false) List<Integer> CategoryId,
                                     @RequestParam(value = "time", required = false) String time,
                                     @RequestParam(value = "city", required = false) String city) throws ParseException {
        return merchantService.myEvents(status, CategoryId, time, city);
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

    @Operation(summary = "Chỉnh sửa trạng thái sự kiện")
    @PostMapping("/event-status/{id}")
    public ApiResponse<?> updateStatus(@PathVariable("id") int eventId) {
        return ApiResponse.builder()
                .result(merchantService.deEvents(eventId))
                .build();
    }

    @Operation(summary = "Xem lịch sử khách hàng đặt mua vé")
    @GetMapping("/event/history/{id}")
    public ApiResponse<?> getEventHistory(@PathVariable("id") int eventId) {
        return merchantService.eventBookingHistory(eventId);
    }

    @Operation(summary = "Thu hồi vé")
    @PostMapping("/ticket/revoke")
    public ApiResponse<?> revokeTicket(@RequestParam("qrCode") String qrCode) {
        if(merchantService.revockTicket(qrCode) != 0) {
            return ApiResponse.builder().message("true").build();
        }
        return ApiResponse.builder().message("false").build();
    }
}
