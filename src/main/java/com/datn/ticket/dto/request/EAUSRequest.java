package com.datn.ticket.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EAUSRequest {
    String start_time;
    String end_time;
    String start_booking;
    String end_booking;
    List<TicketTypeRequest> ticketTypeRequests;
}
