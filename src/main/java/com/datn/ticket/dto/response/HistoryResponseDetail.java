package com.datn.ticket.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class HistoryResponseDetail {
    String ticketId;
    String qrcode;
    String eventName;
    String eventDate;
    String eventStartTime;
    String eventEndTime;
    String location;
    List<Map<String, Object>> typeTicket;
    double totalPrice;
}
