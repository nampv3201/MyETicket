package com.datn.ticket.model;


import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
@Table(name = "events_has_categories")
public class EventCat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "Events_id", referencedColumnName = "id")
    private Events events;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "Categories_id", referencedColumnName = "id")
    private Categories categories;

    public EventCat() {
    }

    public EventCat(Events events, Categories categories) {
        this.events = events;
        this.categories = categories;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setEvents(Events events) {
        this.events = events;
    }

    public void setCategories(Categories categories) {
        this.categories = categories;
    }
}
