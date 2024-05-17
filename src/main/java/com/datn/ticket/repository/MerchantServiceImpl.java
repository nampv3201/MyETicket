package com.datn.ticket.repository;

import com.datn.ticket.model.Merchants;
import com.datn.ticket.model.dto.EventStatisticDTO;
import com.datn.ticket.model.dto.StatisticsDetail;
import com.datn.ticket.service.MerchantService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
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
    public void updateMerchant(Merchants merchants) {
        entityManager.merge(merchants);
    }

    @Override
    public List<EventStatisticDTO> getStatistics() throws ParseException {
        Merchants m = myInfor();
        String query = "select sum(c.quantity) as soldTicket, sum(c.cost) as totalRevenue from invoice i " +
                "join cart c on i.Cart_id = c.id " +
                "join createticket ct on c.CreateTicket_id = ct.id " +
                "join events e on ct.Events_id = e.id " +
                "where e.id = :eventId";

        String nativeQuery = "select e.id, e.name, CASE WHEN e.start_time > NOW() THEN 'Chưa diễn ra' ELSE 'Đã diễn ra' END AS Status, " +
                "cast(sum(ct.count) as decimal), " +
                "GROUP_CONCAT(DISTINCT c.category_name SEPARATOR ', ') AS categories " +
                "from createticket ct " +
                "join events e on ct.Events_id = e.id " +
                "join events_has_categories ecat on ecat.Events_id = e.id " +
                "join categories c on ecat.Categories_id = c.id " +
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
            dto.setCategories((String) row[4]);
            try{
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
    public Merchants myInfor() {
        return (Merchants) entityManager.createNativeQuery("select m.* from merchants m " +
                "join account a on m.Account_id = a.id " +
                "where a.id = :id", Merchants.class)
                .setParameter("id", SecurityContextHolder.getContext().getAuthentication().getName())
                .getSingleResult();
    }
}
