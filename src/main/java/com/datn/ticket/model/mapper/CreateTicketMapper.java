package com.datn.ticket.model.mapper;

import com.datn.ticket.model.CreateTickets;
import com.datn.ticket.model.dto.CreateTicketsDTO;

public class CreateTicketMapper {
    public static CreateTicketsDTO createTicketsDTO(CreateTickets tickets){
        CreateTicketsDTO ticketsDTO = new CreateTicketsDTO();
        ticketsDTO.setId(tickets.getId());
        ticketsDTO.setType_name(tickets.getType_name());
        ticketsDTO.setAvailable(tickets.getAvailable());
        ticketsDTO.setCost(tickets.getPrice());

        return ticketsDTO;
    }
}
