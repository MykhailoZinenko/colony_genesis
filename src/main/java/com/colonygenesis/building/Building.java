package com.colonygenesis.building;

import com.colonygenesis.map.Tile;
import com.colonygenesis.map.TerrainType;
import com.colonygenesis.resource.ResourceType;

import java.util.EnumMap;
import java.util.Map;

public abstract class Building {
    // Basic building info
    protected String name;
    protected String description;
    protected BuildingType type;

    // Building status
    protected boolean active;
    protected int constructionTime;
    protected int remainingConstructionTime;

    // Resource information
    protected Map<ResourceType, Integer> constructionCost;
    protected Map<ResourceType, Integer> maintenanceCost;

    // Building placement
    protected Tile location;

    public Building(String name, String description, BuildingType type, int constructionTime) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.constructionTime = constructionTime;
        this.remainingConstructionTime = constructionTime;
        this.active = false;

        // Initialize cost maps
        this.constructionCost = new EnumMap<>(ResourceType.class);
        this.maintenanceCost = new EnumMap<>(ResourceType.class);
    }

    public boolean build(Tile location) {
        // We already checked canBuildOn in placeBuilding, so just set location
        this.location = location;
        return true;
    }
    public void demolish() {
        if (location != null) {
            location.removeBuilding();
            location = null;
        }
    }

    // In Building.java, double-check the update method:
    public void update() {
        System.out.println("Updating building: " + name + ", remaining time: " + remainingConstructionTime);

        if (remainingConstructionTime > 0) {
            remainingConstructionTime--;
            System.out.println("Construction progress for " + name + ": " + remainingConstructionTime + " turns left");

            if (remainingConstructionTime == 0) {
                System.out.println("Building " + name + " construction completed!");
                activate();
            }
        }
    }

    public void activate() {
        active = true;
    }

    public void deactivate() {
        active = false;
    }

    public void toggleActive() {
        active = !active;
    }

    // Resource-related methods
    public Map<ResourceType, Integer> getConstructionCost() {
        return new EnumMap<>(constructionCost); // Return defensive copy
    }

    public Map<ResourceType, Integer> getMaintenanceCost() {
        return new EnumMap<>(maintenanceCost); // Return defensive copy
    }

    public double getProductionModifier(ResourceType type) {
        // Base implementation, to be overridden by subclasses
        return 1.0; // No modification by default
    }

    public abstract Map<ResourceType, Integer> getProduction();

    // Validation methods
    public boolean canBuildOn(Tile tile) {
        // Check if tile is habitable
        System.out.println("4: " + tile + " " + !tile.isHabitable() + " " + tile.hasBuilding());
        if (tile == null || !tile.isHabitable()) {
            return false;
        }

        System.out.println("2:"  + isCompatibleWithTerrain(tile.getTerrainType()) + " Building " + name + " at " + tile.getTerrainType() + tile.isHabitable() + tile.hasBuilding());

        // Check terrain compatibility
        return isCompatibleWithTerrain(tile.getTerrainType());
    }

    protected boolean isCompatibleWithTerrain(TerrainType terrain) {
        // Default compatibility rules - subclasses can override
        return terrain != TerrainType.WATER;
    }

    // Status methods
    public boolean isCompleted() {
        return remainingConstructionTime <= 0;
    }

    public boolean isActive() {
        return active && isCompleted();
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public BuildingType getType() {
        return type;
    }

    public int getConstructionTime() {
        return constructionTime;
    }

    public int getRemainingConstructionTime() {
        return remainingConstructionTime;
    }

    public Tile getLocation() {
        return location;
    }
}