package com.datn.ticket.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class HistoryEventResponse {
    int eventId;
    String eventName;
    String eDate;
    String eStart;
    String eEnd;
    String location;
}
