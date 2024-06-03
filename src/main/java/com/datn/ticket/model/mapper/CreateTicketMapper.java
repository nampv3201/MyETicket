package com.datn.ticket.model.mapper;

import com.datn.ticket.model.CreateTickets;
import com.datn.ticket.dto.response.CreateTicketsResponse;

public class CreateTicketMapper {
    public static CreateTicketsResponse createTicketsDTO(CreateTickets tickets){
        CreateTicketsResponse ticketsDTO = new CreateTicketsResponse();
        ticketsDTO.setId(tickets.getId());
        ticketsDTO.setType_name(tickets.getType_name());
        ticketsDTO.setAvailable(tickets.getAvailable());
        ticketsDTO.setPrice(tickets.getPrice());

        return ticketsDTO;
    }
}
