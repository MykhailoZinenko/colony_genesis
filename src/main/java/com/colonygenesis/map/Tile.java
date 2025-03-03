package com.colonygenesis.map;

//import com.colonygenesis.building.Building;
import com.colonygenesis.building.Building;
import com.colonygenesis.resource.ResourceType;
//import com.colonygenesis.environment.EnvironmentalEffect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Tile {
    // Position in the grid
    private final int x;
    private final int y;

    // Tile properties
    private TerrainType terrainType;
    private boolean revealed;

    // What's on this tile
    private Building building;
    //private List<EnvironmentalEffect> effects;

    // Resource information
    private Map<ResourceType, Double> resourceModifiers;
    private ResourceDeposit resourceDeposit; // Special resource node

    public Tile(int x, int y, TerrainType terrainType) {
        this.x = x;
        this.y = y;
        this.terrainType = terrainType;
        this.revealed = false;
        //this.effects = new ArrayList<>();
        this.resourceModifiers = new HashMap<>();
    }

    // Getter for coordinates (immutable)
    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    // Terrain methods
    public TerrainType getTerrainType() {
        return terrainType;
    }

    public void setTerrainType(TerrainType terrainType) {
        this.terrainType = terrainType;
    }

    // Visibility methods
    public boolean isRevealed() {
        return revealed;
    }

    public void reveal() {
        this.revealed = true;
    }

    // Building methods
    public boolean hasBuilding() {
        return building != null;
    }

    public Building getBuilding() {
        return building;
    }

    // In Tile.java:
    public boolean setBuilding(Building building) {
        // We already verified the tile is habitable and empty in building.canBuildOn
        // Just set the building without additional checks
        this.building = building;
        return true;
    }
    public void removeBuilding() {
        this.building = null;
    }

    // Resource methods
    public double getResourceYield(ResourceType resourceType) {
        double baseYield = terrainType.getResourceModifier(resourceType);

        // Apply tile-specific modifiers
        if (resourceModifiers.containsKey(resourceType)) {
            baseYield *= resourceModifiers.get(resourceType);
        }

        // Apply building modifiers if present
        if (building != null) {
            baseYield *= building.getProductionModifier(resourceType);
        }
//
//        // Apply effects
//        for (EnvironmentalEffect effect : effects) {
//            baseYield *= effect.getResourceModifier(resourceType);
//        }

        return baseYield;
    }

    // Resource deposit methods
    public boolean hasResourceDeposit() {
        return resourceDeposit != null;
    }

    public ResourceDeposit getResourceDeposit() {
        return resourceDeposit;
    }

    public void setResourceDeposit(ResourceDeposit deposit) {
        this.resourceDeposit = deposit;
    }

    // Environmental effects
//    public void addEffect(EnvironmentalEffect effect) {
//        effects.add(effect);
//    }
//
//    public void removeEffect(EnvironmentalEffect effect) {
//        effects.remove(effect);
//    }
//
//    public List<EnvironmentalEffect> getEffects() {
//        return new ArrayList<>(effects); // Return defensive copy
//    }

    // Utility methods
    public boolean isHabitable() {
        return terrainType.isHabitable();
    }

    @Override
    public String toString() {
        return "Tile[" + x + "," + y + "] " + terrainType.getName();
    }
}