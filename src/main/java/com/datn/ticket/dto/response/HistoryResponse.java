package com.datn.ticket.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class HistoryResponse {
    String paymentId;
    String paymentTime;
    List<HistoryEventResponse> events;
}
