package com.datn.ticket.model.mapper;

import com.datn.ticket.dto.response.CreateTicketsResponse;
import com.datn.ticket.dto.response.TicketTypeResponse;
import com.datn.ticket.model.CreateTickets;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2024-07-07T15:55:02+0700",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 19.0.1 (Oracle Corporation)"
)
public class EventMapperNewImpl implements EventMapperNew {

    @Override
    public TicketTypeResponse toTicketTypeResponse(CreateTickets createTicket) {
        if ( createTicket == null ) {
            return null;
        }

        TicketTypeResponse.TicketTypeResponseBuilder ticketTypeResponse = TicketTypeResponse.builder();

        ticketTypeResponse.id( createTicket.getId() );
        ticketTypeResponse.type_name( createTicket.getType_name() );
        ticketTypeResponse.price( createTicket.getPrice() );
        ticketTypeResponse.available( createTicket.getAvailable() );

        return ticketTypeResponse.build();
    }

    @Override
    public List<TicketTypeResponse> toTicketTypeResponseList(List<CreateTickets> createTickets) {
        if ( createTickets == null ) {
            return null;
        }

        List<TicketTypeResponse> list = new ArrayList<TicketTypeResponse>( createTickets.size() );
        for ( CreateTickets createTickets1 : createTickets ) {
            list.add( toTicketTypeResponse( createTickets1 ) );
        }

        return list;
    }

    @Override
    public CreateTicketsResponse createTicketDTO(CreateTickets createTickets) {
        if ( createTickets == null ) {
            return null;
        }

        CreateTicketsResponse createTicketsResponse = new CreateTicketsResponse();

        createTicketsResponse.setId( createTickets.getId() );
        createTicketsResponse.setType_name( createTickets.getType_name() );
        createTicketsResponse.setPrice( createTickets.getPrice() );
        createTicketsResponse.setAvailable( createTickets.getAvailable() );

        return createTicketsResponse;
    }
}
