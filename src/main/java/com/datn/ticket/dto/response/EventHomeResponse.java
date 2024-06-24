package com.datn.ticket.dto.response;
import com.datn.ticket.dto.EventHome;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventHomeResponse {
    int count;
    List<EventHome> events;
}
