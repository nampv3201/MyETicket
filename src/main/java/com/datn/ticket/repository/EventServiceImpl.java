package com.datn.ticket.repository;

import com.datn.ticket.model.Categories;
import com.datn.ticket.model.CreateTickets;
import com.datn.ticket.model.Events;
import com.datn.ticket.model.dto.response.CreateTicketsResponse;
import com.datn.ticket.model.dto.EventDTO;
import com.datn.ticket.model.dto.response.ApiResponse;
import com.datn.ticket.model.mapper.CreateTicketMapper;
import com.datn.ticket.model.mapper.EventHomeMapper;
import com.datn.ticket.model.mapper.EventMapper;
import com.datn.ticket.service.EventService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
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
                "where e.name like :keyWord and e.status = 1 group by e.id");
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
        categoryList = categoryQuery.getResultList();

        TypedQuery<CreateTickets> typedQuery = manager.createQuery("Select tt from CreateTickets tt where tt.events = :event", CreateTickets.class);
        typedQuery.setParameter("event", e);
        for(CreateTickets c : typedQuery.getResultList()){
            ticketsDTOS.add(CreateTicketMapper.createTicketsDTO(c));
        }
        return ApiResponse.<EventDTO>builder()
                        .result(EventMapper.eventDTO(e, categoryList, ticketsDTOS)).build();
    }

    @Override
    public ResponseEntity<Object> getEventByFilter(Integer MerchantId, List<Integer> CategoryId, String time, String city) {
        List<Object[]> events = new ArrayList<>();
        Query getEvent;
        StringBuilder query = new StringBuilder("Select e.id, e.name, e.banner, e.city, e.location, e.start_booking, min(ct.price) " +
                "from events e " +
                "join createticket ct on ct.Events_id = e.id " +
                "where e.status = 1 ");


        // Add query by filtering
        if(MerchantId != null) {
            query.append("and e.Merchants_id = :MerchantId ");
        }
        if(CategoryId != null) {
            query.append("and e.id in (select ecat.Events_id from events_has_categories ecat where ecat.Categories_id in :CategoryId) ");
        }
        if(time == null){
            query.append("and e.end_time > now() ");
        }
        if(time!=null){
            if(time.equals("before")){
                query.append("and e.end_time < now() ");
            }
            if(city != null){
                query.append("and e.city = :city ");
            }
        }

        // Create Query
        query.append("group by e.id, e.name");
        getEvent = manager.createNativeQuery(query.toString());

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

        events = getEvent.getResultList();
        return ResponseEntity.ok().body(EventHomeMapper.eventHomeDTO(events));
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


}


