package com.datn.ticket.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AMerchantResponse {
    int mId;
    String username;
    String mName;
    String address;
    int status;
}
