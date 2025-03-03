package com.colonygenesis.event.events;

import com.colonygenesis.event.GameEvent;
import com.colonygenesis.map.Tile;

public class TileEvent extends GameEvent {
    private final Tile tile;
    private final TileUpdateType updateType;

    public TileEvent(Object source, Tile tile, TileUpdateType updateType) {
        super(source, EventType.TILE_UPDATED);
        this.tile = tile;
        this.updateType = updateType;
    }

    public Tile getTile() {
        return tile;
    }

    public TileUpdateType getUpdateType() {
        return updateType;
    }

    public enum TileUpdateType {
        REVEALED,
        TERRAIN_CHANGED,
        RESOURCE_FOUND,
        BUILDING_ADDED,
        BUILDING_REMOVED,
        EFFECT_APPLIED,
        EFFECT_REMOVED
    }
}