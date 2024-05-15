package com.datn.ticket.model.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EUFRequest {
    String eventName;
    String eventDescription;
    String eventLocation;
    String eventBanner;
    int eventLimit;
}
