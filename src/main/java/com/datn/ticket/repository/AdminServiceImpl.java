package com.datn.ticket.repository;

import com.datn.ticket.dto.response.*;
import com.datn.ticket.exception.AppException;
import com.datn.ticket.exception.ErrorCode;
import com.datn.ticket.model.Categories;
import com.datn.ticket.model.Merchants;
import com.datn.ticket.model.PaymentGateway;
import com.datn.ticket.dto.EventStatisticDTO;
import com.datn.ticket.model.Users;
import com.datn.ticket.model.mapper.EventHomeMapper;
import com.datn.ticket.service.AdminService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
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
    @PreAuthorize("hasRole('ADMIN')")
    public List<AccountResponse> getAccount() {
        Query query = manager.createNativeQuery("select a.id, a.username, " +
                "GROUP_CONCAT(DISTINCT r.role_name ORDER BY r.role_name ASC SEPARATOR ', ') " +
                "from account a " +
                "join account_has_role ar on a.id = ar.Account_id " +
                "join role r on ar.role_id = r.id group by a.id", AccountResponse.class);
        List<AccountResponse> accounts = query.getResultList();
        return accounts;
    }

    // Lấy account cụ thể
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public AccountResponse getByID(Integer id) {
        Query query = manager.createNativeQuery("select a.id, a.username, " +
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
    @PreAuthorize("hasRole('ADMIN')")
    public Merchants getMerchantInfor(Integer id) {
        TypedQuery<Merchants> getMerchant = manager.createQuery("Select m from Merchants m where m.id = :id", Merchants.class);
        getMerchant.setParameter("id", id);
        return getMerchant.getSingleResult();
    }

    // Lấy danh sách Merchant
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public List<AMerchantResponse> getListMerchants() {
        Query getMerchant = manager.createNativeQuery("select m.id, a.username, m.name, m.address, ar.status from merchants m " +
                "join account a on m.Account_id = a.id " +
                "join account_has_role ar on a.id = ar.Account_id " +
                "join role r on ar.role_id = r.id " +
                "where r.role_name = 'MERCHANT'", AMerchantResponse.class);
        return getMerchant.getResultList();
    }

    // Lấy thông tin user cụ thể
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public Users getUserInfor(Integer id) {
        TypedQuery<Users> getUser = manager.createQuery("Select m from Users m where m.id = :id", Users.class);
        getUser.setParameter("id", id);
        return getUser.getSingleResult();
    }

    // Lấy danh sách User
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public List<AUserResponse> getListUsers() {
        Query getMerchant = manager.createNativeQuery("select u.id, a.username, u.name, u.address, u.phone, ar.status from users u " +
                "join account a on u.Account_id = a.id " +
                "join account_has_role ar on a.id = ar.Account_id " +
                "join role r on ar.role_id = r.id " +
                "where r.role_name = 'USER'", AUserResponse.class);
        return getMerchant.getResultList();
    }

    // Thêm danh mục mới
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void addNewCategory(Categories categories) {
        manager.persist(categories);
    }

    // Xóa danh mục
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void removeCategory(int catId) {
        manager.createNativeQuery("delete from categories where id = :catId")
                .setParameter("catId", catId)
                .executeUpdate();
    }

    // Thêm cổng thanh toán
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void addNewPaymentGateway(PaymentGateway gateway) {
        manager.persist(gateway);
    }

    // Đổi trạng thái sự kiện
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void changeEventStatus(int eventId, String status) {
        StringBuilder query = new StringBuilder();
        query.append("update events e set e.status = ");
        if (status != null){
            query.append(":status ");
        }else{
            query.append("IF(e.status = 'available', 'rejected', 'available') ");
        }
        query.append("where e.id = :eventId");
        Query myQuery = manager.createNativeQuery(query.toString()).setParameter("eventId", eventId);
        if(status != null) {
            myQuery.setParameter("status", status);
        }
        myQuery.executeUpdate();
    }

    // Vô hiệu hóa tài khoản
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void changeAccountStatus(String username, String roleName) {
        Query query = manager.createNativeQuery("Update account_has_role ar " +
                "join role r on ar.role_id = r.id " +
                "join account a on ar.Account_id = a.id " +
                "set ar.status = not ar.status " +
                "where a.username = :username and r.role_name = :roleName");
        query.setParameter("username", username)
                .setParameter("roleName", roleName).executeUpdate();
    }


    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<?> allEvents(Integer offset, Integer size, String merchantName, List<Integer> categoriesId, String time,
                                    List<String> city, String fromTime, String toTime, Double minPrice, Double maxPrice, String status) {

        List<Object[]> events = new ArrayList<>();
        Query getEvent;
        StringBuilder query = new StringBuilder("select e.id, e.name, concat(e.city, ', ', e.location), GROUP_CONCAT(DISTINCT c.category_name SEPARATOR ', '), " +
                "concat(DATE_FORMAT(STR_TO_DATE(e.start_time, '%Y-%m-%d %H:%i:%s'), '%H:%i'), " +
                "' - ', " +
                "DATE_FORMAT(STR_TO_DATE(e.end_time, '%Y-%m-%d %H:%i:%s'), '%H:%i'), " +
                "' ngày ', " +
                "DATE_FORMAT(STR_TO_DATE(e.start_time, '%Y-%m-%d %H:%i:%s'), '%Y-%m-%d')), " +
                "sum(ct.count), sum(ct.available), min(ct.price), e.status, m.id, m.name " +
                "from events e " +
                "join merchants m on e.Merchants_id = m.id  " +
                "join createticket ct on ct.Events_id = e.id " +
                "join events_has_categories ecat on ecat.Events_id = e.id " +
                "join categories c on ecat.Categories_id = c.id " +
                "where 1=1 ");


        // Add query by filtering
        if(merchantName != null) {
            query.append("and m.name = :merchantName ");
        }
        if(status != null) {
            query.append("and e.status = :status ");
        }
        if(categoriesId != null) {
            query.append("and e.id in (select ecat.Events_id from events_has_categories ecat where ecat.Categories_id in :categoriesId) ");
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
        query.append("and e.start_time between :fromTime and :toTime " +
                "group by e.id, e.name " +
                "having min(ct.price) between :minPrice and :maxPrice " +
                "limit :limit offset :offset ");
        getEvent = manager.createNativeQuery(query.toString())
                .setParameter("fromTime", fromTime)
                .setParameter("toTime", toTime)
                .setParameter("minPrice", minPrice)
                .setParameter("maxPrice", String.valueOf(maxPrice))
                .setParameter("limit", size)
                .setParameter("offset", offset);

        // Add Query Params
        if(merchantName != null) {
            getEvent.setParameter("merchantName", merchantName);
        }
        if(categoriesId != null && !categoriesId.isEmpty()) {
            getEvent.setParameter("categoriesId", categoriesId);
        }
        if(city != null){
            getEvent.setParameter("city", city);
        }
        if(status != null){
            getEvent.setParameter("status", status);
        }
        try {
            events = getEvent.getResultList();
            return ApiResponse.builder().result(EventHomeMapper.adminAllEvents(events)).build();
        }catch (EmptyResultDataAccessException e){
            throw AppException.from(e, ErrorCode.ITEM_NOT_FOUND);
        }

    }

    @Override
    public List<Categories> getAllCategories() {
        TypedQuery<Categories> query = manager.createQuery("select c from Categories c", Categories.class);
        return query.getResultList();
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
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
    public List<AdminEventResponse> getEventPending() {
        Query query = manager.createNativeQuery("select e.id, e.name, concat(e.city, ', ', e.location) as location, e.status, m.id, m.name from events e " +
                "join merchants m on e.Merchants_id = m.id " +
                "where e.status = 'pending'", AdminEventResponse.class);
        try {
            return query.getResultList();
        }catch(Exception e) {
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    @Override
    @Transactional
    public ApiResponse<?> getPaymentHistory(String paymentDate, String status, Integer uId) {
        Query timeQuery = manager.createNativeQuery("SET time_zone = '+7:00'");
        timeQuery.executeUpdate();

        StringBuilder sql = new StringBuilder("Select p.id, cast(p.payment_time as char), p.payment_status, cast(p.payment_amount as char), u.id from payment p " +
                "join users u on p.Users_id = u.id " +
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

//        try {
            return ApiResponse.<List<PaymentHistoryResponse>>builder()
                    .result(query.getResultList())
                    .build();
//        }catch (NoResultException e){
//            log.error("Not found");
//            return ApiResponse.<List<PaymentHistoryResponse>>builder().message(e.getMessage()).build();
//        }catch(AppException e){
//            throw new AppException(e.getErrorCode());
//        }catch (Exception e){
//            log.error(e.getMessage());
//            return ApiResponse.<List<PaymentHistoryResponse>>builder().message(e.getMessage()).build();
//        }
    }
}
