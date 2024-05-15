package com.datn.ticket.model.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AccountResponse {
    int id;
    String username;
    int status;
    Set<String> roles;
}
