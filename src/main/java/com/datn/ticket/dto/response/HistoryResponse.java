package com.datn.ticket.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class HistoryResponse {
    String ticketId;
    String eventName;
    String eventDate;
    String eventStartTime;
    String eventEndTime;
    String location;
    String ticketName;
}
