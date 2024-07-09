package com.datn.ticket.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EUFRequest {
    String eventName;
    String eventDescription;
    String eventCity;
    String eventLocation;
    MultipartFile eventBanner;
    int eventLimit;
}
