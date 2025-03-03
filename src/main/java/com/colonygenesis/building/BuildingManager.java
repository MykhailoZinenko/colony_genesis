package com.colonygenesis.building;

import com.colonygenesis.core.Game;
import com.colonygenesis.map.Tile;
import com.colonygenesis.resource.ResourceType;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class BuildingManager {
    private Game game;
    private List<Building> buildings;
    private List<Building> constructionQueue;
    private Map<BuildingType, Integer> buildingCounts;

    public BuildingManager(Game game) {
        this.game = game;
        this.buildings = new ArrayList<>();
        this.constructionQueue = new ArrayList<>();
        this.buildingCounts = new EnumMap<>(BuildingType.class);

        // Initialize count for each building type
        for (BuildingType type : BuildingType.values()) {
            buildingCounts.put(type, 0);
        }
    }

    // Update BuildingManager.placeBuilding method:
    // Update in BuildingManager.java:
    public boolean placeBuilding(Building building, Tile tile) {
        // Check if we can afford it
        if (!canAfford(building.getConstructionCost())) {
            System.out.println("Cannot afford building!");
            return false;
        }

        // Check if the building can be placed on this tile BEFORE setting it
        if (!building.canBuildOn(tile)) {
            System.out.println("Building can't be placed on this tile!");
            return false;
        }

        // Now that we've confirmed it can be built, deduct resources
        deductResources(building.getConstructionCost());

        // First associate the building with the location
        boolean buildSuccess = building.build(tile);
        if (!buildSuccess) {
            // Something went wrong in building.build()
            refundResources(building.getConstructionCost());
            System.out.println("Building.build() failed!");
            return false;
        }

        // Then set the building on the tile (this shouldn't fail now)
        boolean tileSuccess = tile.setBuilding(building);
        if (!tileSuccess) {
            // Something went wrong setting the building on tile
            building.demolish(); // Clear building location
            refundResources(building.getConstructionCost());
            System.out.println("Tile.setBuilding() failed!");
            return false;
        }

        // If we got here, building placement was successful

        // Add to our tracking
        buildings.add(building);

        // Add to construction queue if not already completed
        if (!building.isCompleted()) {
            constructionQueue.add(building);
            System.out.println("Added " + building.getName() + " to construction queue");
            System.out.println("Construction time: " + building.getRemainingConstructionTime() + " turns");
        }

        // Update building count
        BuildingType type = building.getType();
        buildingCounts.put(type, buildingCounts.get(type) + 1);

        return true;
    }

    public void removeBuilding(Building building) {
        if (building == null) return;

        building.demolish();
        buildings.remove(building);
        constructionQueue.remove(building);

        // Update building count
        BuildingType type = building.getType();
        buildingCounts.put(type, buildingCounts.get(type) - 1);
    }

    // Update BuildingManager.updateConstructionQueue method:
    public void updateConstructionQueue() {
        List<Building> completedBuildings = new ArrayList<>();

        // Log what's in the queue
        System.out.println("Construction queue size: " + constructionQueue.size());

        for (Building building : constructionQueue) {
            // CRITICAL FIX: Make sure we're correctly updating the construction progress
            int before = building.getRemainingConstructionTime();
            building.update();
            int after = building.getRemainingConstructionTime();

            System.out.println("Building " + building.getName() + " construction: " +
                    before + " -> " + after + " turns remaining");

            if (building.isCompleted()) {
                System.out.println("Building " + building.getName() + " completed!");
                completedBuildings.add(building);

                // IMPORTANT: Make sure we're activating the building when completed
                building.activate();

                // Notify UI if available
                if (game.getUserInterface() != null) {
                    game.getUserInterface().showMessage(
                            "Construction of " + building.getName() + " completed!", "success");
                }
            }
        }

        // Remove completed buildings from the queue
        constructionQueue.removeAll(completedBuildings);
    }

    public Map<ResourceType, Integer> calculateTotalProduction() {
        Map<ResourceType, Integer> totalProduction = new EnumMap<>(ResourceType.class);

        // Initialize with zeros
        for (ResourceType type : ResourceType.values()) {
            totalProduction.put(type, 0);
        }

        // Add production from each building
        for (Building building : buildings) {
            if (building.isActive()) {
                Map<ResourceType, Integer> production = building.getProduction();

                for (Map.Entry<ResourceType, Integer> entry : production.entrySet()) {
                    ResourceType type = entry.getKey();
                    int amount = entry.getValue();

                    // Add to total
                    totalProduction.put(type, totalProduction.get(type) + amount);
                }
            }
        }

        return totalProduction;
    }

    private boolean canAfford(Map<ResourceType, Integer> cost) {
        for (Map.Entry<ResourceType, Integer> entry : cost.entrySet()) {
            ResourceType type = entry.getKey();
            int amount = entry.getValue();

            if (game.getResourceManager().getResource(type) < amount) {
                return false;
            }
        }

        return true;
    }

    private void deductResources(Map<ResourceType, Integer> cost) {
        for (Map.Entry<ResourceType, Integer> entry : cost.entrySet()) {
            ResourceType type = entry.getKey();
            int amount = entry.getValue();

            game.getResourceManager().removeResource(type, amount);
        }
    }

    private void refundResources(Map<ResourceType, Integer> cost) {
        for (Map.Entry<ResourceType, Integer> entry : cost.entrySet()) {
            ResourceType type = entry.getKey();
            int amount = entry.getValue();

            game.getResourceManager().addResource(type, amount);
        }
    }

    public List<Building> getActiveBuildings() {
        List<Building> activeBuildings = new ArrayList<>();

        for (Building building : buildings) {
            if (building.isActive()) {
                activeBuildings.add(building);
            }
        }

        return activeBuildings;
    }

    public List<Building> getBuildings() {
        return new ArrayList<>(buildings);
    }

    public List<Building> getConstructionQueue() {
        return new ArrayList<>(constructionQueue);
    }

    public int getBuildingCount(BuildingType type) {
        return buildingCounts.getOrDefault(type, 0);
    }
}