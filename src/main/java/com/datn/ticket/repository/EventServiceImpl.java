package com.datn.ticket.repository;

import com.datn.ticket.dto.EventHome;
import com.datn.ticket.dto.response.EventHomeResponse;
import com.datn.ticket.exception.AppException;
import com.datn.ticket.exception.ErrorCode;
import com.datn.ticket.model.Categories;
import com.datn.ticket.model.CreateTickets;
import com.datn.ticket.model.Events;
import com.datn.ticket.dto.response.CreateTicketsResponse;
import com.datn.ticket.dto.EventDTO;
import com.datn.ticket.dto.response.ApiResponse;
import com.datn.ticket.model.mapper.CategoriesMapper;
import com.datn.ticket.model.mapper.CreateTicketMapper;
import com.datn.ticket.model.mapper.EventHomeMapper;
import com.datn.ticket.model.mapper.EventMapper;
import com.datn.ticket.service.EventService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Repository
@Slf4j
public class EventServiceImpl implements EventService {

    private final EntityManager manager;

    @Autowired
    public EventServiceImpl(EntityManager manager) {
        this.manager = manager;
    }

    @Override
    public ResponseEntity<Object> findEventByName(String keyWord) {
        Query query = manager.createNativeQuery("Select e.id, e.name, e.banner, e.city, e.location, e.start_booking, min(ct.price) " +
                "from events e " +
                "join createticket ct on ct.Events_id = e.id " +
                "where e.name like :keyWord and e.status = 'available' group by e.id");
        query.setParameter("keyWord", "%" + keyWord + "%");
        List<Object[]> events = query.getResultList();

        return ResponseEntity.ok(EventHomeMapper.eventHomeDTO(events));
    }

    @Override
    public ApiResponse<EventDTO> getEvent(int id) {
        Events e;
        List<String> categoryList;
        List<CreateTicketsResponse> ticketsDTOS = new ArrayList<>();

        TypedQuery<Events> eventQuery = manager.createQuery("Select e from Events e where e.id = :id", Events.class);
        eventQuery.setParameter("id", id);
        e = eventQuery.getSingleResult();

        Query categoryQuery = manager.createQuery("SELECT ec.categories.category_name FROM EventCat ec " +
                "WHERE ec.events = :event");
        categoryQuery.setParameter("event", e);
        try {
            categoryList = categoryQuery.getResultList();
            TypedQuery<CreateTickets> typedQuery = manager.createQuery("Select tt from CreateTickets tt where tt.events = :event", CreateTickets.class);
            typedQuery.setParameter("event", e);
            for (CreateTickets c : typedQuery.getResultList()) {
                ticketsDTOS.add(CreateTicketMapper.createTicketsDTO(c));
            }

            List<EventHome> suggest_events = getSuggestEvents();

            return ApiResponse.<EventDTO>builder()
                    .result(EventMapper.eventDTO(e, categoryList, ticketsDTOS, suggest_events)).build();

        }catch (EmptyResultDataAccessException ex){
            throw AppException.from(ex, ErrorCode.ITEM_NOT_FOUND);
        }catch (Exception ex){
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    @Override
    public EventHomeResponse getEventByFilterWithPage(Integer offset, Integer size, Integer MerchantId, List<Integer> CategoryId, String time,
                                                      List<String> city, String fromTime, String toTime, Double minPrice, Double maxPrice) {
        List<Object[]> events = new ArrayList<>();
        Query getEvent;
        StringBuilder query = new StringBuilder("Select e.id, e.name, e.banner, e.city, e.location, e.start_booking, min(ct.price) " +
                "from events e " +
                "join createticket ct on ct.Events_id = e.id " +
                "where e.status = 'available' ");


        // Add query by filtering
        if(MerchantId != null) {
            query.append("and e.Merchants_id = :MerchantId ");
        }
        if(CategoryId != null) {
            query.append("and e.id in (select ecat.Events_id from events_has_categories ecat where ecat.Categories_id in :CategoryId) ");
        }
        if(time!=null){
            if(time.equals("before")){
                query.append("and e.end_time < now() ");
            }if(time.equals("after")){
                query.append("and e.end_time > now() ");
            }
        }
        if(city != null){
            query.append("and e.city in :city ");
        }

        // Create Query
        query.append(" group by e.id, e.name " +
                "having e.start_booking between :fromTime and :toTime " +
                "and min(ct.price) between :minPrice and :maxPrice " +
                "limit :limit offset :offset ");
        getEvent = manager.createNativeQuery(query.toString())
                .setParameter("fromTime", fromTime)
                .setParameter("toTime", toTime)
                .setParameter("minPrice", minPrice)
                .setParameter("maxPrice", String.valueOf(maxPrice))
                .setParameter("limit", size)
                .setParameter("offset", offset);

        // Add Query Params
        if(MerchantId != null) {
            getEvent.setParameter("MerchantId", MerchantId);
        }
        if(CategoryId != null && !CategoryId.isEmpty()) {
            getEvent.setParameter("CategoryId", CategoryId);
        }
        if(city != null){
            getEvent.setParameter("city", city);
        }
        try {
            events = getEvent.getResultList();
            return EventHomeResponse.builder().count(events.size())
                    .events(EventHomeMapper.eventHomeDTO(events))
                    .build();
        }catch (EmptyResultDataAccessException e){
            throw AppException.from(e, ErrorCode.ITEM_NOT_FOUND);
        }
    }

    @Override
    public List<EventHome> getEventByCatIdWithLimit(int catId, int limit) {
        List<Object[]> events = new ArrayList<>();
        Query getEvent;
        StringBuilder query = new StringBuilder("Select e.id, e.name, e.banner, e.city, e.location, e.start_booking, min(ct.price) " +
                "from events e " +
                "join events_has_categories ecat on e.id = ecat.Events_id " +
                "join categories c on ecat.Categories_id = c.id " +
                "join createticket ct on ct.Events_id = e.id " +
                "where e.status = 'available' and c.id = :cartId ");


        // Create Query
        query.append(" group by e.id, e.name " +
                "ORDER BY RAND() LIMIT :limit");
        getEvent = manager.createNativeQuery(query.toString())
                .setParameter("limit", limit)
                .setParameter("cartId", catId);

        try {
            events = getEvent.getResultList();
            return EventHomeMapper.eventHomeDTO(events);
        }catch (EmptyResultDataAccessException e){
            throw AppException.from(e, ErrorCode.ITEM_NOT_FOUND);
        }
    }

    @Override
    public ApiResponse<?> getEventByCategory(String category) {
        String catName = CategoriesMapper.mapCategory(category);
        Query query = manager.createNativeQuery("Select e.id, e.name, e.banner, e.city, e.location, e.start_booking, min(ct.price) " +
                "from events e " +
                "join createticket ct on ct.Events_id = e.id " +
                "join events_has_categories ecat on ecat.Events_id = e.id " +
                "join categories cat on ecat.Categories_id = cat.id " +
                "where e.status = 'available' and cat.category_name = :catName " +
                "group by e.id, e.name")
                .setParameter("catName", catName);
        try {
            List<Object[]> events = query.getResultList();
            return ApiResponse.<List<EventHome>>builder()
                   .result(EventHomeMapper.eventHomeDTO(events))
                   .build();
        }catch (Exception e) {
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    @Override
    public CreateTickets getTicketType(Integer ticketTypeID) {
        return manager.createQuery("select ct from CreateTickets  ct where ct.id = :ticketTypeID", CreateTickets.class)
                .setParameter("ticketTypeID", ticketTypeID)
                .getSingleResult();
    }


    @Override
    public List<Categories> getCategories(List<Integer> ids) {
        TypedQuery<Categories> query = manager.createQuery("Select cat from Categories cat where cat.id in :ids", Categories.class);
        query.setParameter("ids", ids);
        return query.getResultList();
    }

    @Override
    public List<CreateTickets> getTicketTypeByEvent(int eventId) {
        TypedQuery<CreateTickets> query = manager.createQuery("Select t from CreateTickets t where t.events.id = :id", CreateTickets.class);
        query.setParameter("id", eventId);

        return query.getResultList();
    }

    @Override
    public CreateTickets getTicketTypeUpdate(int typeId) {
        TypedQuery<CreateTickets> query = manager.createQuery("Select tt from CreateTickets tt where tt.id = :id", CreateTickets.class);
        query.setParameter("id", typeId);
        return query.getSingleResult();
    }

    @Override
    public List<Categories> getCatByEvent(int eventId) {
        TypedQuery<Categories> query = manager.createQuery("Select c from Categories c where c.id in (select cat.categories.id from EventCat cat where cat.events.id = :id)", Categories.class);
        query.setParameter("id", eventId);
        return query.getResultList();
    }

    @Override
    public List<Categories> getAllCategories() {
        TypedQuery<Categories> query = manager.createQuery("select c from Categories c", Categories.class);
        return query.getResultList();
    }

    @Override
    public Categories getSingleCategory(int catId) {
        TypedQuery<Categories> query = manager.createQuery("Select c from Categories c where c.id  = :id", Categories.class);
        query.setParameter("id", catId);
        return query.getSingleResult();
    }

    public List<EventHome> getSuggestEvents(){
        List<Object[]> suggestions = new ArrayList<>();
        suggestions = manager.createNativeQuery("Select e.id, e.name, e.banner, e.city, e.location, e.start_booking, min(ct.price) " +
                "from events e " +
                "join createticket ct on ct.Events_id = e.id " +
                "where e.status = 'available' and e.end_time > now() " +
                "group by e.id, e.name " +
                "ORDER BY RAND() " +
                "LIMIT 2 offset 0").getResultList();

        return EventHomeMapper.eventHomeDTO(suggestions);
    }
}


