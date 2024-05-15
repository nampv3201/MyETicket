package com.datn.ticket.model.dto.response;

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
}
