package com.datn.ticket.model.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EUSRequest {
    String start_time;
    String end_time;
    String start_booking;
    String end_booking;
    List<TicketTypeRequest> ticketTypeRequests;
}
