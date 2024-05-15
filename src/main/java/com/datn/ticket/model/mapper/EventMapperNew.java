package com.datn.ticket.model.mapper;

import com.datn.ticket.model.CreateTickets;
import com.datn.ticket.model.dto.response.TicketTypeResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper()
public interface EventMapperNew {
    EventMapperNew INSTANCE = Mappers.getMapper(EventMapperNew.class);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "type_name", target = "type_name")
    @Mapping(source = "price", target = "price")
    @Mapping(source = "available", target = "available")
    TicketTypeResponse toTicketTypeResponse(CreateTickets createTicket);
    List<TicketTypeResponse> toTicketTypeResponseList(List<CreateTickets> createTickets);
}
