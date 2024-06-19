package com.datn.ticket.dto;

import com.datn.ticket.dto.response.CreateTicketsResponse;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventDTO {
    int id;

    String name;
    String description;
    String city;
    String location;
    Date start_time;
    Date end_time;
    Date start_booking;
    Date end_booking;
    int max_limit;
    String banner;
    int merchantId;
    String merchantName;
    List<String> categories;
    String status;

    List<CreateTicketsResponse> createTicketsResponseList;

    List<EventHome> suggestEvents;
}
