package com.datn.ticket.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentHistoryResponse {
    String paymentId;
    String paymentTime;
    String paymentStatus;
    String paymentAmount;
    int uId;
}
