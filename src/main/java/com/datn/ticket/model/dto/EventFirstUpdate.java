package com.datn.ticket.model.dto;

import com.datn.ticket.model.Categories;
import lombok.Getter;

import java.util.List;

@Getter
public class EventFirstUpdate {
    private String eventName, eventDescription, eventLocation, eventBanner;
    private int eventMaxLimit;
    private List<Categories> categoriesList;

    public EventFirstUpdate() {
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public void setEventDescription(String eventDescription) {
        this.eventDescription = eventDescription;
    }

    public void setEventLocation(String eventLocation) {
        this.eventLocation = eventLocation;
    }

    public void setEventBanner(String eventBanner) {
        this.eventBanner = eventBanner;
    }

    public void setEventMaxLimit(int eventMaxLimit) {
        this.eventMaxLimit = eventMaxLimit;
    }

    public void setCategoriesList(List<Categories> categoriesList) {
        this.categoriesList = categoriesList;
    }
}
