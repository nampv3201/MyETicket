package com.datn.ticket.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Entity
@Table(name = "ticketrelease")
@Getter
@Setter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TicketRelease {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    UUID id;

    @Column(name = "qrcode")
    String qrcode;

    @Column(name = "status")
    String status;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "Cart_id", referencedColumnName ="id")
    Cart cart;

    public TicketRelease() {
    }
}
