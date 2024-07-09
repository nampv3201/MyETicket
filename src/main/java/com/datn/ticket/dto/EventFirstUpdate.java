package com.datn.ticket.dto;

import com.datn.ticket.model.Categories;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventFirstUpdate {
    private String eventName, eventDescription, eventCity, eventLocation;
    private String eventBanner;
    private int eventMaxLimit;
    private List<Categories> categoriesList;
}
