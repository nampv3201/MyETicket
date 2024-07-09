package com.datn.ticket.repository;

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
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Repository;

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

        // check if registered
        Query query = manager.createNativeQuery("select * from account_has_role ar where ar.Account_id = :account_id " +
                "and ar.role_id = 2").setParameter("account_id", authentication.getName());
        if (query.getResultList().size() > 0) {
            log.info(authentication.getName());
            return ApiResponse.<AuthenticationResponse>builder().code(ErrorCode.ROLE_HAS_REGISTERED.getCode())
                    .message(ErrorCode.ROLE_HAS_REGISTERED.getMessage()).build();
        }

        // check condition
        if(checkMerchantCondition(signUpRequest.getSignUpRequest().getLicense()) == false){
            log.info(signUpRequest.getSignUpRequest().getLicense());
            return ApiResponse.builder()
                    .code(ErrorCode.LICENSE_HAS_REGISTERED.getCode())
                    .message(ErrorCode.LICENSE_HAS_REGISTERED.getMessage())
                    .build();
        }

        // Create merchant
        manager.createNativeQuery("insert into merchants " +
                        "(`name`, `address`, `phone`, `license`, `Account_id`) " +
                        "values (:name, :address, :phone, :license, :Account_id)")
                .setParameter("name", signUpRequest.getSignUpRequest().getName())
                .setParameter("address", signUpRequest.getSignUpRequest().getAddress())
                .setParameter("phone", signUpRequest.getSignUpRequest().getPhone())
                .setParameter("license", signUpRequest.getSignUpRequest().getLicense())
                .setParameter("Account_id", authentication.getName()).executeUpdate();

        // Add role
        manager.createNativeQuery("insert into account_has_role " +
                        "(`Account_id`, `role_id`, `status`) " +
                        "values (:account_id, 2, 1)")
                .setParameter("account_id", authentication.getName()).executeUpdate();

        AuthenticationResponse res = accountService.refreshToken(RefreshRequest.builder()
                .token(signUpRequest.getToken())
                .build());

        return ApiResponse.<AuthenticationResponse>builder()
                .message("Đăng ký thành công")
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
        String query = "select c.id as Cart_id, ct.id as TicketType_id, ct.type_name, c.quantity, ct.available, c.cost, e.id as Event_id, e.name " +
                "from cart c " +
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

    @SneakyThrows
    @Override
    @Transactional
    @PreAuthorize("hasRole('USER') || hasRole('ADMIN')")
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
            newPayment.setParameter(2, "Thanh toán thành công").executeUpdate();

            // Thông tin events
            List<Object[]> eventMails = manager.createNativeQuery("select e.id, e.name, " +
                            "group_concat(c.id SEPARATOR ', ') as tType, " +
                            "group_concat(concat(c.quantity, ' vé loại ', ct.type_name) SEPARATOR ', ') as ticket " +
                            "from Cart c " +
                            "join createticket ct on c.CreateTicket_id = ct.id " +
                            "join events e on ct.Events_id = e.id " +
                            "where c.id in :cartId " +
                            "group by e.id, e.name")
                    .setParameter("cartId", response.getCartIds()).getResultList();

            for(Object[] row : eventMails){
                Map<String, String> qrCode = new HashMap<>();
                int[] cartIds = Arrays.stream(row[2].toString().split(", ")).mapToInt(Integer::parseInt).toArray();

                // Cập nhật
                for(int cartId : cartIds){
                    Object[] obj = (Object[]) manager.createNativeQuery("select c.CreateTicket_id, c.cost, ct.type_name, c.quantity " +
                                    "from Cart c " +
                                    "join createTicket ct on c.CreateTicket_id = ct.id " +
                                    "where c.id = :cartId")
                            .setParameter("cartId", cartId).getSingleResult();

                    int ctId = Integer.parseInt(String.valueOf(obj[0]));
                    double price = Double.parseDouble(String.valueOf(obj[1]));
                    String ctName = (String)obj[2];
                    int quantity = Integer.parseInt(String.valueOf(obj[3]));

                    // Insert invoice
                    manager.createNativeQuery("insert into invoice (`Cart_id`, `Payment_id`) " +
                                    "values (:cartId, :paymentId)").setParameter("cartId",cartId)
                            .setParameter("paymentId", response.getBankTranNo()).executeUpdate();

                    // Update cart status
                    manager.createNativeQuery("update cart c set c.status = 1 where c.id = :cartId")
                            .setParameter("cartId", cartId).executeUpdate();

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
                            .setParameter("cartId", cartId)
                            .setParameter("ctId", ctId).executeUpdate();

                    // Update user's points
                    manager.createNativeQuery("update users u set u.point = u.point + :point where u.id = :uid")
                            .setParameter("point", ((Double) (response.getAmount()/10000.0)).intValue())
                            .setParameter("uid", response.getUId()).executeUpdate();

                    for(int i = 0; i < quantity; i++){
                        // Generate ticket
                        UUID id = UUID.randomUUID();
                        try{
                            String qrCodeTxt = QRCodeService.generateQRCode(id.toString());
                            manager.createNativeQuery("insert into ticketrelease (`id`, `qrcode`, `status`, `Cart_id`) " +
                                            "values (?,?,?,?)")
                                    .setParameter(1, id.toString())
                                    .setParameter(2, qrCodeTxt)
                                    .setParameter(3, "Chưa sử dụng")
                                    .setParameter(4, cartId).executeUpdate();

                            qrCode.put(id.toString(), qrCodeTxt);
                        } catch (Exception e) {
                            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
                        }
                    }
                }

                try {
                    qrcodeService.sendQR(qrCode, response.getEmail(), row);
                } catch (Exception e) {
                    throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
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
    public UserTicketResponse myHistory() {
        List<HistoryResponse> history = new ArrayList<>();

        Query getPayment = manager.createNativeQuery("select p.id, date_format(str_to_date(p.payment_time, '%Y-%m-%d %H:%i:%s'), '%Y-%m-%d') as paymentTime " +
                "from payment p " +
                "where p.Users_id = :uId " +
                "order by paymentTime DESC").setParameter("uId", myInfor().getId());

        List<Object[]> pResponse = getPayment.getResultList();
        for(Object[] p : pResponse){
            HistoryResponse paymentResponse = new HistoryResponse();
            paymentResponse.setPaymentId(String.valueOf(p[0]));
            paymentResponse.setPaymentTime(String.valueOf(p[1]));

            Query getEventHis = manager.createNativeQuery("select distinct e.id as eventId, e.name, " +
                    "date_format(str_to_date(e.start_time, '%Y-%m-%d %H:%i:%s'), '%Y-%m-%d'), " +
                    "date_format(str_to_date(e.start_time, '%Y-%m-%d %H:%i:%s'), '%H:%i'), " +
                    "date_format(str_to_date(e.end_time, '%Y-%m-%d %H:%i:%s'), '%H:%i'), " +
                    "Concat(e.city, ', ', e.location) as location " +
                    "from events e " +
                    "join createticket ct on ct.Events_id = e.id " +
                    "join cart c on c.CreateTicket_id = ct.id " +
                    "join invoice i on i.Cart_id = c.id " +
                    "join payment p on i.Payment_id = p.id " +
                    "where p.id = :pId", HistoryEventResponse.class).setParameter("pId", String.valueOf(p[0]));

            paymentResponse.setEvents(getEventHis.getResultList());

            history.add(paymentResponse);
        }
        return UserTicketResponse.builder()
                .count(history.size())
                .ticket(history)
                .build();
    }

    @Override
    @PreAuthorize("hasRole('USER') || hasRole('ADMIN')")
    public List<HistoryResponseDetail> getHistoryResponseDetail(String pId, String eId) {
        Query query = manager.createNativeQuery("select tr.id, tr.qrcode, ct.type_name, tr.status " +
                        "from invoice i " +
                        "join payment p on p.id = i.Payment_id " +
                        "join cart c on c.id = i.Cart_id " +
                        "join createticket ct on ct.id = c.CreateTicket_id " +
                        "join events e on e.id = ct.Events_id " +
                        "join ticketrelease tr on tr.Cart_id = c.id " +
                        "where e.id = :eId and p.id = :pId", HistoryResponseDetail.class)
                .setParameter("eId", eId)
                .setParameter("pId", pId);

        return (List<HistoryResponseDetail>) query.getResultList();
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