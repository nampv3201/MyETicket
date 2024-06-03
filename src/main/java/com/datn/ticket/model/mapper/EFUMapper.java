package com.datn.ticket.model.mapper;

import com.datn.ticket.model.Categories;
import com.datn.ticket.model.Events;
import com.datn.ticket.dto.EventFirstUpdate;

import java.util.List;

public class EFUMapper {
    public static EventFirstUpdate cast(Events events, List<Categories> categories){
        EventFirstUpdate efu = new EventFirstUpdate();
        efu.setEventName(events.getName());
        efu.setEventDescription(events.getDescription());
        efu.setEventCity(events.getCity());
        efu.setEventLocation(events.getLocation());
        efu.setEventBanner(events.getBanner());
        efu.setEventMaxLimit(events.getMax_limit());
        efu.setCategoriesList(categories);

        return efu;
    }
}
