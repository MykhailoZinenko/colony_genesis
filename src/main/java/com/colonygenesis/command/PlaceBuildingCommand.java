package com.colonygenesis.command;

import com.colonygenesis.building.Building;
import com.colonygenesis.building.BuildingManager;
import com.colonygenesis.map.Tile;
import com.colonygenesis.util.LoggerUtils;
import com.colonygenesis.util.Result;

import java.util.logging.Logger;

/**
 * Command for placing a building on a tile.
 */
public class PlaceBuildingCommand implements Command {
    private static final Logger LOGGER = LoggerUtils.getLogger(PlaceBuildingCommand.class);

    private final BuildingManager buildingManager;
    private final Building building;
    private final Tile tile;
    private Building placedBuilding;

    /**
     * Creates a new command to place a building.
     *
     * @param buildingManager The building manager
     * @param building The building to place
     * @param tile The tile to place the building on
     */
    public PlaceBuildingCommand(BuildingManager buildingManager, Building building, Tile tile) {
        this.buildingManager = buildingManager;
        this.building = building;
        this.tile = tile;
    }

    @Override
    public Result<Building> execute() {
        LOGGER.info("Executing PlaceBuildingCommand: " + building.getName() + " at " + tile);
        Result<Building> result = buildingManager.placeBuilding(building, tile);

        if (result.isSuccess()) {
            placedBuilding = result.getValueOrNull();
        }

        return result;
    }

    @Override
    public boolean isUndoable() {
        return placedBuilding != null;
    }

    @Override
    public Result<?> undo() {
        if (!isUndoable()) {
            return Result.failure("Nothing to undo");
        }

        LOGGER.info("Undoing PlaceBuildingCommand: " + building.getName() + " at " + tile);
        return buildingManager.removeBuilding(placedBuilding);
    }
}