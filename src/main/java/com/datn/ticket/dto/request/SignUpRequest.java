package com.datn.ticket.dto.request;

import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SignUpRequest {
    @Size(min = 8, message = "Yêu cầu ít nhất 8 ký tự")
    String username;
    @Size(min = 8, message = "Yêu cầu ít nhất 8 ký tự")
    String password;
    List<Integer> role;
}
