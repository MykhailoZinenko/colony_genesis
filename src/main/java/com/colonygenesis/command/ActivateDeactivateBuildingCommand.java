package com.colonygenesis.command;

import com.colonygenesis.building.Building;
import com.colonygenesis.util.LoggerUtils;
import com.colonygenesis.util.Result;

import java.util.logging.Logger;

/**
 * Command for toggling a building's active state.
 */
public class ActivateDeactivateBuildingCommand implements Command {
    private static final Logger LOGGER = LoggerUtils.getLogger(ActivateDeactivateBuildingCommand.class);

    private final Building building;
    private final boolean targetState; // true = activate, false = deactivate
    private boolean previousState;

    /**
     * Creates a command to set a building's active state.
     *
     * @param building The building to modify
     * @param activate Whether to activate (true) or deactivate (false)
     */
    public ActivateDeactivateBuildingCommand(Building building, boolean activate) {
        this.building = building;
        this.targetState = activate;
    }

    @Override
    public Result<Boolean> execute() {
        if (building == null) {
            return Result.failure("Building cannot be null");
        }

        if (!building.isCompleted()) {
            return Result.failure("Cannot change active state of building under construction");
        }

        // Store previous state for undo
        previousState = building.isActive();

        // Only change state if it's different
        if (previousState != targetState) {
            LOGGER.info("Changing " + building.getName() + " active state from " +
                    previousState + " to " + targetState);

            if (targetState) {
                building.activate();
            } else {
                building.deactivate();
            }

            return Result.success(targetState);
        }

        // No change needed
        return Result.success(previousState);
    }

    @Override
    public boolean isUndoable() {
        return building != null && building.isCompleted();
    }

    @Override
    public Result<Boolean> undo() {
        if (!isUndoable()) {
            return Result.failure("Cannot undo activation state change");
        }

        LOGGER.info("Undoing " + building.getName() + " active state change, reverting to " + previousState);

        if (previousState) {
            building.activate();
        } else {
            building.deactivate();
        }

        return Result.success(previousState);
    }
}