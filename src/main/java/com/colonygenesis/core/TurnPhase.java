package com.colonygenesis.core;

public enum TurnPhase {
    PLANNING("Planning", "Plan your next actions", true),
    BUILDING("Building", "Construct and upgrade buildings", true),
    PRODUCTION("Production", "Resource production and consumption", false),
    EVENTS("Events", "Handle planetary events", false),
    END_TURN("End Turn", "Finalize turn and advance", false);

    private final String name;
    private final String description;
    private final boolean requiresInput; // Does this phase need player input

    TurnPhase(String name, String description, boolean requiresInput) {
        this.name = name;
        this.description = description;
        this.requiresInput = requiresInput;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public boolean requiresInput() {
        return requiresInput;
    }
}