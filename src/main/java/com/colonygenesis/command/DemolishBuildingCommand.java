package com.colonygenesis.command;

import com.colonygenesis.building.Building;
import com.colonygenesis.building.BuildingManager;
import com.colonygenesis.map.Tile;
import com.colonygenesis.resource.ResourceType;
import com.colonygenesis.util.LoggerUtils;
import com.colonygenesis.util.Result;

import java.util.EnumMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Command for demolishing a building.
 */
public class DemolishBuildingCommand implements Command {
    private static final Logger LOGGER = LoggerUtils.getLogger(DemolishBuildingCommand.class);

    private final BuildingManager buildingManager;
    private final Building building;
    private Tile location;
    private Map<ResourceType, Integer> constructionCost;
    private int remainingConstructionTime;
    private boolean wasActive;

    /**
     * Creates a command to demolish a building.
     *
     * @param buildingManager The building manager
     * @param building The building to demolish
     */
    public DemolishBuildingCommand(BuildingManager buildingManager, Building building) {
        this.buildingManager = buildingManager;
        this.building = building;
    }

    @Override
    public Result<Void> execute() {
        if (building == null) {
            return Result.failure("Building cannot be null");
        }

        // Save state for undo
        location = building.getLocation();
        constructionCost = new EnumMap<>(building.getConstructionCost());
        remainingConstructionTime = building.getRemainingConstructionTime();
        wasActive = building.isActive();

        if (location == null) {
            return Result.failure("Building has no location");
        }

        LOGGER.info("Demolishing " + building.getName() + " at " + location);

        // Remove the building
        return buildingManager.removeBuilding(building);
    }

    @Override
    public boolean isUndoable() {
        return location != null && !location.hasBuilding();
    }

    @Override
    public Result<Building> undo() {
        if (!isUndoable()) {
            return Result.failure("Cannot undo demolition");
        }

        LOGGER.info("Undoing demolition of " + building.getName());

        // We can't fully restore the building's state, but we can place it back
        // Ideally we'd store more state and restore it completely
        Result<Building> result = buildingManager.placeBuilding(building, location);

        if (result.isSuccess()) {
            // Try to restore active state
            if (wasActive && building.isCompleted()) {
                building.activate();
            }
        }

        return result;
    }
}