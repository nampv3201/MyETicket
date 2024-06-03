package com.datn.ticket.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentHistoryResponseDetail {
    String paymentId;
    String paymentTime;
    String paymentStatus;
    List<Object> cart;
    int uId;
    String uName;
}
