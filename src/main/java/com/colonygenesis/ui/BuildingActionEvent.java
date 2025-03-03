package com.colonygenesis.ui;

import com.colonygenesis.building.Building;
import javafx.event.Event;
import javafx.event.EventType;

public class BuildingActionEvent extends Event {
    public static final EventType<BuildingActionEvent> ANY =
            new EventType<>(Event.ANY, "BUILDING_ACTION");
    public static final EventType<BuildingActionEvent> TOGGLE_ACTIVE =
            new EventType<>(ANY, "TOGGLE_ACTIVE");
    public static final EventType<BuildingActionEvent> DEMOLISH =
            new EventType<>(ANY, "DEMOLISH");
    public static final EventType<BuildingActionEvent> SHOW_INFO = new EventType<>("SHOW_INFO");

    private final Building building;

    public BuildingActionEvent(EventType<BuildingActionEvent> eventType, Building building) {
        super(eventType);
        this.building = building;
    }

    public Building getBuilding() {
        return building;
    }
}