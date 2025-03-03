package com.colonygenesis.event;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EventBus {
    private static final Logger LOGGER = Logger.getLogger(EventBus.class.getName());
    private static EventBus instance;

    // Using ConcurrentHashMap for thread safety
    private final Map<GameEvent.EventType, Set<EventListener>> listeners = new ConcurrentHashMap<>();

    private EventBus() {
        // Private constructor for singleton
        // Initialize event types
        for (GameEvent.EventType type : GameEvent.EventType.values()) {
            listeners.put(type, new HashSet<>());
        }
    }

    public static synchronized EventBus getInstance() {
        if (instance == null) {
            instance = new EventBus();
        }
        return instance;
    }

    public void register(EventListener listener, GameEvent.EventType... eventTypes) {
        if (eventTypes.length == 0) {
            // Register for all events if none specified
            for (GameEvent.EventType type : GameEvent.EventType.values()) {
                listeners.get(type).add(listener);
            }
        } else {
            // Register for specific events
            for (GameEvent.EventType type : eventTypes) {
                listeners.get(type).add(listener);
            }
        }
    }

    public void unregister(EventListener listener) {
        for (Set<EventListener> eventListeners : listeners.values()) {
            eventListeners.remove(listener);
        }
    }

    public void publish(GameEvent event) {
        LOGGER.log(Level.FINE, "Publishing event: " + event.getType() + " from " + event.getSource());

        Set<EventListener> eventListeners = listeners.get(event.getType());
        if (eventListeners != null) {
            for (EventListener listener : eventListeners) {
                try {
                    if (listener.isInterestedIn(event.getType())) {
                        listener.onEvent(event);
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Error dispatching event " + event.getType() + " to listener " + listener, e);
                }
            }
        }
    }
}