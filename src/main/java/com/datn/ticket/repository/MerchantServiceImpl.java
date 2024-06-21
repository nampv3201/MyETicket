package com.datn.ticket.repository;

import com.datn.ticket.exception.AppException;
import com.datn.ticket.exception.ErrorCode;
import com.datn.ticket.model.*;
import com.datn.ticket.dto.EventStatisticDTO;
import com.datn.ticket.dto.StatisticsDetail;
import com.datn.ticket.dto.response.ApiResponse;
import com.datn.ticket.model.mapper.EventHomeMapper;
import com.datn.ticket.service.MerchantService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class MerchantServiceImpl implements MerchantService {
    private final EntityManager entityManager;

    @Autowired
    public MerchantServiceImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('MERCHANT') || hasRole('ADMIN')")
    public void updateMerchant(Merchants merchants) {
        try {
            entityManager.merge(merchants);
        }catch (Exception ex){
            throw new AppException(ErrorCode.USER_NOT_EXISTED);
        }
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('MERCHANT') || hasRole('ADMIN')")
    public void addEvent(Events events, List<CreateTickets> ticketsList, List<Categories> categories) {
        entityManager.persist(events);
        if(entityManager.contains(events)){
            for(CreateTickets c : ticketsList){
                c.setEvents(events);
                entityManager.persist(c);
            }

            for(Categories cat : categories){
                EventCat ec = new EventCat();
                ec.setEvents(events);
                ec.setCategories(cat);
                entityManager.persist(ec);
            }

            entityManager.createNativeQuery("insert into revenue (`Events_id`, `totalRevenue`) values (:eventId, 0)")
                    .setParameter("eventId", events.getId()).executeUpdate();
        }
    }

    @Override
    @Transactional()
    @PreAuthorize("hasRole('MERCHANT') || hasRole('ADMIN')")
    public ApiResponse<?> UpdateEvent(Events events, List<CreateTickets> updateTickets,
                                      List<CreateTickets> newTickets, List<Categories> newCategories, List<Categories> removeCategories) {
        try{
            entityManager.merge(events);
            Events uEvent = getEventUpdate(events.getId());
            if(!newTickets.isEmpty()){
                try{
                    for(CreateTickets c : newTickets){
                        c.setEvents(uEvent);
                        c.setMerchants(uEvent.getMerchants());
                        entityManager.persist(c);
                    }
                }catch(Exception e){
                    throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
                }
            }

            if(!updateTickets.isEmpty()){
                try{
                    for(CreateTickets c : updateTickets){
                        entityManager.merge(c);
                    }
                }catch (Exception e){
                    throw  new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
                }
            }

            if(!newCategories.isEmpty()){
                try {
                    for (Categories cat : newCategories) {
                        entityManager.createNativeQuery("insert into events_has_categories (`Events_id`, `Categories_id`) " +
                                        "values (?, ?)").setParameter(1, events.getId())
                                .setParameter(2, cat.getId()).executeUpdate();
                    }
                }catch(Exception e){
                    throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
                }
            }

            if(!removeCategories.isEmpty()){
                try{
                    for(Categories cat : removeCategories){
                        entityManager.createNativeQuery("delete from events_has_categories e where e.Events_id = :eventId " +
                                        "and e.Categories_id = :catId").setParameter("eventId", events.getId())
                                .setParameter("catId", cat.getId()).executeUpdate();
                    }
                }catch(Exception e){
                    throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
                }
            }

            return ApiResponse.builder().message("Cập nhật thành công").build();
        }catch(Exception ex){
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    @Override
    @PreAuthorize("hasRole('MERCHANT') || hasRole('ADMIN')")
    public Events getEventUpdate(int eventId) {
        TypedQuery<Events> eventQuery = entityManager.createQuery("Select e from Events e where e.id = :id", Events.class);
        eventQuery.setParameter("id", eventId);

        return eventQuery.getSingleResult();
    }

    @Override
    @PreAuthorize("hasRole('MERCHANT') || hasRole('ADMIN')")
    public ApiResponse<?> myEvents(Integer status, List<Integer> CategoryId, String time, String city) {
        List<Object[]> events = new ArrayList<>();
        Query getEvent;
        StringBuilder query = new StringBuilder("Select e.id, e.name, e.banner, e.city, e.location, e.start_booking, min(ct.price) " +
                "from events e " +
                "join createticket ct on ct.Events_id = e.id " +
                "where e.Merchants_id = :mId ");

        if(status != null) {
            query.append("and e.status = :status ");
        }
        if(CategoryId != null) {
            query.append("and e.id in (select ecat.Events_id from events_has_categories ecat where ecat.Categories_id in :CategoryId) ");
        }
        if(time.equals("before")){
            query.append("and e.end_time < now() ");
        }
        if(time.equals("after")){
            query.append("and e.end_time > now() ");
        }
        if(city != null){
            query.append("and e.city = :city ");
        }

        // Create Query
        query.append("group by e.id, e.name");
        getEvent = entityManager.createNativeQuery(query.toString());
        getEvent.setParameter("mId", myInfor().getId());

        if(status != null) {
            getEvent.setParameter("status", status);
        }
        if(CategoryId != null && !CategoryId.isEmpty()) {
            getEvent.setParameter("CategoryId", CategoryId);
        }
        if(city != null){
            getEvent.setParameter("city", city);
        }

        events = getEvent.getResultList();
        return ApiResponse.builder()
                .result(EventHomeMapper.eventHomeDTO(events))
                .build();
    }


    @Override
    @PreAuthorize("hasRole('MERCHANT') || hasRole('ADMIN')")
    public List<EventStatisticDTO> getStatistics() throws ParseException {
        Merchants m = myInfor();
        String query = "select sum(c.quantity) as soldTicket, sum(c.cost) as totalRevenue from invoice i " +
                "join cart c on i.Cart_id = c.id " +
                "join createticket ct on c.CreateTicket_id = ct.id " +
                "join events e on ct.Events_id = e.id " +
                "where e.id = :eventId";

        String getCatQuery = "select GROUP_CONCAT(DISTINCT c.category_name SEPARATOR ', ') from categories c " +
                "join events_has_categories ecat on ecat.Categories_id = c.id " +
                "join events e on ecat.Events_id = e.id " +
                "where e.id = :eventId";

        String nativeQuery = "select e.id, e.name, CASE WHEN e.start_time > NOW() THEN 'Chưa diễn ra' ELSE 'Đã diễn ra' END AS Status, " +
                "cast(sum(ct.count) as decimal)" +
                "from createticket ct " +
                "join events e on ct.Events_id = e.id " +
                "join merchants m on e.Merchants_id = m.id " +
                "where m.id = :merchantID " +
                "group by e.id";

        List<Object[]> resultListNav = entityManager.createNativeQuery(nativeQuery, Object[].class)
                .setParameter("merchantID", m.getId())
                .getResultList();

        List<EventStatisticDTO> dtos = new ArrayList<>();
        for (Object[] row : resultListNav) {
            EventStatisticDTO dto = new EventStatisticDTO();
            dto.setEventId((Integer) row[0]);
            dto.setEventName((String) row[1]);
            dto.setStatus((String) row[2]);
            dto.setTotalTicket(((BigDecimal) row[3]).intValue());

            try{
                String cat = (String) entityManager.createNativeQuery(getCatQuery).setParameter("eventId", row[0]).getSingleResult();
                dto.setCategories(cat);
                Object[] sold = (Object[]) entityManager.createNativeQuery(query, Object[].class)
                        .setParameter("eventId", row[0])
                        .getSingleResult();
                dto.setSoldTicket(sold[0] == null ? 0 : ((BigDecimal)sold[0]).intValue());
                dto.setTotalRevenue(sold[1] == null ? 0 : ((Double)sold[1]));
            }catch (NoResultException e){
                dto.setSoldTicket(0);
                dto.setTotalRevenue(0);
            }
            dtos.add(dto);
        }
        return dtos;
    }

    @Override
    @PreAuthorize("hasRole('MERCHANT') || hasRole('ADMIN')")
    public List<StatisticsDetail> getStatisticsByEvent(int eventId) throws ParseException {
        String query = "select e.id, e.name, GROUP_CONCAT(DISTINCT cat.category_name SEPARATOR ', ') AS categories, " +
                "CASE WHEN e.start_time > NOW() THEN 'Chưa diễn ra' ELSE 'Đã diễn ra' END AS Status, " +
                "ct.id, ct.type_name, ct.count, COALESCE(SUM(c.quantity), 0) as soldTicket, " +
                "COALESCE(sum(c.cost), 0) as totalRevenue from createticket ct " +
                "join events e on ct.Events_id = e.id " +
                "join events_has_categories ecat on ecat.Events_id = e.id " +
                "join categories cat on ecat.Categories_id = cat.id " +
                "left join cart c on c.CreateTicket_id = ct.id " +
                "left join invoice i on i.Cart_id = c.id " +
                "where e.id = :eventId " +
                "group by ct.type_name, ct.id, ct.count";

        List<Object[]> resultListNav = entityManager.createNativeQuery(query, Object[].class)
                .setParameter("eventId", eventId)
                .getResultList();

        List<StatisticsDetail> dtos = new ArrayList<>();
        for (Object[] row : resultListNav) {
            StatisticsDetail summary = new StatisticsDetail();
            summary.setEventId((Integer) row[0]);
            summary.setEventName((String) row[1]);
            summary.setCategories((String) row[2]);
            summary.setStatus((String) row[3]);
            summary.setTicketTypeId((Integer) row[4]);
            summary.setTicketTypeName((String) row[5]);
            summary.setTotalTicket((Integer) row[6]);
            summary.setSoldTicket(((BigDecimal) row[7]).intValue());
            summary.setTypeRevenue((Double) row[8]);

            dtos.add(summary);
        }

        return dtos;
    }

    @Override
    @PreAuthorize("hasRole('MERCHANT') || hasRole('ADMIN')")
    @Transactional
    public String deEvents(int id) {
        try{
            entityManager.createNativeQuery("update events set deleted = not deleted where id = :id")
                    .setParameter("id", id)
                    .executeUpdate();
            return "Thành công";
        }catch (Exception e){
            return "Thất bại";
        }
    }

    @Override
    @PreAuthorize("hasRole('MERCHANT') || hasRole('ADMIN')")
    public Merchants myInfor() {
        try {
            return (Merchants) entityManager.createNativeQuery("select m.* from merchants m " +
                            "join account a on m.Account_id = a.id " +
                            "where a.id = :id", Merchants.class)
                    .setParameter("id", SecurityContextHolder.getContext().getAuthentication().getName())
                    .getSingleResult();
        }catch (Exception ex){
            throw new AppException(ErrorCode.USER_NOT_EXISTED);
        }
    }

    @Override
    @Transactional
    public void revockTicket(String qrCode) {
        entityManager.createNativeQuery("update ticketrelease t set t.status = 0").executeUpdate();
    }
}
