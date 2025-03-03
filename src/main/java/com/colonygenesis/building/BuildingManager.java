package com.colonygenesis.building;

import com.colonygenesis.core.Game;
import com.colonygenesis.event.EventBus;
import com.colonygenesis.event.events.BuildingEvent;
import com.colonygenesis.map.Tile;
import com.colonygenesis.resource.ResourceType;
import com.colonygenesis.util.LoggerUtils;
import com.colonygenesis.util.Result;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class BuildingManager {
    private static final Logger LOGGER = LoggerUtils.getLogger(BuildingManager.class);

    private final Game game;
    private final List<Building> buildings;
    private final List<Building> constructionQueue;
    private final Map<BuildingType, Integer> buildingCounts;
    private final EventBus eventBus;

    public BuildingManager(Game game) {
        this.game = game;
        this.buildings = new ArrayList<>();
        this.constructionQueue = new ArrayList<>();
        this.buildingCounts = new EnumMap<>(BuildingType.class);
        this.eventBus = EventBus.getInstance();

        // Initialize count for each building type
        for (BuildingType type : BuildingType.values()) {
            buildingCounts.put(type, 0);
        }

        LOGGER.info("BuildingManager initialized");
    }

    /**
     * Places a building on the specified tile.
     *
     * @param building The building to place
     * @param tile The tile to place the building on
     * @return A Result indicating success or failure with a detailed message
     */
    public Result<Building> placeBuilding(Building building, Tile tile) {
        // Validate parameters
        if (building == null) {
            return Result.failure("Building cannot be null");
        }
        if (tile == null) {
            return Result.failure("Tile cannot be null");
        }

        // Check if we can afford it
        if (!canAfford(building.getConstructionCost())) {
            LOGGER.warning("Cannot afford building: " + building.getName());
            return Result.failure("Cannot afford building: " + building.getName());
        }

        // Check if the building can be placed on this tile
        if (!building.canBuildOn(tile)) {
            LOGGER.warning("Building " + building.getName() + " can't be placed on tile " + tile);
            return Result.failure("Building can't be placed on this tile");
        }

        // Now that we've confirmed it can be built, deduct resources
        deductResources(building.getConstructionCost());
        building.markResourcesDeducted(); // Mark resources as deducted for this building

        // First associate the building with the location
        boolean buildSuccess = building.build(tile);
        if (!buildSuccess) {
            // Something went wrong in building.build()
            refundResources(building.getConstructionCost());
            LOGGER.warning("Building.build() failed for " + building.getName());
            return Result.failure("Failed to build " + building.getName());
        }

        // Then set the building on the tile
        boolean tileSuccess = tile.setBuilding(building);
        if (!tileSuccess) {
            // Something went wrong setting the building on tile
            building.demolish(); // Clear building location
            refundResources(building.getConstructionCost());
            LOGGER.warning("Tile.setBuilding() failed for " + building.getName() + " at " + tile);
            return Result.failure("Failed to set building on tile");
        }

        // If we got here, building placement was successful
        LOGGER.info("Building " + building.getName() + " placed successfully at " + tile);

        // Add to our tracking
        buildings.add(building);

        // Add to construction queue if not already completed
        if (!building.isCompleted()) {
            constructionQueue.add(building);
            LOGGER.info("Added " + building.getName() + " to construction queue");
            LOGGER.info("Construction time: " + building.getRemainingConstructionTime() + " turns");
        }

        // Update building count
        BuildingType type = building.getType();
        buildingCounts.put(type, buildingCounts.get(type) + 1);

        // Publish building placed event
        eventBus.publish(BuildingEvent.placed(this, building, tile));

        return Result.success(building);
    }

    /**
     * Removes a building from the game.
     *
     * @param building The building to remove
     * @return A Result indicating success or failure
     */
    public Result<Void> removeBuilding(Building building) {
        if (building == null) {
            return Result.failure("Building cannot be null");
        }

        Tile location = building.getLocation();
        if (location == null) {
            LOGGER.warning("Attempted to remove building with no location: " + building.getName());
            return Result.failure("Building has no location");
        }

        building.demolish();
        buildings.remove(building);
        constructionQueue.remove(building);

        // Update building count
        BuildingType type = building.getType();
        buildingCounts.put(type, buildingCounts.get(type) - 1);

        LOGGER.info("Building " + building.getName() + " removed from " + location);

        // Publish building removed event
        eventBus.publish(BuildingEvent.removed(this, building, location));

        return Result.success();
    }

    /**
     * Updates the construction progress of buildings in the construction queue.
     */
    public void updateConstructionQueue() {
        List<Building> completedBuildings = new ArrayList<>();

        LOGGER.fine("Construction queue size: " + constructionQueue.size());

        for (Building building : constructionQueue) {
            int before = building.getRemainingConstructionTime();
            building.update();
            int after = building.getRemainingConstructionTime();

            LOGGER.fine("Building " + building.getName() + " construction: " +
                    before + " -> " + after + " turns remaining");

            if (building.isCompleted()) {
                LOGGER.info("Building " + building.getName() + " completed!");
                completedBuildings.add(building);

                // Activate the building when completed
                building.activate();

                // Publish building completed event
                eventBus.publish(BuildingEvent.completed(this, building, building.getLocation()));

                // Also publish activation event
                eventBus.publish(BuildingEvent.activated(this, building, building.getLocation()));

                // No need to deduct resources again - already happened during placement
            }
        }

        // Remove completed buildings from the queue
        constructionQueue.removeAll(completedBuildings);
    }

    /**
     * Calculates the total production from all active buildings.
     *
     * @return A map of resources to their production amounts
     */
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

    /**
     * Checks if the player can afford the specified resources.
     */
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

    /**
     * Deducts resources from the player's stockpile.
     */
    private void deductResources(Map<ResourceType, Integer> cost) {
        for (Map.Entry<ResourceType, Integer> entry : cost.entrySet()) {
            ResourceType type = entry.getKey();
            int amount = entry.getValue();

            game.getResourceManager().removeResource(type, amount);
        }
    }

    /**
     * Refunds resources to the player's stockpile.
     */
    private void refundResources(Map<ResourceType, Integer> cost) {
        for (Map.Entry<ResourceType, Integer> entry : cost.entrySet()) {
            ResourceType type = entry.getKey();
            int amount = entry.getValue();

            game.getResourceManager().addResource(type, amount);
        }
    }

    // Getter methods

    /**
     * Gets all active buildings.
     */
    public List<Building> getActiveBuildings() {
        List<Building> activeBuildings = new ArrayList<>();

        for (Building building : buildings) {
            if (building.isActive()) {
                activeBuildings.add(building);
            }
        }

        return activeBuildings;
    }

    /**
     * Gets all buildings (active and inactive).
     */
    public List<Building> getBuildings() {
        return new ArrayList<>(buildings);
    }

    /**
     * Gets all buildings currently under construction.
     */
    public List<Building> getConstructionQueue() {
        return new ArrayList<>(constructionQueue);
    }

    /**
     * Gets the count of buildings of a specific type.
     */
    public int getBuildingCount(BuildingType type) {
        return buildingCounts.getOrDefault(type, 0);
    }
}