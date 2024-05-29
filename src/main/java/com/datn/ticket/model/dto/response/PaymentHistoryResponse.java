package com.datn.ticket.model.dto.response;

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
    Date paymentTime;
    String paymentStatus;
    double paymentAmount;
    int uId;
}
