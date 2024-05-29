package com.datn.ticket.repository;

import com.datn.ticket.exception.AppException;
import com.datn.ticket.exception.ErrorCode;
import com.datn.ticket.model.Categories;
import com.datn.ticket.model.Merchants;
import com.datn.ticket.model.PaymentGateway;
import com.datn.ticket.model.dto.EventStatisticDTO;
import com.datn.ticket.model.dto.response.AccountResponse;
import com.datn.ticket.model.dto.response.ApiResponse;
import com.datn.ticket.model.dto.response.PaymentHistoryResponse;
import com.datn.ticket.model.dto.response.PaymentHistoryResponseDetail;
import com.datn.ticket.model.mapper.EventHomeMapper;
import com.datn.ticket.service.AdminService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.*;

@Repository
@Slf4j
public class AdminServiceImpl implements AdminService {
    private final EntityManager manager;

    @Autowired
    public AdminServiceImpl(EntityManager manager) {
        this.manager = manager;
    }

    // Lấy danh sách account
    @Override
    @PreAuthorize("hasRole('Admin')")
    public List<AccountResponse> getAccount() {
        Query query = manager.createNativeQuery("select a.id, a.username, a.status, " +
                "GROUP_CONCAT(DISTINCT r.role_name ORDER BY r.role_name ASC SEPARATOR ', ') " +
                "from account a " +
                "join account_has_role ar on a.id = ar.Account_id " +
                "join role r on ar.role_id = r.id group by a.id", AccountResponse.class);
        List<AccountResponse> accounts = query.getResultList();
        return accounts;
    }

    // Lấy account cụ thể
    @Override
    @PreAuthorize("hasRole('Admin')")
    public AccountResponse getByID(Integer id) {
        Query query = manager.createNativeQuery("select a.id, a.username, a.status, " +
                "GROUP_CONCAT(DISTINCT r.role_name ORDER BY r.role_name ASC SEPARATOR ', ') " +
                "from account a " +
                "join account_has_role ar on a.id = ar.Account_id " +
                "join role r on ar.role_id = r.id where a.id = :id", AccountResponse.class);
        query.setParameter("id", id);
        try{
            return (AccountResponse) query.getSingleResult();
        }catch (NoResultException e) {
            throw new AppException(ErrorCode.USER_NOT_EXISTED);
        }
    }

    // Lấy thông tin merchant cụ thể
    @Override
    @PreAuthorize("hasRole('Admin')")
    public Merchants getMerchantInfor(Integer id) {
        TypedQuery<Merchants> getMerchant = manager.createQuery("Select m from Merchants m where m.id = :id", Merchants.class);
        getMerchant.setParameter("id", id);
        return getMerchant.getSingleResult();
    }

    // Lấy danh sách Merchant
    @Override
    @PreAuthorize("hasRole('Admin')")
    public List<Merchants> getListMerchants() {
        TypedQuery<Merchants> getMerchant = manager.createQuery("Select m from Merchants m", Merchants.class);
        return getMerchant.getResultList();
    }

    // Thêm danh mục mới
    @Override
    @PreAuthorize("hasRole('Admin')")
    @Transactional
    public void addNewCategory(Categories categories) {
        manager.persist(categories);
    }

    // Xóa danh mục
    @Override
    @PreAuthorize("hasRole('Admin')")
    @Transactional
    public void removeCategory(int catId) {
        manager.createNativeQuery("delete from categories where id = :catId")
                .setParameter("catId", catId)
                .executeUpdate();
    }

    // Thêm cổng thanh toán
    @Override
    @PreAuthorize("hasRole('Admin')")
    @Transactional
    public void addNewPaymentGateway(PaymentGateway gateway) {
        manager.persist(gateway);
    }


    // Vô hiệu hóa sự kiện
    @Override
    @PreAuthorize("hasRole('Admin')")
    @Transactional
    public void disableEvent(int eventId) {
        Query query = manager.createNativeQuery("Update events e set e.status = 0 where e.id = :eventId");
        query.setParameter("eventId", eventId).executeUpdate();
    }

    // Kích hoạt sự kiện
    @Override
    @PreAuthorize("hasRole('Admin')")
    @Transactional
    public void enableEvent(int eventId) {
        Query query = manager.createNativeQuery("Update events e set e.status = 1 where e.id = :eventId");
        query.setParameter("eventId", eventId).executeUpdate();
    }

    // Vô hiệu hóa tài khoản
    @Override
    @PreAuthorize("hasRole('Admin')")
    @Transactional
    public void disaleAccount(int accountId, int roleId) {
        Query query = manager.createNativeQuery("Update account_has_role a set a.status = 0 " +
                "where a.Account_id = :accountId and a.role_id = :roleId");
        query.setParameter("accountId", accountId)
                .setParameter("roleId", roleId).executeUpdate();
    }

    // Kích hoạt tài khoản
    @Override
    @PreAuthorize("hasRole('Admin')")
    @Transactional
    public void enableAccount(int accountId, int roleId) {
        Query query = manager.createNativeQuery("Update account_has_role a set a.status = 1 " +
                "where a.Account_id = :accountId and a.role_id = :roleId");
        query.setParameter("accountId", accountId)
                .setParameter("roleId", roleId).executeUpdate();
    }

    @Override
    @PreAuthorize("hasRole('Admin')")
    public ApiResponse<?> allEvents(Integer MerchantId, List<Integer> CategoryId, Integer allTime, String city) {
        List<Object[]> events = new ArrayList<>();
        Query getEvent;
        StringBuilder query = new StringBuilder("Select e.id, e.name, e.banner, e.city, e.location, e.start_booking, min(ct.price) " +
                "from events e " +
                "join createticket ct on ct.Events_id = e.id " +
                "where 1=1 ");


        // Add query by filtering
        if(MerchantId != null) {
            query.append("and e.Merchants_id = :MerchantId ");
        }
        if(CategoryId != null) {
            query.append("and e.id in (select ecat.Events_id from events_has_categories ecat where ecat.Categories_id in :CategoryId) ");
        }
        if(allTime == null){
            query.append("and e.end_time > now() ");
        }
        if(city != null){
            query.append("and e.city = :city ");
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
        return ApiResponse.builder().result(EventHomeMapper.eventHomeDTO(events)).build();
    }

    @Override
    public List<Categories> getAllCategories() {
        TypedQuery<Categories> query = manager.createQuery("select c from Categories c", Categories.class);
        return query.getResultList();
    }

    @Override
    @PreAuthorize("hasRole('Admin')")
    public List<EventStatisticDTO> getStatistics(int merchantId) throws ParseException {
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

        List<Object[]> resultListNav = manager.createNativeQuery(nativeQuery, Object[].class)
                .setParameter("merchantID", merchantId)
                .getResultList();

        List<EventStatisticDTO> dtos = new ArrayList<>();
        for (Object[] row : resultListNav) {
            EventStatisticDTO dto = new EventStatisticDTO();
            dto.setEventId((Integer) row[0]);
            dto.setEventName((String) row[1]);
            dto.setStatus((String) row[2]);
            dto.setTotalTicket(((BigDecimal) row[3]).intValue());

            try{
                String cat = (String) manager.createNativeQuery(getCatQuery).setParameter("eventId", row[0]).getSingleResult();
                dto.setCategories(cat);
                Object[] sold = (Object[]) manager.createNativeQuery(query, Object[].class)
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
    public ApiResponse<?> getPaymentHistoryDetail(String paymentId) {
        List<Object> cart = new ArrayList<Object>();
        PaymentHistoryResponseDetail response = new PaymentHistoryResponseDetail();
        List<Object[]> historyResponseDetail =
                manager.createNativeQuery("select p.id, p.payment_time, p.payment_status, c.id as cart_id, c.cost, u.id, u.name from payment p " +
                        "join invoice i on i.Payment_id = p.id " +
                        "join cart c on i.Cart_id = c.id " +
                        "join createticket ct on c.CreateTicket_id = ct.id " +
                        "join events e on e.id = ct.Events_id " +
                        "join users u on c.Users_id = u.id " +
                        "where p.id = :paymentId")
                .setParameter("paymentId", paymentId)
                .getResultList();
        if(!historyResponseDetail.isEmpty()) {
            response.setPaymentId(historyResponseDetail.get(0)[0].toString());
            response.setPaymentTime(historyResponseDetail.get(0)[1].toString());
            response.setPaymentStatus(historyResponseDetail.get(0)[2].toString());
            response.setUId((Integer) historyResponseDetail.get(0)[5]);
            response.setUName(historyResponseDetail.get(0)[6].toString());
            for(Object[] o : historyResponseDetail){
                Map<String, Object> myMap = new HashMap<>();
                myMap.put("cardId", (Integer) o[3]);
                myMap.put("price", (Double) o[4]);
                cart.add(myMap);
            }
            response.setCart(cart);
        }

        try{
            return ApiResponse.<PaymentHistoryResponseDetail>builder()
                    .result(response)
                    .build();
        }catch (NoResultException e) {
            log.error("Not found");
            return ApiResponse.builder().message(e.getMessage()).build();
        }catch(AppException e) {
            throw new AppException(e.getErrorCode());
        }
    }

    @Override
    public ApiResponse<?> getPaymentHistory(String paymentDate, String status, Integer uId) {
        StringBuilder sql = new StringBuilder("Select p.id, p.payment_time, p.payment_status, c.cost, u.id from payment p " +
                "join invoice i on i.Payment_id = p.id " +
                "join cart c on i.Cart_id = c.id " +
                "join createticket ct on c.CreateTicket_id = ct.id " +
                "join events e on e.id = ct.Events_id " +
                "join users u on c.Users_id = u.id " +
                "where 1=1 ");

        if (paymentDate != null) {
            sql.append("and Date(p.payment_time) = :paymentDate ");
        }
        if (status != null) {
            sql.append("and p.payment_status = :status ");
        }
        if(uId != null){
            sql.append("and u.id = :uId ");
        }

        Query query = manager.createNativeQuery(sql.toString(), PaymentHistoryResponse.class);
        if (paymentDate != null) {
            query.setParameter("paymentDate", paymentDate);
        }
        if (status != null) {
            query.setParameter("status", status);
        }
        if(uId != null){
            query.setParameter("uId", uId);
        }

        try {
            return ApiResponse.<List<PaymentHistoryResponse>>builder()
                    .result(query.getResultList())
                    .build();
        }catch (NoResultException e){
            log.error("Not found");
            return ApiResponse.<List<PaymentHistoryResponse>>builder().message(e.getMessage()).build();
        }catch(AppException e){
            throw new AppException(e.getErrorCode());
        }catch (Exception e){
            log.error(e.getMessage());
            return ApiResponse.<List<PaymentHistoryResponse>>builder().message(e.getMessage()).build();
        }
    }
}
