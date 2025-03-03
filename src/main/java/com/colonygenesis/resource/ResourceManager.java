package com.colonygenesis.resource;

import com.colonygenesis.building.Building;
import com.colonygenesis.core.Game;
import com.colonygenesis.map.Tile;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ResourceManager {
    private final Game game;

    // Resource tracking
    private Map<ResourceType, Integer> resources; // Current stockpiles
    private Map<ResourceType, Integer> capacity; // Max storage capacity
    private Map<ResourceType, Integer> production; // Per turn production
    private Map<ResourceType, Integer> consumption; // Per turn consumption
    private Map<ResourceType, Integer> lastTurnResources;
    public ResourceManager(Game game) {
        this.game = game;

        // Initialize resource maps
        resources = new EnumMap<>(ResourceType.class);
        capacity = new EnumMap<>(ResourceType.class);
        production = new EnumMap<>(ResourceType.class);
        consumption = new EnumMap<>(ResourceType.class);

        // Set default values
        for (ResourceType type : ResourceType.values()) {
            resources.put(type, 0);
            capacity.put(type, type.getBaseStorage());
            production.put(type, 0);
            consumption.put(type, 0);
        }

        // Starting resources for a new colony
        resources.put(ResourceType.FOOD, 1000);
        resources.put(ResourceType.WATER, 1000);
        resources.put(ResourceType.MATERIALS, 2000);
        resources.put(ResourceType.ENERGY, 500);

        lastTurnResources = new EnumMap<>(ResourceType.class);
        for (ResourceType type : ResourceType.values()) {
            lastTurnResources.put(type, 0);
        }

        String initialPhase = game.getTurnManager().getCurrentPhase().getName();
    }

    public int getResource(ResourceType type) {
        return resources.getOrDefault(type, 0);
    }

    public Map<ResourceType, Integer> getAllResources() {
        // Return a defensive copy
        return new EnumMap<>(resources);
    }

    // Add this method to the ResourceManager class:

    public Map<ResourceType, Integer> getAllProduction() {
        // Return a defensive copy
        return new EnumMap<>(production);
    }

    public Map<ResourceType, Integer> getAllConsumption() {
        // Return a defensive copy
        return new EnumMap<>(consumption);
    }

    public Map<ResourceType, Integer> getAllNetProduction() {
        Map<ResourceType, Integer> netProduction = new EnumMap<>(ResourceType.class);

        for (ResourceType type : ResourceType.values()) {
            netProduction.put(type, getNetProduction(type));
        }

        return netProduction;
    }

    public int getCapacity(ResourceType type) {
        return capacity.getOrDefault(type, 0);
    }

    public int getProduction(ResourceType type) {
        return production.getOrDefault(type, 0);
    }

    public int getConsumption(ResourceType type) {
        return consumption.getOrDefault(type, 0);
    }

    public int getNetProduction(ResourceType type) {
        return getProduction(type) - getConsumption(type);
    }

    public boolean addResource(ResourceType type, int amount) {
        if (amount <= 0) return false;

        int current = resources.getOrDefault(type, 0);
        int cap = capacity.getOrDefault(type, 0);

        // For non-storable resources, we don't enforce capacity
        if (type.isStorable() && current + amount > cap) {
            resources.put(type, cap); // Cap at maximum
            return false; // Indicate some was wasted
        } else {
            resources.put(type, current + amount);
            return true; // All added successfully
        }
    }

    public boolean removeResource(ResourceType type, int amount) {
        if (amount <= 0) return false;

        int current = resources.getOrDefault(type, 0);

        if (current < amount) {
            return false; // Not enough resources
        }

        resources.put(type, current - amount);
        return true; // Successfully removed
    }

    public void updateStorage() {
        // Update capacities based on buildings (we'll implement this later)
    }

    public void calculateProduction() {
        // Reset production and consumption
        for (ResourceType type : ResourceType.values()) {
            production.put(type, 0);
            consumption.put(type, 0);
        }

        // Get production from buildings (if BuildingManager exists)
        if (game.getBuildingManager() != null) {
            Map<ResourceType, Integer> buildingProduction =
                    game.getBuildingManager().calculateTotalProduction();

            for (Map.Entry<ResourceType, Integer> entry : buildingProduction.entrySet()) {
                ResourceType type = entry.getKey();
                int amount = entry.getValue();

                if (amount > 0) {
                    // Positive values are production
                    production.put(type, production.get(type) + amount);
                } else if (amount < 0) {
                    // Negative values are consumption
                    consumption.put(type, consumption.get(type) - amount);
                }
            }
        }

        // Additional production/consumption from other sources will go here
        // (e.g., population consumption, environmental effects, etc.)
    }


// Update processTurn method:
public void processTurn() {
    // Store current resource values for change tracking
    for (ResourceType type : ResourceType.values()) {
        lastTurnResources.put(type, resources.get(type));
    }

    // Calculate production and consumption for this turn
    calculateProduction();

    // Apply production and consumption
    StringBuilder resourceReport = new StringBuilder("Resource changes:\n");

    for (ResourceType type : ResourceType.values()) {
        int net = getNetProduction(type);
        int before = resources.get(type);

        if (net > 0) {
            addResource(type, net);
            resourceReport.append(type.getName()).append(": +").append(net).append("\n");
        } else if (net < 0) {
            if (!removeResource(type, -net)) {
                // Not enough resources - trigger shortages
                handleShortage(type, -net);
                resourceReport.append(type.getName()).append(": SHORTAGE\n");
            } else {
                resourceReport.append(type.getName()).append(": ").append(net).append("\n");
            }
        }
    }

    // Log resource changes
    System.out.println(resourceReport.toString());

    // Update storage capacities
    updateStorage();
}

// Add method to get resource changes:
public Map<ResourceType, Integer> getResourceChanges() {
    Map<ResourceType, Integer> changes = new EnumMap<>(ResourceType.class);

    for (ResourceType type : ResourceType.values()) {
        int current = resources.get(type);
        int previous = lastTurnResources.get(type);
        changes.put(type, current - previous);
    }

    return changes;
}

// Enhance handleShortage method:
private void handleShortage(ResourceType type, int shortageAmount) {
    System.out.println("SHORTAGE: " + type.getName() + " - " + shortageAmount + " units short");

    // Notify the player
    if (game.getUserInterface() != null) {
        game.getUserInterface().showMessage("Shortage of " + type.getName() + "!", "error");
    }

    // Apply shortage effects based on resource type
    switch (type) {
        case FOOD:
            // Food shortage could affect population growth or morale
            // We'll implement this when we have population
            break;
        case ENERGY:
            // Energy shortage could deactivate some buildings
            deactivateRandomBuilding();
            break;
        case WATER:
            // Water shortage could affect food production
            // We'll implement this when we have more complex resource interactions
            break;
        default:
            // Other resources may have less severe effects
            break;
    }
}

// Add method to handle energy shortages:
private void deactivateRandomBuilding() {
    if (game.getBuildingManager() == null) return;

    List<Building> activeBuildings = game.getBuildingManager().getActiveBuildings();
    if (activeBuildings.isEmpty()) return;

    // Pick a random building to deactivate
    int index = new Random().nextInt(activeBuildings.size());
    Building building = activeBuildings.get(index);

    building.deactivate();

    // Notify the player
    if (game.getUserInterface() != null) {
        game.getUserInterface().showMessage(
                building.getName() + " was deactivated due to energy shortage!",
                "error"
        );
    }
}

    // Increase storage capacity (e.g., when building storage buildings)
    public void increaseCapacity(ResourceType type, int amount) {
        if (amount <= 0) return;

        int current = capacity.getOrDefault(type, 0);
        capacity.put(type, current + amount);
    }
}