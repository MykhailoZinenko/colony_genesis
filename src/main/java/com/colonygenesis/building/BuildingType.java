package com.colonygenesis.building;

public enum BuildingType {
    HABITATION("Habitation", "Housing and life support for colonists"),
    PRODUCTION("Production", "Produces basic resources"),
    RESEARCH("Research", "Generates research points"),
    STORAGE("Storage", "Increases resource storage capacity"),
    INFRASTRUCTURE("Infrastructure", "Improves colony operations"),
    DEFENSE("Defense", "Protects against threats"),
    SPECIAL("Special", "Unique buildings with special effects");

    private final String name;
    private final String description;

    BuildingType(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}