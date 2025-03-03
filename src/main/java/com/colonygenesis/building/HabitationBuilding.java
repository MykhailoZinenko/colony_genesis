package com.colonygenesis.building;

import com.colonygenesis.resource.ResourceType;

import java.util.EnumMap;
import java.util.Map;

public class HabitationBuilding extends Building {
    private int capacity; // How many colonists it can house
    private float comfortLevel; // 0.0 to 1.0, affects morale

    public HabitationBuilding(String name, String description, int constructionTime,
                              int capacity, float comfortLevel) {
        super(name, description, BuildingType.HABITATION, constructionTime);
        this.capacity = capacity;
        this.comfortLevel = comfortLevel;

        // Habitation buildings consume resources
        this.maintenanceCost.put(ResourceType.ENERGY, capacity / 5);
        this.maintenanceCost.put(ResourceType.WATER, capacity / 10);
    }

    @Override
    public Map<ResourceType, Integer> getProduction() {
        // Habitation buildings consume, not produce
        Map<ResourceType, Integer> production = new EnumMap<>(ResourceType.class);

        if (isActive()) {
            // Negative values for consumption
            for (Map.Entry<ResourceType, Integer> entry : maintenanceCost.entrySet()) {
                production.put(entry.getKey(), -entry.getValue());
            }
        }

        return production;
    }

    public int getPopulationCapacity() {
        return isActive() ? capacity : 0;
    }

    public float getComfortModifier() {
        return isActive() ? comfortLevel : 0;
    }
}