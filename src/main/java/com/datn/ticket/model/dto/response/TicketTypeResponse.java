package com.datn.ticket.model.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TicketTypeResponse {
    int id;
    String type_name;
    double price;
    int available;
}
