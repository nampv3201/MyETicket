package com.datn.ticket.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.web.multipart.MultipartFile;

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
    List<Integer> categories;
    int eventLimit;
}
