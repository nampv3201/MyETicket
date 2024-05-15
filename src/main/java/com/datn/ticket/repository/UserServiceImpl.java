package com.datn.ticket.repository;

import com.datn.ticket.model.Cart;
import com.datn.ticket.model.Users;
import com.datn.ticket.model.dto.request.UpdateCartRequest;
import com.datn.ticket.model.dto.response.CartResponse;
import com.datn.ticket.model.dto.response.PaymentResponse;
import com.datn.ticket.service.UserService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Slf4j
public class UserServiceImpl implements UserService {
    private final EntityManager manager;

    @Autowired
    public UserServiceImpl(EntityManager entityManager){
        this.manager = entityManager;
    }


    @Override
    @Transactional
    public void updateUser(Users user) {
        manager.merge(user);
    }

    @Override
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
    public List<CartResponse> myCart() {
        String query = "select c.id as Cart_id, ct.id as TicketType_id, ct.type_name, c.quantity, c.cost, e.id as Event_id, e.name from cart c " +
                "join createticket ct on c.CreateTicket_id = ct.id " +
                "join events e on ct.Events_id = e.id " +
                "where c.Users_id = :UID and c.status = 0";
        log.info("UID: {}", SecurityContextHolder.getContext().getAuthentication().getName());
        return manager.createNativeQuery(query, CartResponse.class)
                .setParameter("UID", myInfor().getId())
                .getResultList();
    }

    @Override
    @Transactional
    public void removeFromCart(List<Integer> cartId) {
        for(int i : cartId){
            manager.createNativeQuery("delete from cart where id = :id")
                   .setParameter("id", i)
                   .executeUpdate();
        }
    }

    @Override
    @Transactional
    public void updateCart(UpdateCartRequest request) {
        double result = ((Long) manager.createNativeQuery("select ct.price from createticket ct " +
                        "join cart c on c.CreateTicket_id = ct.id " +
                        "where c.id =:id")
                .setParameter("id", request.getCartId()).getSingleResult()).doubleValue();
        log.info("Cost {}", result);
        Double price = request.getUpdateNumber() * result;
        manager.createNativeQuery("update cart set cart.quantity = :quantity, cart.cost = :cost where cart.id = :id")
                .setParameter("quantity", request.getUpdateNumber())
                .setParameter("cost", price)
                .setParameter("id", request.getCartId())
                .executeUpdate();
    }

    @Override
    @Transactional
    public void payment(PaymentResponse response) {
        String paymentStatus;
        Query newPayment = manager.createNativeQuery("insert into payment (`id`, `payment_status`, `payment_time`, `payment_amount`, `PaymentMethod_id`, `Users_id`) " +
                "values (?,?,?,?,?,?)")
                .setParameter(1, response.getBankTranNo())
                .setParameter(3, response.getPaymentDate())
                .setParameter(4, response.getAmount())
                .setParameter(5, 1)
                .setParameter(6, response.getUId());

        if(response.getResponseCode().equals("00")){
            paymentStatus = "Thanh toán thành công";
            newPayment.setParameter(2, paymentStatus).executeUpdate();

            // Tạo invoice
            for(int i : response.getCartIds()){
                Object[] obj = (Object[]) manager.createNativeQuery("select c.CreateTicket_id, c.cost from Cart c where c.id = :cartId")
                                .setParameter("cartId", i).getSingleResult();

                int ctId = (int) obj[0];
                double price = (double)obj[1];

                manager.createNativeQuery("insert into invoice (`Cart_id`, `Payment_id`) " +
                        "values (:cartId, :paymentId)").setParameter("cartId",i)
                        .setParameter("paymentId", response.getBankTranNo()).executeUpdate();

                manager.createNativeQuery("update cart set status = 1 where id = :cartId")
                        .setParameter("cartId", i).executeUpdate();

                manager.createNativeQuery("update revenue r set r.totalRevenue = r.totalRevenue + :price where r.Events_id in " +
                        "(SELECT ct.Events_id " +
                        "FROM createticket ct " +
                        "JOIN events e ON ct.Events_id = e.id " +
                        "WHERE ct.id = :ctId);")
                        .setParameter("price", price)
                        .setParameter("ctId", ctId).executeUpdate();
            }
        }else{
            paymentStatus = "Thanh toán thất bại";
            newPayment.setParameter(2, paymentStatus).executeUpdate();
        }
    }
}
