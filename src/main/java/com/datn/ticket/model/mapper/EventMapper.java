package com.datn.ticket.model.mapper;

import com.datn.ticket.dto.EventHome;
import com.datn.ticket.model.Events;
import com.datn.ticket.dto.response.CreateTicketsResponse;
import com.datn.ticket.dto.EventDTO;

import java.util.Base64;
import java.util.List;

public class EventMapper {
    public static EventDTO eventDTO(Events events, List<String> categories
            , List<CreateTicketsResponse> createTicketsResponseList, List<EventHome> suggestEvent){

        EventDTO eventDTO = new EventDTO();
        eventDTO.setId(events.getId());
        eventDTO.setName(events.getName());
        eventDTO.setDescription(events.getDescription());
        eventDTO.setCity(events.getCity());
        eventDTO.setLocation(events.getLocation());
        eventDTO.setStart_time(events.getStart_time());
        eventDTO.setEnd_time(events.getEnd_time());
        eventDTO.setStart_booking(events.getStart_booking());
        eventDTO.setEnd_booking(events.getEnd_booking());
        eventDTO.setMax_limit(events.getMax_limit());
        eventDTO.setBanner(events.getBanner());
        eventDTO.setMerchantId(events.getMerchants().getId());
        eventDTO.setMerchantName(events.getMerchants().getName());
        eventDTO.setCreateTicketsResponseList(createTicketsResponseList);
        eventDTO.setCategories(categories);
        eventDTO.setStatus(events.getStatus());
        eventDTO.setSuggestEvents(suggestEvent);

        return eventDTO;
    }
}
