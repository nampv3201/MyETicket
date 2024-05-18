package com.datn.ticket.model.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EAFRequest {
    String eventName;
    String eventDescription;
    String eventCity;
    String eventLocation;
    String eventBanner;
    List<Integer> categories;
    int eventLimit;
}
