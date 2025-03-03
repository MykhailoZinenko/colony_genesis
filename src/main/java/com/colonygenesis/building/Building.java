package com.colonygenesis.building;

import com.colonygenesis.event.EventBus;
import com.colonygenesis.event.events.BuildingEvent;
import com.colonygenesis.map.Tile;
import com.colonygenesis.map.TerrainType;
import com.colonygenesis.resource.ResourceType;
import com.colonygenesis.util.LoggerUtils;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;

public abstract class Building {
    private static final Logger LOGGER = LoggerUtils.getLogger(Building.class);
    private final EventBus eventBus = EventBus.getInstance();

    // Basic building info
    protected final String name;
    protected final String description;
    protected final BuildingType type;

    // Building status
    protected boolean active;
    protected final int constructionTime;
    protected int remainingConstructionTime;
    protected boolean resourcesDeducted; // Flag to track if construction costs have been deducted

    // Resource information
    protected final Map<ResourceType, Integer> constructionCost;
    protected final Map<ResourceType, Integer> maintenanceCost;

    // Building placement
    protected Tile location;

    public Building(String name, String description, BuildingType type, int constructionTime) {
        this.name = Objects.requireNonNull(name, "Building name cannot be null");
        this.description = Objects.requireNonNull(description, "Building description cannot be null");
        this.type = Objects.requireNonNull(type, "Building type cannot be null");
        this.constructionTime = constructionTime;
        this.remainingConstructionTime = constructionTime;
        this.active = false;
        this.resourcesDeducted = false;

        // Initialize cost maps
        this.constructionCost = new EnumMap<>(ResourceType.class);
        this.maintenanceCost = new EnumMap<>(ResourceType.class);
    }

    /**
     * Builds this building at the specified location.
     *
     * @param location The tile to build on
     * @return true if successful, false otherwise
     */
    public boolean build(Tile location) {
        if (location == null) {
            LOGGER.warning("Attempted to build " + name + " at null location");
            return false;
        }

        this.location = location;
        LOGGER.fine("Building " + name + " placement initialized at " + location);
        return true;
    }

    /**
     * Demolishes this building, removing it from its location.
     */
    public void demolish() {
        if (location != null) {
            LOGGER.info("Demolishing " + name + " at " + location);
            Tile oldLocation = location;
            location.removeBuilding();
            location = null;

            // Publish building removed event
            eventBus.publish(BuildingEvent.removed(this, this, oldLocation));
        }
    }

    /**
     * Updates this building's state for the current turn.
     */
    public void update() {
        if (remainingConstructionTime > 0) {
            LOGGER.fine("Updating building: " + name + ", remaining time: " + remainingConstructionTime);
            remainingConstructionTime--;

            if (remainingConstructionTime == 0) {
                LOGGER.info("Building " + name + " construction completed!");
                activate();

                // Publish building completed event
                eventBus.publish(BuildingEvent.completed(this, this, location));
            }
        }
    }

    /**
     * Activates this building, allowing it to produce resources.
     */
    public void activate() {
        if (!active) {
            active = true;
            LOGGER.info("Building " + name + " activated");

            // Publish building activated event
            eventBus.publish(BuildingEvent.activated(this, this, location));
        }
    }

    /**
     * Deactivates this building, stopping resource production.
     */
    public void deactivate() {
        if (active) {
            active = false;
            LOGGER.info("Building " + name + " deactivated");

            // Publish building deactivated event
            eventBus.publish(BuildingEvent.deactivated(this, this, location));
        }
    }

    /**
     * Toggles this building's active state.
     *
     * @return The new active state
     */
    public boolean toggleActive() {
        if (active) {
            deactivate();
        } else {
            activate();
        }
        return active;
    }

    /**
     * Gets this building's construction cost.
     *
     * @return A defensive copy of the construction cost map
     */
    public Map<ResourceType, Integer> getConstructionCost() {
        return new EnumMap<>(constructionCost);
    }

    /**
     * Gets this building's maintenance cost.
     *
     * @return A defensive copy of the maintenance cost map
     */
    public Map<ResourceType, Integer> getMaintenanceCost() {
        return new EnumMap<>(maintenanceCost);
    }

    /**
     * Gets the production modifier this building provides for a resource.
     *
     * @param type The resource type
     * @return The production modifier (1.0 = no modification)
     */
    public double getProductionModifier(ResourceType type) {
        return 1.0; // No modification by default
    }

    /**
     * Gets the resources produced by this building.
     *
     * @return A map of resources to their production amounts
     */
    public abstract Map<ResourceType, Integer> getProduction();

    /**
     * Checks if this building can be built on the specified tile.
     *
     * @param tile The tile to check
     * @return true if the building can be built on the tile, false otherwise
     */
    public boolean canBuildOn(Tile tile) {
        if (tile == null) {
            LOGGER.warning("Attempted to check if " + name + " can be built on null tile");
            return false;
        }

        // Check if tile is habitable
        if (!tile.isHabitable()) {
            LOGGER.fine("Tile " + tile + " is not habitable for " + name);
            return false;
        }

        // Check if tile already has a building
        if (tile.hasBuilding()) {
            LOGGER.fine("Tile " + tile + " already has a building, cannot place " + name);
            return false;
        }

        // Check terrain compatibility
        boolean compatible = isCompatibleWithTerrain(tile.getTerrainType());
        if (!compatible) {
            LOGGER.fine("Terrain " + tile.getTerrainType() + " is not compatible with " + name);
        }

        return compatible;
    }

    /**
     * Checks if this building is compatible with the specified terrain type.
     *
     * @param terrain The terrain type to check
     * @return true if compatible, false otherwise
     */
    protected boolean isCompatibleWithTerrain(TerrainType terrain) {
        // Default compatibility rules - subclasses can override
        return terrain != TerrainType.WATER;
    }

    /**
     * Checks if this building's construction is completed.
     *
     * @return true if completed, false otherwise
     */
    public boolean isCompleted() {
        return remainingConstructionTime <= 0;
    }

    /**
     * Checks if this building is active and completed.
     *
     * @return true if active and completed, false otherwise
     */
    public boolean isActive() {
        return active && isCompleted();
    }

    /**
     * Marks that resources have been deducted for this building.
     */
    public void markResourcesDeducted() {
        this.resourcesDeducted = true;
    }

    /**
     * Checks if resources have been deducted for this building.
     *
     * @return true if resources have been deducted, false otherwise
     */
    public boolean areResourcesDeducted() {
        return resourcesDeducted;
    }

    /**
     * Gets this building's name.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets this building's description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets this building's type.
     */
    public BuildingType getType() {
        return type;
    }

    /**
     * Gets this building's construction time.
     */
    public int getConstructionTime() {
        return constructionTime;
    }

    /**
     * Gets this building's remaining construction time.
     */
    public int getRemainingConstructionTime() {
        return remainingConstructionTime;
    }

    /**
     * Gets this building's location.
     */
    public Tile getLocation() {
        return location;
    }

    /**
     * Gets this building's location as an Optional.
     */
    public Optional<Tile> getLocationOptional() {
        return Optional.ofNullable(location);
    }

    @Override
    public String toString() {
        return "Building[" + name + ", type=" + type +
                ", completed=" + isCompleted() +
                ", active=" + active + "]";
    }
}