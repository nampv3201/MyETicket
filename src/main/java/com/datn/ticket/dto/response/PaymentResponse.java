package com.datn.ticket.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentResponse {
    List<Integer> cartIds;
    String vnp_TxnRef;
    String responseCode;
    String paymentDate;
    double amount;
    int uId;
    String email;
}
