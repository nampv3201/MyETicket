package com.datn.ticket.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MerchantSignUpRequest {
    String name;
    String address;
    String phone;
    String license;
}
