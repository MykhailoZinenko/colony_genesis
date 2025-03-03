package com.colonygenesis.map;

public enum PlanetType {
    TEMPERATE("Temperate", "Balanced resources and mild climate"),
    DESERT("Desert", "Hot and dry with scarce water but abundant energy"),
    TUNDRA("Tundra", "Cold with limited food but rich in minerals"),
    VOLCANIC("Volcanic", "Harsh environment with rich rare resources"),
    OCEANIC("Oceanic", "Water-covered with limited land but abundant food");

    private final String name;
    private final String description;

    PlanetType(String name, String description) {
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