package com.colonygenesis.ui;

import com.colonygenesis.map.Tile;
import javafx.event.Event;
import javafx.event.EventType;

public class TileEvent extends Event {
    public static final EventType<TileEvent> ANY = new EventType<>(Event.ANY, "TILE_EVENT");
    public static final EventType<TileEvent> TILE_SELECTED = new EventType<>(ANY, "TILE_SELECTED");
    public static final EventType<TileEvent> TILE_HOVER = new EventType<>(ANY, "TILE_HOVER");
    public static final EventType<TileEvent> TILE_ACTION = new EventType<>(ANY, "TILE_ACTION");

    private final Tile tile;

    public TileEvent(EventType<TileEvent> eventType, Tile tile) {
        super(eventType);
        this.tile = tile;
    }

    public Tile getTile() {
        return tile;
    }
}