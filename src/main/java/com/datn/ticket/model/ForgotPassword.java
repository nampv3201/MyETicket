package com.datn.ticket.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "forgotpassword")

public class ForgotPassword {
    @Id
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "Account_id", referencedColumnName = "id")
    Accounts accounts;

    @Column(name = "OTP")
    String OTP;

    @Column(name = "expirationTime")
    Date expirationTime;
}
