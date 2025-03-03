package com.colonygenesis.command;

import com.colonygenesis.controller.GameController;
import com.colonygenesis.core.TurnPhase;
import com.colonygenesis.util.LoggerUtils;
import com.colonygenesis.util.Result;

import java.util.logging.Logger;

/**
 * Command for advancing to the next game phase.
 */
public class AdvancePhaseCommand implements Command {
    private static final Logger LOGGER = LoggerUtils.getLogger(AdvancePhaseCommand.class);

    private final GameController gameController;
    private TurnPhase previousPhase;
    private int previousTurn;

    /**
     * Creates a command to advance to the next phase.
     *
     * @param gameController The game controller
     */
    public AdvancePhaseCommand(GameController gameController) {
        this.gameController = gameController;
    }

    @Override
    public Result<TurnPhase> execute() {
        LOGGER.info("Executing AdvancePhaseCommand");

        // Store state for potential undo
        previousPhase = gameController.getGame().getTurnManager().getCurrentPhase();
        previousTurn = gameController.getGame().getCurrentTurn();

        // Execute the current phase and advance to the next
        return gameController.executeCurrentPhase();
    }

    @Override
    public boolean isUndoable() {
        // Phase advancement is not undoable for simplicity
        // In a more complex implementation, we might store the full game state
        return false;
    }

    @Override
    public Result<?> undo() {
        return Result.failure("Cannot undo phase advancement");
    }
}