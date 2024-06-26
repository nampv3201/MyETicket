package com.datn.ticket.model.mapper;

import com.datn.ticket.dto.EventHome;
import com.datn.ticket.dto.response.AdminGetAllEventResponse;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class EventHomeMapper {

    public static List<EventHome> eventHomeDTO(List<Object[]> events){
        List<EventHome> eventHomes = new ArrayList<>();
        for(Object[] e : events){
            EventHome eventHome = new EventHome();
            eventHome.setId((Integer) e[0]);
            eventHome.setName((String) e[1]);
            eventHome.setBanner((String) e[2]);
            eventHome.setCity((String) e[3]);
            eventHome.setLocation((String) e[4]);
            eventHome.setStartDate(((Timestamp) e[5]).toLocalDateTime().toLocalDate());
            eventHome.setMinPrice(((Long) e[6]).doubleValue());
            eventHomes.add(eventHome);
        }

        return eventHomes;
    }

    public static List<AdminGetAllEventResponse> adminAllEvents(List<Object[]> events){
        List<AdminGetAllEventResponse> result = new ArrayList<>();
        for(Object[] e : events){
            AdminGetAllEventResponse response = new AdminGetAllEventResponse();
            response.setEventId((Integer) e[0]);
            response.setEventName((String) e[1]);
            response.setLocation((String) e[2]);
            response.setCategory((String) e[3]);
            response.setEventTime((String) e[4]);
            response.setTotalTicket(((BigDecimal) e[5]).toPlainString());
            response.setAvailable(((BigDecimal) e[6]).toPlainString());
            response.setMinPrice(((Long) e[7]).toString());
            response.setStatus((String) e[8]);
            response.setMId((int) e[9]);
            response.setMName((String) e[10]);

            result.add(response);
        }

        return result;
    }
}
