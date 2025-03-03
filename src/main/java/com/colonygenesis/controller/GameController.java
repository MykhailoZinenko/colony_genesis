package com.colonygenesis.controller;

import com.colonygenesis.building.Building;
import com.colonygenesis.command.ActivateDeactivateBuildingCommand;
import com.colonygenesis.command.AdvancePhaseCommand;
import com.colonygenesis.command.Command;
import com.colonygenesis.command.CommandHistory;
import com.colonygenesis.command.DemolishBuildingCommand;
import com.colonygenesis.command.PlaceBuildingCommand;
import com.colonygenesis.core.Game;
import com.colonygenesis.core.TurnPhase;
import com.colonygenesis.event.EventBus;
import com.colonygenesis.event.events.TurnEvent;
import com.colonygenesis.map.Tile;
import com.colonygenesis.util.LoggerUtils;
import com.colonygenesis.util.Result;

import java.util.logging.Logger;

/**
 * Central controller for game actions, using the command pattern.
 */
public class GameController {
    private static final Logger LOGGER = LoggerUtils.getLogger(GameController.class);

    private final Game game;
    private final CommandHistory commandHistory;
    private final EventBus eventBus;

    public GameController(Game game) {
        this.game = game;
        this.commandHistory = new CommandHistory();
        this.eventBus = EventBus.getInstance();

        LOGGER.info("GameController initialized");
    }

    /**
     * Places a building on a tile.
     *
     * @param building The building to place
     * @param tile The tile to place it on
     * @return A Result indicating success or failure
     */
    public Result<?> placeBuilding(Building building, Tile tile) {
        LOGGER.info("Requesting to place " + building.getName() + " at " + tile);

        // Create and execute the command
        Command command = new PlaceBuildingCommand(game.getBuildingManager(), building, tile);
        return commandHistory.executeCommand(command);
    }

    /**
     * Demolishes a building.
     *
     * @param building The building to demolish
     * @return A Result indicating success or failure
     */
    public Result<?> demolishBuilding(Building building) {
        LOGGER.info("Requesting to demolish " + building.getName());

        // Create and execute the command
        Command command = new DemolishBuildingCommand(game.getBuildingManager(), building);
        return commandHistory.executeCommand(command);
    }

    /**
     * Activates or deactivates a building.
     *
     * @param building The building to modify
     * @param activate Whether to activate (true) or deactivate (false)
     * @return A Result indicating success or failure
     */
    public Result<?> setActivateBuilding(Building building, boolean activate) {
        LOGGER.info("Requesting to set " + building.getName() + " active state to " + activate);

        // Execute the command
        Command command = new ActivateDeactivateBuildingCommand(building, activate);
        Result<?> result = commandHistory.executeCommand(command);

        // Force resource recalculation
        game.getResourceManager().recalculateProduction();

        return result;
    }

    /**
     * Toggles a building's active state.
     *
     * @param building The building to toggle
     * @return A Result indicating success or failure
     */
    public Result<?> toggleBuildingActive(Building building) {
        LOGGER.info("Requesting to toggle " + building.getName() + " active state");

        // Create and execute the command
        Command command = new ActivateDeactivateBuildingCommand(building, !building.isActive());
        return commandHistory.executeCommand(command);
    }

    /**
     * Advances to the next phase in the turn.
     *
     * @return A Result indicating success or failure
     */
    public Result<TurnPhase> advancePhase() {
        LOGGER.info("Requesting to advance to the next phase");

        // Create and execute the command
        Command command = new AdvancePhaseCommand(this);
        Result<?> result = commandHistory.executeCommand(command);

        // Cast the result to the correct type
        if (result.isSuccess()) {
            TurnPhase newPhase = game.getTurnManager().getCurrentPhase();
            return Result.success(newPhase);
        } else {
            return Result.failure(result.getErrorMessage());
        }
    }

    /**
     * Executes the current phase.
     *
     * @return A Result indicating success or failure
     */
    public Result<TurnPhase> executeCurrentPhase() {
        TurnPhase currentPhase = game.getTurnManager().getCurrentPhase();
        LOGGER.info("Executing phase: " + currentPhase.getName());

        // Mark current phase as completed
        game.getTurnManager().setPhaseCompleted(true);

        // Execute the current phase
        game.getTurnManager().executeCurrentPhase();
        game.getTurnManager().advancePhase();

        TurnPhase newPhase = game.getTurnManager().getCurrentPhase();
        LOGGER.info("Phase execution complete, now in: " + newPhase.getName());

        // Publish phase changed event
        eventBus.publish(TurnEvent.phaseChanged(
                this, game.getCurrentTurn(), newPhase, currentPhase));

        return Result.success(newPhase);
    }



    /**
     * Undoes the last action if possible.
     *
     * @return A Result indicating success or failure
     */
    public Result<?> undo() {
        LOGGER.info("Attempting to undo last action");
        return commandHistory.undo();
    }

    /**
     * Redoes the last undone action if possible.
     *
     * @return A Result indicating success or failure
     */
    public Result<?> redo() {
        LOGGER.info("Attempting to redo last undone action");
        return commandHistory.redo();
    }

    /**
     * Checks if there are actions that can be undone.
     */
    public boolean canUndo() {
        return commandHistory.canUndo();
    }

    /**
     * Checks if there are actions that can be redone.
     */
    public boolean canRedo() {
        return commandHistory.canRedo();
    }

    /**
     * Gets the current game instance.
     */
    public Game getGame() {
        return game;
    }
}