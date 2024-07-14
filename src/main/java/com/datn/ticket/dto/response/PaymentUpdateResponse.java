package com.datn.ticket.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentUpdateResponse {
    String pId;
    String amount;
    String status;
    String pTime;
}
