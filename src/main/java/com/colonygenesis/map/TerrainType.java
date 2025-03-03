package com.colonygenesis.map;

import javafx.scene.paint.Color;
import java.util.HashMap;
import java.util.Map;

import com.colonygenesis.resource.ResourceType;

public enum TerrainType {
    PLAINS("Plains", 1.0, 1.0, Color.LIGHTGREEN),
    MOUNTAINS("Mountains", 2.5, 0.7, Color.GRAY),
    FOREST("Forest", 1.5, 0.9, Color.DARKGREEN),
    WATER("Water", 3.0, 0.0, Color.LIGHTSKYBLUE),
    DESERT("Desert", 1.2, 0.5, Color.SANDYBROWN),
    TUNDRA("Tundra", 1.8, 0.6, Color.LIGHTBLUE);

    private final String name;
    private final double movementCost;
    private final double buildingModifier;
    private final Color displayColor;
    private final Map<ResourceType, Double> resourceModifiers;

    TerrainType(String name, double movementCost, double buildingModifier, Color displayColor) {
        this.name = name;
        this.movementCost = movementCost;
        this.buildingModifier = buildingModifier;
        this.displayColor = displayColor;
        this.resourceModifiers = new HashMap<>();

        // We'll initialize resource modifiers later when ResourceType is implemented
        // For now, let's leave it empty
    }

    public String getName() {
        return name;
    }

    public double getMovementCost() {
        return movementCost;
    }

    public double getBuildingModifier() {
        return buildingModifier;
    }

    public Color getDisplayColor() {
        return displayColor;
    }

    public double getResourceModifier(ResourceType type) {
        return resourceModifiers.getOrDefault(type, 1.0);
    }

    // This will be called when we initialize ResourceType
    // In TerrainType.java, update the initializeResourceModifiers method:

    public void initializeResourceModifiers() {
        // Clear any existing modifiers
        resourceModifiers.clear();

        // Set default modifier for all resources (1.0 = no effect)
        for (ResourceType type : ResourceType.values()) {
            resourceModifiers.put(type, 1.0);
        }

        // Apply specific modifiers based on terrain type
        switch (this) {
            case PLAINS:
                // Good for food production, average for others
                resourceModifiers.put(ResourceType.FOOD, 1.5);
                resourceModifiers.put(ResourceType.WATER, 1.2);
                break;

            case MOUNTAINS:
                // Rich in minerals, poor in food/water
                resourceModifiers.put(ResourceType.FOOD, 0.5);
                resourceModifiers.put(ResourceType.MATERIALS, 1.8);
                resourceModifiers.put(ResourceType.RARE_MINERALS, 2.0);
                resourceModifiers.put(ResourceType.WATER, 0.7);
                break;

            case FOREST:
                // Good balance of food and materials
                resourceModifiers.put(ResourceType.FOOD, 1.3);
                resourceModifiers.put(ResourceType.MATERIALS, 1.4);
                resourceModifiers.put(ResourceType.WATER, 1.1);
                break;

            case WATER:
                // Obviously good for water, can't build here though
                resourceModifiers.put(ResourceType.WATER, 3.0);
                resourceModifiers.put(ResourceType.FOOD, 0.8); // Fishing
                break;

            case DESERT:
                // Poor for most resources, good for energy (solar)
                resourceModifiers.put(ResourceType.FOOD, 0.3);
                resourceModifiers.put(ResourceType.WATER, 0.2);
                resourceModifiers.put(ResourceType.ENERGY, 1.5);
                resourceModifiers.put(ResourceType.ALIEN_COMPOUNDS, 1.2); // Desert ruins
                break;

            case TUNDRA:
                // Cold and limited, but some rare resources
                resourceModifiers.put(ResourceType.FOOD, 0.4);
                resourceModifiers.put(ResourceType.WATER, 0.8); // Ice
                resourceModifiers.put(ResourceType.ENERGY, 0.7);
                resourceModifiers.put(ResourceType.RARE_MINERALS, 1.4);
                break;
        }
    }

    public boolean isHabitable() {
        // Water is not habitable
        return this != WATER;
    }
}