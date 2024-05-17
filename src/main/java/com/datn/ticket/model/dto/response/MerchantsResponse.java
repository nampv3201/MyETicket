package com.datn.ticket.model.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MerchantsResponse {
    int id;
    String name, address, phone, license, description;

}
