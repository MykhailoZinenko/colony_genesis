package com.colonygenesis.event;

import java.time.Instant;
import java.util.UUID;

public abstract class GameEvent {
    private final UUID id;
    private final Instant timestamp;
    private final Object source;
    private final EventType type;

    public GameEvent(Object source, EventType type) {
        this.id = UUID.randomUUID();
        this.timestamp = Instant.now();
        this.source = source;
        this.type = type;
    }

    public UUID getId() {
        return id;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public Object getSource() {
        return source;
    }

    public EventType getType() {
        return type;
    }

    public enum EventType {
        RESOURCE_CHANGED,
        BUILDING_PLACED,
        BUILDING_COMPLETED,
        BUILDING_ACTIVATED,
        BUILDING_DEACTIVATED,
        BUILDING_REMOVED,  // Add this entry
        TURN_ADVANCED,
        PHASE_CHANGED,
        TILE_UPDATED,
        GAME_STATE_CHANGED,
        RESOURCE_SHORTAGE  // Add this entry
    }
}