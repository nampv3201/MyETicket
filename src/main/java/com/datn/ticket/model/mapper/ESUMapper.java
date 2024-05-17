package com.datn.ticket.model.mapper;

import com.datn.ticket.model.Events;
import com.datn.ticket.model.dto.response.CreateTicketsResponse;
import com.datn.ticket.model.dto.EventSecondUpdate;

import java.util.List;

public class ESUMapper {
    public static EventSecondUpdate cast(Events events, List<CreateTicketsResponse> tickets){
        EventSecondUpdate esu = new EventSecondUpdate();
        esu.setStart_time(events.getStart_time());
        esu.setEnd_time(events.getEnd_time());
        esu.setStart_booking(events.getStart_booking());
        esu.setEnd_booking(events.getEnd_booking());
        esu.setCreateTickets(tickets);
        return esu;
    }
}
