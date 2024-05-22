package com.datn.ticket.model.dto.response;

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
public class HistoryResponseDetail {
    String ticketId;
    String qrcode;
    String eventName;
    String ticketName;
    String eventDate;
    String eventStartTime;
    String eventEndTime;
    String location;
    int quantity;
    double totalPrice;
}
