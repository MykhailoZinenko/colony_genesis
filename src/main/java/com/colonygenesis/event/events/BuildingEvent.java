package com.colonygenesis.event.events;

import com.colonygenesis.building.Building;
import com.colonygenesis.event.GameEvent;
import com.colonygenesis.map.Tile;

public class BuildingEvent extends GameEvent {
    private final Building building;
    private final Tile tile;

    public BuildingEvent(Object source, EventType type, Building building, Tile tile) {
        super(source, type);
        this.building = building;
        this.tile = tile;
    }

    public Building getBuilding() {
        return building;
    }

    public Tile getTile() {
        return tile;
    }

    // Factory methods for common building events
    public static BuildingEvent placed(Object source, Building building, Tile tile) {
        return new BuildingEvent(source, EventType.BUILDING_PLACED, building, tile);
    }

    public static BuildingEvent completed(Object source, Building building, Tile tile) {
        return new BuildingEvent(source, EventType.BUILDING_COMPLETED, building, tile);
    }

    public static BuildingEvent activated(Object source, Building building, Tile tile) {
        return new BuildingEvent(source, EventType.BUILDING_ACTIVATED, building, tile);
    }

    public static BuildingEvent deactivated(Object source, Building building, Tile tile) {
        return new BuildingEvent(source, EventType.BUILDING_DEACTIVATED, building, tile);
    }

    // Add the "removed" factory method
    public static BuildingEvent removed(Object source, Building building, Tile tile) {
        return new BuildingEvent(source, EventType.BUILDING_REMOVED, building, tile);
    }
}