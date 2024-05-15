package com.datn.ticket.controller;

import com.datn.ticket.model.Categories;
import com.datn.ticket.model.CreateTickets;
import com.datn.ticket.model.Events;
import com.datn.ticket.model.Merchants;
import com.datn.ticket.model.dto.CreateTicketsDTO;
import com.datn.ticket.model.dto.EventSecondUpdate;
import com.datn.ticket.model.dto.request.EUSRequest;
import com.datn.ticket.model.dto.request.TicketTypeRequest;
import com.datn.ticket.model.dto.response.ApiResponse;
import com.datn.ticket.model.dto.response.TicketTypeResponse;
import com.datn.ticket.model.mapper.CreateTicketMapper;
import com.datn.ticket.model.mapper.EFUMapper;
import com.datn.ticket.model.mapper.ESUMapper;
import com.datn.ticket.model.mapper.EventMapperNew;
import com.datn.ticket.service.EventService;
import com.datn.ticket.service.MerchantService;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
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
import java.util.Map;

@RestController
@RequestMapping("/event")
@Tag(name = "Event Controller")
public class EventController {

    private final EventService eventService;
    private final MerchantService merchantService;
    private HttpSession session;
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
    public ResponseEntity<Object> getEvent(@PathVariable("id") int id){
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
    public void addEvent(@RequestBody String jsonMap) throws ParseException{
        if(session == null){
            session = request.getSession();
        }
        Events newEvent = new Events();
        List<Integer> cList = new ArrayList<>();

        Gson gson = new Gson();
        JsonElement root = gson.fromJson(jsonMap, JsonElement.class);
        if(root.isJsonObject()){
            JsonObject jsonObject = root.getAsJsonObject();

            // Event information
            newEvent.setName(jsonObject.get("eventName").getAsString());
            newEvent.setDescription(jsonObject.get("eventDescription").getAsString());
            newEvent.setLocation(jsonObject.get("eventLocation").getAsString());
            newEvent.setBanner(jsonObject.get("eventBanner").getAsString());
            newEvent.setMax_limit(Integer.parseInt(jsonObject.get("eventMaxLimit").getAsString()));

            // Categories list
            JsonArray categoriesArray = jsonObject.getAsJsonArray("categories");

            // Xử lý mảng "categories" nếu cần
            for (JsonElement element : categoriesArray) {
                cList.add(element.getAsInt());
            }
        }

        session.setAttribute("tempEvent", newEvent);
        session.setAttribute("tempCategories", cList);
    }

    @Operation(summary = "Tạo mới event - Bước 2")
    @PostMapping("/add-event-ticket")
    public void addEventTicket(@RequestBody String jsonMap) throws ParseException{
        Merchants m = new Merchants();
        Events tEvent = (Events) session.getAttribute("tempEvent");
        List<Integer> tempC = (List<Integer>) session.getAttribute("tempCategories");


        List<CreateTickets> createTickets = new ArrayList<>();
        Gson gson = new Gson();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        JsonElement root = gson.fromJson(jsonMap, JsonElement.class);
        if(root.isJsonObject()){
            JsonObject jsonObject = root.getAsJsonObject();

            // Event information
            m = merchantService.getMerchantInfor(jsonObject.get("merchantId").getAsInt());
            tEvent.setStart_time(format.parse(jsonObject.get("start_time").getAsString()));
            tEvent.setEnd_time(format.parse(jsonObject.get("end_time").getAsString()));
            tEvent.setStart_booking(format.parse(jsonObject.get("start_booking").getAsString()));
            tEvent.setEnd_booking(format.parse(jsonObject.get("end_booking").getAsString()));
            tEvent.setMerchants(m);

            // Create ticket information
            JsonArray ticketTypeArray = jsonObject.getAsJsonArray("createTickets");

            // Xử lý mảng "attributes" nếu cần
            for (JsonElement element : ticketTypeArray) {
                CreateTickets tickets = new CreateTickets();
                JsonObject ticketObject = element.getAsJsonObject();
                String typeName = ticketObject.get("type_name").getAsString();
                double price = Double.parseDouble(ticketObject.get("cost").getAsString());
                int quantity = ticketObject.get("quantity").getAsInt();

                tickets.setType_name(typeName);
                tickets.setPrice(price);
                tickets.setCount(quantity);
                tickets.setAvailable(quantity);
                tickets.setMerchants(m);

                createTickets.add(tickets);
            }
        }

        eventService.addEvent(tEvent, createTickets, eventService.getCategories(tempC));
        session.removeAttribute("tempEvent");
        session.removeAttribute("tempCategories");
        session.invalidate();
    }


    @Operation(summary = "Lấy thông tin tổng quan của event - Cập nhật")
    @GetMapping("/update/first-step/{id}")
    public ResponseEntity<Object> getEventUpdate(@PathVariable("id") int eventId){
        if(session == null){
            session = request.getSession();
            session.setAttribute("tempTicketAdd", new ArrayList<CreateTickets>());
            session.setAttribute("tempTicketUpdate", new ArrayList<CreateTickets>());
            session.setAttribute("tempCategoriesAdd", new ArrayList<Categories>());
            session.setAttribute("tempCategoriesRemove", new ArrayList<Categories>());
        }

        Events event = eventService.getEventUpdate(eventId);
        List<Categories> categories = eventService.getCatByEvent(eventId);
        return ResponseEntity.ok().body(EFUMapper.cast(event, categories));
    }

    @Operation(summary = "Xóa category cho event")
    @PostMapping("/delete/category/{id}")
    public void deleteCategory(@PathVariable("id") int id){
        ArrayList<Categories> createCategories = (ArrayList<Categories>) session.getAttribute("tempCategoriesRemove");
        Categories c = eventService.getSingleCategory(id);
        for(Categories cat : createCategories){
            if(cat.getId() == c.getId()){
                createCategories.remove(cat);
                break;
            }
        }
        createCategories.add(c);
    }

    @Operation(summary = "Thêm category cho event")
    @PostMapping("/add/category/{id}")
    public void addCategory(@PathVariable("id") int id){
        ArrayList<Categories> createCategories = (ArrayList<Categories>) session.getAttribute("tempCategoriesAdd");
        Categories c = eventService.getSingleCategory(id);
        for(Categories cat : createCategories){
            if(cat.getId() == c.getId()){
                createCategories.remove(cat);
                break;
            }
        }
        createCategories.add(c);
    }

    @Operation(summary = "Cập nhật event - Bước 1")
    @PostMapping("/update/first-step/{id}")
    public void update(@PathVariable("id") int eventId, @RequestBody String jsonMap){
        Events event = eventService.getEventUpdate(eventId);
        Gson gson = new Gson();
        JsonElement root = gson.fromJson(jsonMap, JsonElement.class);
        if(root.isJsonObject()){
            JsonObject jsonObject = root.getAsJsonObject();
            // Event information
            event.setName(jsonObject.get("eventName").getAsString());
            event.setDescription(jsonObject.get("eventDescription").getAsString());
            event.setLocation(jsonObject.get("eventLocation").getAsString());
            event.setBanner(jsonObject.get("eventBanner").getAsString());
            event.setMax_limit(Integer.parseInt(jsonObject.get("eventLimit").getAsString()));
        }

        session.setAttribute("tempEvent", event);
    }

    @Operation(summary = "Lấy thông tin về thơi gian và vé - Cập nhật")
    @GetMapping("/update/second-step/{id}")
    public ResponseEntity<EventSecondUpdate> getUpdateTicket(@PathVariable("id") int eventId){
        Events event = eventService.getEventUpdate(eventId);
        List<CreateTicketsDTO> tickets = new ArrayList<>();
        for(CreateTickets c : eventService.getTicketTypeByEvent(eventId)){
            tickets.add(CreateTicketMapper.createTicketsDTO(c));
        }
        return ResponseEntity.ok(ESUMapper.cast(event, tickets));
    }

    @Operation(summary = "Cập nhật thông tin loại vé")
    @PostMapping("/update/ticket/{id}")
    public void updateTicketTemp(@PathVariable("id") int id, @RequestBody Map<String, Object> jsonMap){
        ArrayList<CreateTickets> createTickets = (ArrayList<CreateTickets>) session.getAttribute("tempTicketUpdate");
        CreateTickets c = eventService.getTicketTypeUpdate(id);
        c.setCount((Integer) jsonMap.get("quantity"));
        c.setType_name(jsonMap.get("typeName").toString());
        c.setPrice((Double) jsonMap.get("price"));

        for(CreateTickets tickets : createTickets){
            if(tickets.getId() == c.getId()){
                createTickets.remove(tickets);
                break;
            }
        }

        createTickets.add(c);
    }

    @Operation(summary = "Cập nhật event - Bước 2")
    @PostMapping("/update/second-step/{id}")
    public String updateTicket(@PathVariable("id") int eventId, @RequestBody EUSRequest request) throws ParseException {
        Events e = (Events) session.getAttribute("tempEvent");
        List<CreateTickets> addTickets = new ArrayList<>();

//        Gson gson = new Gson();
//        JsonElement root = gson.fromJson(jsonMap, JsonElement.class);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

//        if(root.isJsonObject()){
//            JsonObject jsonObject = root.getAsJsonObject();
//            e.setStart_time(format.parse(jsonObject.get("start_time").getAsString()));
//            e.setEnd_time(format.parse(jsonObject.get("end_time").getAsString()));
//            e.setStart_booking(format.parse(jsonObject.get("start_booking").getAsString()));
//            e.setEnd_booking(format.parse(jsonObject.get("end_booking").getAsString()));
//
//            JsonArray ticketTypeArray = jsonObject.getAsJsonArray("addTicketType");
//
//            // Xử lý mảng "attributes" nếu cần
//            for (JsonElement element : ticketTypeArray) {
//                CreateTickets tickets = new CreateTickets();
//                JsonObject ticketObject = element.getAsJsonObject();
//
//                tickets.setType_name(ticketObject.get("typeName").getAsString());
//                tickets.setPrice(Double.parseDouble(ticketObject.get("price").getAsString()));
//                tickets.setCount(ticketObject.get("quantity").getAsInt());
//                tickets.setAvailable(ticketObject.get("quantity").getAsInt());
//
//                addTickets.add(tickets);
//            }
//        }
        e.setStart_time(format.parse(request.getStart_time()));
        e.setEnd_time(format.parse(request.getEnd_time()));
        e.setStart_booking(format.parse(request.getStart_booking()));
        e.setEnd_booking(format.parse(request.getEnd_booking()));
        for(TicketTypeRequest typeRequest : request.getTicketTypeRequests()) {
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

        String status = eventService.UpdateEvent(e, updateTicket, addTickets, addCat, removeCat).getBody().toString();
        session.invalidate();
        return status;
    }

    @Operation(summary = "Lấy danh sách vé của 1 event")
    @GetMapping("/ticketType/{eventId}")
    ApiResponse<List<TicketTypeResponse>> getTicketType(@PathVariable("eventId") Integer eventId){
        return ApiResponse.<List<TicketTypeResponse>>builder()
                .result(EventMapperNew.INSTANCE.toTicketTypeResponseList(eventService.getTicketTypeByEvent(eventId)))
                .build();
    }

}
