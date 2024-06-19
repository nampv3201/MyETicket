package com.datn.ticket.repository;

import com.datn.ticket.dto.request.LogoutRequest;
import com.datn.ticket.dto.request.RefreshRequest;
import com.datn.ticket.dto.request.SignUpMerchantInApp;
import com.datn.ticket.dto.response.*;
import com.datn.ticket.exception.AppException;
import com.datn.ticket.exception.ErrorCode;
import com.datn.ticket.model.Cart;
import com.datn.ticket.model.Users;
import com.datn.ticket.dto.request.UpdateCartRequest;
import com.datn.ticket.service.AccountService;
import com.datn.ticket.service.EventService;
import com.datn.ticket.service.UserService;
import com.datn.ticket.util.QRCodeService;
import com.nimbusds.jose.JOSEException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestBody;

import java.text.ParseException;
import java.util.*;

@Repository
@Slf4j
public class UserServiceImpl implements UserService {
    private final EntityManager manager;

    @Autowired
    private EventService eventService;

    @Autowired
    AccountService accountService;

    @Autowired
    private QRCodeService qrcodeService;

    @Autowired
    private JavaMailSender emailSender;

    @Autowired
    public UserServiceImpl(EntityManager entityManager){
        this.manager = entityManager;
    }


    @Override
    @Transactional
    @PreAuthorize("hasRole('USER')")
    public void updateUser(Users user) {
        manager.merge(user);
    }

    @Override
//    @PreAuthorize("hasRole('USER')")
    public Users myInfor() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        String query = "Select u.* from users u " +
                "join account a on u.Account_id = a.id " +
                "where a.id = :id";
        Query getUsers = manager.createNativeQuery(query, Users.class);
        getUsers.setParameter("id", authentication.getName());
        return (Users) getUsers.getSingleResult();
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('USER') || hasRole('ADMIN') || hasRole('MERCHANT')")
    public ApiResponse<?> signUpMerchant(SignUpMerchantInApp signUpRequest) throws ParseException, JOSEException {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        // check role
        Query query = manager.createNativeQuery("select * from account_has_role ar where ar.Account_id = :account_id " +
                "and ar.role_id = 2").setParameter("account_id", authentication.getName());
        if (query.getResultList().size() > 0) {
            return ApiResponse.<AuthenticationResponse>builder().code(ErrorCode.ROLE_HAS_REGISTERED.getCode())
                    .message(ErrorCode.ROLE_HAS_REGISTERED.getMessage()).build();
        }

        // check condition
        if(checkMerchantCondition(signUpRequest.getSignUpRequest().getLicense())){
            log.info(signUpRequest.getSignUpRequest().getLicense());
            return ApiResponse.builder()
                    .code(ErrorCode.LICENSE_HAS_REGISTERED.getCode())
                    .message(ErrorCode.LICENSE_HAS_REGISTERED.getMessage())
                    .build();
        }

        manager.createNativeQuery("insert into merchants " +
                        "(`name`, `address`, `phone`, `license`, `Account_id`) " +
                        "values (:name, :address, :phone, :license, :Account_id)")
                .setParameter("name", signUpRequest.getSignUpRequest().getName())
                .setParameter("address", signUpRequest.getSignUpRequest().getAddress())
                .setParameter("phone", signUpRequest.getSignUpRequest().getPhone())
                .setParameter("license", signUpRequest.getSignUpRequest().getLicense())
                .setParameter("Account_id", authentication.getName()).executeUpdate();

        manager.createNativeQuery("insert into account_has_role " +
                        "(`Account_id`, `role_id`, `status`) " +
                        "values (:account_id, 2, 1)")
                .setParameter("account_id", authentication.getName()).executeUpdate();

        AuthenticationResponse res = accountService.refreshToken(RefreshRequest.builder()
                .token(signUpRequest.getToken())
                .build());

        return ApiResponse.<AuthenticationResponse>builder()
                .result(res).build();
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('USER') || hasRole('ADMIN')")
    public void addToCart(List<Cart> carts) {
        Users u = myInfor();
        for(Cart c : carts){
            Double price = Double.parseDouble(manager.createNativeQuery("select ct.price from createticket ct where ct.id = :id")
                    .setParameter("id", c.getCreateTickets().getId())
                    .getSingleResult()
                    .toString()) * c.getQuantity();
            c.setCost(price);
            c.setStatus(0);
            c.setUser(u);

            if(!manager.createNativeQuery("Select * from cart where cart.CreateTicket_id = :ctId " +
                    "and cart.Users_id = :uId " +
                    "and cart.status = 0")
                    .setParameter("ctId", c.getCreateTickets().getId())
                    .setParameter("uId", u.getId())
                    .getResultList().isEmpty()){
                manager.createNativeQuery("update cart set `quantity` = `quantity` + :quantity, `cost` = `cost` + :cost " +
                        "where cart.CreateTicket_id = :ctId " +
                        "and cart.Users_id = :uId " +
                        "and cart.status = 0").setParameter("quantity", c.getQuantity())
                        .setParameter("cost", c.getCost())
                        .setParameter("uId", u.getId())
                        .setParameter("ctId", c.getCreateTickets().getId()).executeUpdate();
                continue;
            }

            manager.createNativeQuery("INSERT INTO cart (`quantity`, `cost`, `status`, `Users_id`, `CreateTicket_id`) " +
                    "VALUES (?, ?, ?, ?, ?)")
                    .setParameter(1, c.getQuantity())
                    .setParameter(2, c.getCost())
                    .setParameter(3, c.getStatus())
                    .setParameter(4, c.getUser().getId())
                    .setParameter(5, c.getCreateTickets().getId())
                    .executeUpdate();
        }
    }

    @Override
    @Transactional
//    @PreAuthorize("hasRole('USER') || hasRole('ADMIN')")
    public List<Integer> directOrder(List<Cart> carts) {
        Users u = myInfor();
        List<Integer> cartIds = new ArrayList<>();
        for(Cart c : carts){
            Double price = Double.parseDouble(manager.createNativeQuery("select ct.price from createticket ct where ct.id = :id")
                    .setParameter("id", c.getCreateTickets().getId())
                    .getSingleResult()
                    .toString()) * c.getQuantity();
            c.setCost(price);
            c.setStatus(0);
            c.setUser(u);

            manager.persist(c);
            cartIds.add(c.getId());
        }
        return cartIds;
    }

    @Override
    @PreAuthorize("hasRole('USER') || hasRole('ADMIN')")
    public Cart getSingleCart(int cartId) {
        return manager.createQuery("select c from Cart c where c.id = :cartId", Cart.class)
                .setParameter("cartId", cartId)
                .getSingleResult();
    }

    @Override
    @PreAuthorize("hasRole('USER') || hasRole('ADMIN')")
    public List<CartResponse> myCart() {
        String query = "select c.id as Cart_id, ct.id as TicketType_id, ct.type_name, c.quantity, ct.available, c.cost, e.id as Event_id, e.name from cart c " +
                "join createticket ct on c.CreateTicket_id = ct.id " +
                "join events e on ct.Events_id = e.id " +
                "where c.Users_id = :UID and c.status = 0";
        return manager.createNativeQuery(query, CartResponse.class)
                .setParameter("UID", myInfor().getId())
                .getResultList();
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('USER') || hasRole('ADMIN')")
    public void removeFromCart(List<Integer> cartId) {
        for(int i : cartId){
            manager.createNativeQuery("delete from cart where id = :id")
                   .setParameter("id", i)
                   .executeUpdate();
        }
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('USER') || hasRole('ADMIN')")
    public void updateCart(UpdateCartRequest request) {
        double result = ((Long) manager.createNativeQuery("select ct.price from createticket ct " +
                        "join cart c on c.CreateTicket_id = ct.id " +
                        "where c.id =:id")
                .setParameter("id", request.getCartId()).getSingleResult()).doubleValue();
        Double price = request.getUpdateNumber() * result;
        manager.createNativeQuery("update cart set cart.quantity = :quantity, cart.cost = :cost where cart.id = :id")
                .setParameter("quantity", request.getUpdateNumber())
                .setParameter("cost", price)
                .setParameter("id", request.getCartId())
                .executeUpdate();
    }

    @Override
    @PreAuthorize("hasRole('USER') || hasRole('ADMIN')")
    public ApiResponse checkQuantity(int cartId) {
        Query query = manager.createNativeQuery("SELECT CASE WHEN c.quantity < ct.available THEN 'OK' " +
                "ELSE concat(ct.type_name + ' còn lại: ',  ct.available) END AS result " +
                "FROM cart c " +
                "JOIN createticket ct ON c.CreateTicket_id = ct.id " +
                "where c.id = :cartID").setParameter("cartID", cartId);

        return ApiResponse.builder().message((String) query.getSingleResult()).build();
    }

    @Override
    @Transactional
//    @PreAuthorize("hasRole('USER') || hasRole('ADMIN')")
    public String payment(PaymentResponse response) {
        String paymentStatus;

        Query newPayment = manager.createNativeQuery("insert into payment (`id`, `payment_status`, `create_time`, `payment_time`, `payment_amount`, `PaymentMethod_id`, `Users_id`) " +
                "values (?,?,?,now(),?,?,?)")
                .setParameter(1, response.getBankTranNo())
                .setParameter(3, response.getPaymentDate())
                .setParameter(4, response.getAmount())
                .setParameter(5, 1)
                .setParameter(6, response.getUId());

        if(response.getResponseCode().equals("00")){
            paymentStatus = "Thanh toán thành công, vé sẽ sớm được gửi vào email của bạn.";
            newPayment.setParameter(2, paymentStatus).executeUpdate();

            // Thông tin events
            List<Object[]> eventMail = manager.createNativeQuery("select e.id, e.name, " +
                            "group_concat(c.id SEPARATOR ', ') as tType, " +
                            "group_concat(concat(c.quantity, ' vé loại ', ct.type_name) SEPARATOR ', ') as ticket " +
                            "from Cart c " +
                            "join createticket ct on c.CreateTicket_id = ct.id " +
                            "join events e on ct.Events_id = e.id " +
                            "where c.id in :cartId " +
                            "group by e.id, e.name")
                    .setParameter("cartId", response.getCartIds()).getResultList();

            // Tạo invoice
            for(int i : response.getCartIds()){
                Object[] obj = (Object[]) manager.createNativeQuery("select c.CreateTicket_id, c.cost from Cart c where c.id = :cartId")
                                .setParameter("cartId", i).getSingleResult();

                int ctId = (int) obj[0];
                double price = (double)obj[1];

                // Insert invoice
                manager.createNativeQuery("insert into invoice (`Cart_id`, `Payment_id`) " +
                        "values (:cartId, :paymentId)").setParameter("cartId",i)
                        .setParameter("paymentId", response.getBankTranNo()).executeUpdate();

                // Update cart status
                manager.createNativeQuery("update cart set status = 1 where id = :cartId")
                        .setParameter("cartId", i).executeUpdate();

                // Update event revenue
                manager.createNativeQuery("update revenue r set r.totalRevenue = r.totalRevenue + :price where r.Events_id in " +
                        "(SELECT ct.Events_id " +
                        "FROM createticket ct " +
                        "JOIN events e ON ct.Events_id = e.id " +
                        "WHERE ct.id = :ctId);")
                        .setParameter("price", price)
                        .setParameter("ctId", ctId).executeUpdate();

                // Update ticket quantity
                manager.createNativeQuery("update createticket ct set ct.available = ct.available - " +
                                "(select quantity from cart where id = :cartId) where ct.id = :ctId")
                        .setParameter("cartId", i)
                        .setParameter("ctId", ctId).executeUpdate();

                // Update user's points
                manager.createNativeQuery("update users u set u.point = u.point + :point where u.id = :uid")
                        .setParameter("point", ((Double) (response.getAmount()/10000.0)).intValue())
                        .setParameter("uid", response.getUId()).executeUpdate();
            }

            for(Object[] row : eventMail){
                // Generate ticket
                UUID id = UUID.randomUUID();
                manager.createNativeQuery("insert into ticketrelease (`id`, `status`, `Cart_id`) " +
                                "values (?,?,?)")
                        .setParameter(1, id.toString())
                        .setParameter(2, 1)
                        .setParameter(3, row[2]).executeUpdate();


                try{
                    String qrCodeTxt = QRCodeService.generateQRCode(id.toString());

                    manager.createNativeQuery("update ticketrelease t set t.qrcode = :qrcode " +
                                    "where t.Cart_id = :cartId").setParameter("qrcode", qrCodeTxt)
                            .setParameter("cartId", row[2]).executeUpdate();


                    qrcodeService.sendQR(qrCodeTxt, response.getEmail(), eventMail);
                } catch (Exception e) {
                    manager.createNativeQuery("update ticketrelease t set t.status = :status " +
                                    "where t.Cart_id = :cartId").setParameter("status", "Chưa tạo được mã")
                            .setParameter("cartId", row[2]).executeUpdate();
                    throw new RuntimeException(e);
                }
            }
        }else{
            paymentStatus = "Thanh toán thất bại";
            newPayment.setParameter(2, paymentStatus).executeUpdate();
        }
        return paymentStatus;
    }

    @Override
    @PreAuthorize("hasRole('USER') || hasRole('ADMIN')")
    public List<HistoryResponse> myHistory() {
        Query query = manager.createNativeQuery("select tr.id, e.name, " +
                "date_format(str_to_date(e.start_time, '%Y-%m-%d %H:%i:%s'), '%Y-%m-%d'), " +
                "date_format(str_to_date(e.start_time, '%Y-%m-%d %H:%i:%s'), '%H:%i'), " +
                "date_format(str_to_date(e.end_time, '%Y-%m-%d %H:%i:%s'), '%H:%i'), " +
                "date_format(str_to_date(p.payment_time, '%Y-%m-%d %H:%i:%s'), '%Y-%m-%d'), " +
                "Concat(e.city, ', ', e.location)" +
                "from ticketrelease tr " +
                "join cart c on FIND_IN_SET(c.id, tr.Cart_id) > 0 " +
                "join invoice i on i.Cart_id = c.id " +
                "join payment p on i.Payment_id = p.id " +
                "join createticket ct on c.CreateTicket_id = ct.id " +
                "join events e on ct.Events_id = e.id " +
                "where c.Users_id = :uId", HistoryResponse.class)
                .setParameter("uId", myInfor().getId());

        return query.getResultList();
    }

    @Override
    @PreAuthorize("hasRole('USER') || hasRole('ADMIN')")
    public HistoryResponseDetail getHistoryResponseDetail(String id) {
        Query query = manager.createNativeQuery("select tr.id, tr.qrcode, e.name, " +
                        "date_format(str_to_date(e.start_time, '%Y-%m-%d %H:%i:%s'), '%Y-%m-%d %H:%i'), " +
                        "date_format(str_to_date(e.start_time, '%Y-%m-%d %H:%i:%s'), '%H:%i'), " +
                        "date_format(str_to_date(e.end_time, '%Y-%m-%d %H:%i:%s'), '%H:%i'), " +
                        "Concat(e.city, ', ', e.location), ct.type_name, c.quantity, p.payment_amount/100 " +
                        "from cart c " +
                        "JOIN ticketrelease tr ON FIND_IN_SET(c.id, tr.Cart_id) > 0 " +
                        "join createticket ct on c.CreateTicket_id = ct.id " +
                        "join invoice i on i.Cart_id = c.id " +
                        "join payment p on i.Payment_id = p.id " +
                        "join events e on ct.Events_id = e.id " +
                        "where tr.id= :id")
                .setParameter("id", id);

        List<Object[]> response = query.getResultList();
        List<Map<String, Object>> map = new ArrayList<>();

        HistoryResponseDetail detail = new HistoryResponseDetail();
        detail.setTicketId(response.get(0)[0].toString());
        detail.setQrcode(response.get(0)[1].toString());
        detail.setEventName(response.get(0)[2].toString());
        detail.setEventDate(response.get(0)[3].toString());
        detail.setEventStartTime(response.get(0)[4].toString());
        detail.setEventEndTime(response.get(0)[5].toString());
        detail.setLocation(response.get(0)[6].toString());
        detail.setTotalPrice(Double.parseDouble(response.get(0)[9].toString()));

        for(Object[] row : response){
            Map<String, Object> myMap = new HashMap<>();
            myMap.put("typeName", row[7]);
            myMap.put("quantity", row[8]);

            map.add(myMap);
        }
        detail.setTypeTicket(map);
        return detail;
    }

    public boolean checkMerchantCondition(String license){
        Query query = manager.createNativeQuery("select m.license from merchants m where m.license = :license")
                .setParameter("license", license);

        List<Object[]> result = query.getResultList();
        if(result.size() == 0){
            return true;
        }
        return false;
    }
}
