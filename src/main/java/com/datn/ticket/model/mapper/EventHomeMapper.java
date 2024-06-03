package com.datn.ticket.model.mapper;

import com.datn.ticket.dto.EventHome;

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
}
