package com.colonygenesis.resource;

import com.colonygenesis.building.Building;
import com.colonygenesis.core.Game;
import com.colonygenesis.event.EventBus;
import com.colonygenesis.event.GameEvent;
import com.colonygenesis.event.events.BuildingEvent;
import com.colonygenesis.event.events.ResourceEvent;
import com.colonygenesis.util.LoggerUtils;
import com.colonygenesis.util.Result;

import java.util.*;
import java.util.logging.Logger;

public class ResourceManager {
    private static final Logger LOGGER = LoggerUtils.getLogger(ResourceManager.class);

    private final Game game;
    private final EventBus eventBus;

    // Resource tracking
    private final Map<ResourceType, Integer> resources; // Current stockpiles
    private final Map<ResourceType, Integer> capacity; // Max storage capacity
    private final Map<ResourceType, Integer> production; // Per turn production
    private final Map<ResourceType, Integer> consumption; // Per turn consumption
    private final Map<ResourceType, Integer> lastTurnResources; // For change tracking

    public ResourceManager(Game game) {
        this.game = game;
        this.eventBus = EventBus.getInstance();

        // Initialize resource maps
        resources = new EnumMap<>(ResourceType.class);
        capacity = new EnumMap<>(ResourceType.class);
        production = new EnumMap<>(ResourceType.class);
        consumption = new EnumMap<>(ResourceType.class);
        lastTurnResources = new EnumMap<>(ResourceType.class);

        // Set default values
        for (ResourceType type : ResourceType.values()) {
            resources.put(type, 0);
            capacity.put(type, type.getBaseStorage());
            production.put(type, 0);
            consumption.put(type, 0);
            lastTurnResources.put(type, 0);
        }

        // Starting resources for a new colony
        resources.put(ResourceType.FOOD, 1000);
        resources.put(ResourceType.WATER, 1000);
        resources.put(ResourceType.MATERIALS, 2000);
        resources.put(ResourceType.ENERGY, 500);

        LOGGER.info("ResourceManager initialized with starting resources");
    }

    /**
     * Gets the current amount of a specific resource.
     *
     * @param type The resource type
     * @return The amount of the resource
     */
    public int getResource(ResourceType type) {
        return resources.getOrDefault(type, 0);
    }

    /**
     * Gets a map of all current resource amounts.
     *
     * @return A defensive copy of the resources map
     */
    public Map<ResourceType, Integer> getAllResources() {
        return new EnumMap<>(resources);
    }

    /**
     * Gets a map of all resource production values.
     *
     * @return A defensive copy of the production map
     */
    public Map<ResourceType, Integer> getAllProduction() {
        return new EnumMap<>(production);
    }

    /**
     * Gets a map of all resource consumption values.
     *
     * @return A defensive copy of the consumption map
     */
    public Map<ResourceType, Integer> getAllConsumption() {
        return new EnumMap<>(consumption);
    }

    /**
     * Gets a map of net production for all resources.
     *
     * @return A map of resources to their net production values
     */
    public Map<ResourceType, Integer> getAllNetProduction() {
        Map<ResourceType, Integer> netProduction = new EnumMap<>(ResourceType.class);

        for (ResourceType type : ResourceType.values()) {
            netProduction.put(type, getNetProduction(type));
        }

        return netProduction;
    }

    /**
     * Gets the storage capacity for a specific resource.
     *
     * @param type The resource type
     * @return The storage capacity
     */
    public int getCapacity(ResourceType type) {
        return capacity.getOrDefault(type, 0);
    }

    /**
     * Gets the production amount for a specific resource.
     *
     * @param type The resource type
     * @return The production amount
     */
    public int getProduction(ResourceType type) {
        return production.getOrDefault(type, 0);
    }

    /**
     * Gets the consumption amount for a specific resource.
     *
     * @param type The resource type
     * @return The consumption amount
     */
    public int getConsumption(ResourceType type) {
        return consumption.getOrDefault(type, 0);
    }

    /**
     * Gets the net production (production - consumption) for a specific resource.
     *
     * @param type The resource type
     * @return The net production
     */
    public int getNetProduction(ResourceType type) {
        return getProduction(type) - getConsumption(type);
    }

    /**
     * Adds an amount of a resource to the player's stockpile.
     *
     * @param type   The resource type
     * @param amount The amount to add
     * @return A Result indicating success or failure
     */
    public Result<Integer> addResource(ResourceType type, int amount) {
        if (type == null) {
            return Result.failure("Resource type cannot be null");
        }

        if (amount <= 0) {
            return Result.failure("Amount must be positive");
        }

        int current = resources.getOrDefault(type, 0);
        int cap = capacity.getOrDefault(type, 0);
        int previous = current;

        // For non-storable resources, we don't enforce capacity
        if (type.isStorable() && current + amount > cap) {
            resources.put(type, cap); // Cap at maximum

            int actualAdded = cap - previous;
            LOGGER.warning(String.format("Resource %s at capacity: %d/%d. Wasted %d units",
                    type.getName(), cap, cap, amount - actualAdded));

            // Publish resource change event
            eventBus.publish(new ResourceEvent(this, type, cap, previous));

            return Result.failure(String.format("Storage at capacity. Added %d of %d %s",
                    actualAdded, amount, type.getName()));
        } else {
            resources.put(type, current + amount);

            LOGGER.info(String.format("Added %d %s. New total: %d",
                    amount, type.getName(), current + amount));

            // Publish resource change event
            eventBus.publish(new ResourceEvent(this, type, current + amount, previous));

            return Result.success(amount);
        }
    }

    /**
     * Removes an amount of a resource from the player's stockpile.
     *
     * @param type   The resource type
     * @param amount The amount to remove
     * @return A Result indicating success or failure
     */
    public Result<Integer> removeResource(ResourceType type, int amount) {
        if (type == null) {
            return Result.failure("Resource type cannot be null");
        }

        if (amount <= 0) {
            return Result.failure("Amount must be positive");
        }

        int current = resources.getOrDefault(type, 0);
        int previous = current;

        if (current < amount) {
            LOGGER.warning(String.format("Not enough %s: %d/%d needed",
                    type.getName(), current, amount));
            return Result.failure(String.format("Not enough %s: %d/%d needed",
                    type.getName(), current, amount));
        }

        resources.put(type, current - amount);

        LOGGER.info(String.format("Removed %d %s. New total: %d",
                amount, type.getName(), current - amount));

        // Publish resource change event
        eventBus.publish(new ResourceEvent(this, type, current - amount, previous));

        return Result.success(amount);
    }

    /**
     * Updates storage capacities based on buildings.
     */
    public void updateStorage() {
        Map<ResourceType, Integer> oldCapacities = new EnumMap<>(capacity);

        // Reset to base capacities
        for (ResourceType type : ResourceType.values()) {
            capacity.put(type, type.getBaseStorage());
        }

        // Add bonuses from buildings (placeholder implementation)
        // We'll improve this once we have proper storage buildings

        // Log any significant capacity changes
        for (ResourceType type : ResourceType.values()) {
            int oldCap = oldCapacities.get(type);
            int newCap = capacity.get(type);

            if (newCap != oldCap) {
                LOGGER.info(String.format("%s storage capacity changed: %d → %d",
                        type.getName(), oldCap, newCap));
            }
        }
    }

    /**
     * Calculates production and consumption rates for the current turn.
     */
    public void calculateProduction() {
        // Reset production and consumption
        for (ResourceType type : ResourceType.values()) {
            production.put(type, 0);
            consumption.put(type, 0);
        }

        // Get production from buildings
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

        // Additional production/consumption from other sources will be added here
        // (e.g., population consumption, environmental effects, etc.)

        LOGGER.fine("Production and consumption rates calculated");
    }

    /**
     * Processes resource production and consumption for the current turn.
     */
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
                Result<Integer> result = addResource(type, net);
                resourceReport.append(type.getName()).append(": +").append(net);

                if (result.isFailure()) {
                    resourceReport.append(" (").append(result.getErrorMessage()).append(")");
                }

                resourceReport.append("\n");
            } else if (net < 0) {
                int needed = -net;
                Result<Integer> result = removeResource(type, needed);

                if (result.isFailure()) {
                    // Not enough resources - trigger shortages
                    handleShortage(type, needed);
                    resourceReport.append(type.getName()).append(": SHORTAGE (needed ").append(needed).append(")\n");
                } else {
                    resourceReport.append(type.getName()).append(": ").append(net).append("\n");
                }
            }
        }

        // Log resource changes
        LOGGER.info(resourceReport.toString());

        // Update storage capacities
        updateStorage();

        // Publish bulk resource update event
        eventBus.publish(new ResourceEvent(this, new EnumMap<>(resources)));
    }

    /**
     * Gets the change in resource amounts from the previous turn.
     *
     * @return A map of resources to their change values
     */
    public Map<ResourceType, Integer> getResourceChanges() {
        Map<ResourceType, Integer> changes = new EnumMap<>(ResourceType.class);

        for (ResourceType type : ResourceType.values()) {
            int current = resources.get(type);
            int previous = lastTurnResources.get(type);
            changes.put(type, current - previous);
        }

        return changes;
    }

    /**
     * Handles a resource shortage.
     *
     * @param type           The resource type that's short
     * @param shortageAmount The amount of the shortage
     */
    private void handleShortage(ResourceType type, int shortageAmount) {
        LOGGER.warning(String.format("SHORTAGE: %s - %d units short",
                type.getName(), shortageAmount));

        // Notify via event
        eventBus.publish(new GameEvent(this, GameEvent.EventType.RESOURCE_SHORTAGE) {
            private final ResourceType resourceType = type;
            private final int amount = shortageAmount;

            public ResourceType getResourceType() {
                return resourceType;
            }

            public int getAmount() {
                return amount;
            }
        });

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

    /**
     * Deactivates a random building due to energy shortage.
     */
    private void deactivateRandomBuilding() {
        if (game.getBuildingManager() == null) return;

        List<Building> activeBuildings = game.getBuildingManager().getActiveBuildings();
        if (activeBuildings.isEmpty()) return;

        // Pick a random building to deactivate
        int index = new Random().nextInt(activeBuildings.size());
        Building building = activeBuildings.get(index);

        building.deactivate();

        LOGGER.warning("Deactivated " + building.getName() + " due to energy shortage");

        // Publish building deactivated event
        eventBus.publish(BuildingEvent.deactivated(this, building, building.getLocation()));
    }

    /**
     * Increases storage capacity for a resource type.
     *
     * @param type The resource type
     * @param amount The amount to increase capacity by
     * @return A Result indicating success or failure
     */
    public Result<Integer> increaseCapacity(ResourceType type, int amount) {
        if (type == null) {
            return Result.failure("Resource type cannot be null");
        }

        if (amount <= 0) {
            return Result.failure("Amount must be positive");
        }

        int current = capacity.getOrDefault(type, 0);
        int newCapacity = current + amount;
        capacity.put(type, newCapacity);

        LOGGER.info(String.format("Increased %s capacity by %d. New capacity: %d",
                type.getName(), amount, newCapacity));

        return Result.success(newCapacity);
    }

    /**
     * Recalculates production based on current buildings.
     * Call this when building activation state changes.
     */
    public void recalculateProduction() {
        calculateProduction();
        LOGGER.info("Recalculated production due to building state change");

        // Publish resource change event
        eventBus.publish(new ResourceEvent(this, new EnumMap<>(resources)));
    }
}