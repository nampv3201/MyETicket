package com.datn.ticket.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DirectPaymentRequest {
    String email;
    int methodId;
    List<AddToCartRequest> cartId;
}
