package com.colonygenesis.event;

public interface EventListener {
    void onEvent(GameEvent event);

    // Optional method to specify what events this listener cares about
    default boolean isInterestedIn(GameEvent.EventType eventType) {
        return true; // By default, listen to all events
    }
}