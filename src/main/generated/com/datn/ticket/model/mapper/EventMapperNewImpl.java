package com.datn.ticket.model.mapper;

import com.datn.ticket.model.CreateTickets;
import com.datn.ticket.model.dto.response.TicketTypeResponse;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2024-05-13T14:37:27+0700",
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
}
