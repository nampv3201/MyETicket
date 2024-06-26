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
    String bankTranNo;
    String responseCode;
    Date paymentDate;
    double amount;
    int uId;
    int methodId;

    String email;
}
