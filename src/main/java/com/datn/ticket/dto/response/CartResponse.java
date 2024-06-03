package com.datn.ticket.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CartResponse {
    int cartId;
    int ticketTypeId;
    String typeName;
    int quantity;
    int available;
    double price;
    int eventId;
    String eventName;
}
