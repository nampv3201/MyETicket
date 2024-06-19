package com.datn.ticket.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdminGetAllEventResponse {
    int eventId;
    String eventName;
    String location;
    String category;
    String eventTime;
    String totalTicket;
    String available;
    String minPrice;
    String status;
    int mId;
    String mName;
}
