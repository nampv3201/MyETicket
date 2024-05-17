package com.datn.ticket.model.dto.request;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MURequest {
    String name;
    String address;
    String description;
    String phone;
}
