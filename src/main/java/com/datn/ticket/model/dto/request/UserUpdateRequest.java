package com.datn.ticket.model.dto.request;

import com.datn.ticket.util.AgeConstraint;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserUpdateRequest {
    String name;
    String address;
    String phone;

    @AgeConstraint(min = 16, message = "Bạn cần trên 16 tuổi")
    int age;
}
