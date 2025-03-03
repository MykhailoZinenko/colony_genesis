package com.colonygenesis.core;

import com.colonygenesis.event.EventBus;
import com.colonygenesis.event.events.TurnEvent;
import com.colonygenesis.util.LoggerUtils;

import java.util.logging.Logger;

public class TurnManager {
    private static final Logger LOGGER = LoggerUtils.getLogger(TurnManager.class);

    private final Game game;
    private final EventBus eventBus;
    private int turnNumber;
    private TurnPhase currentPhase;
    private boolean phaseCompleted;

    public TurnManager(Game game) {
        this.game = game;
        this.eventBus = EventBus.getInstance();
        this.turnNumber = 1;
        this.currentPhase = TurnPhase.PLANNING;
        this.phaseCompleted = false;

        LOGGER.info("TurnManager initialized at turn 1, phase: PLANNING");
    }

    public void advanceTurn() {
        int previousTurn = turnNumber;
        turnNumber++;
        currentPhase = TurnPhase.PLANNING;
        phaseCompleted = false;

        LOGGER.info("Starting turn " + turnNumber);
        game.setCurrentTurn(turnNumber);

        // Publish turn advanced event
        eventBus.publish(TurnEvent.turnAdvanced(this, turnNumber, previousTurn));
    }

    public void advancePhase() {
        // Make sure the current phase is completed if it requires input
        if (currentPhase.requiresInput() && !phaseCompleted) {
            LOGGER.warning("Attempting to advance from " + currentPhase.getName() + " which was not completed");
            // Allow advancement in development for testing
        }

        // Get the ordinal value of the current phase
        int ordinal = currentPhase.ordinal();
        TurnPhase previousPhase = currentPhase;

        // Get the next phase (or cycle back to PLANNING)
        TurnPhase[] phases = TurnPhase.values();
        currentPhase = phases[(ordinal + 1) % phases.length];
        phaseCompleted = false;

        LOGGER.info("Phase changed to: " + currentPhase.getName());

        // Publish phase changed event
        eventBus.publish(TurnEvent.phaseChanged(this, turnNumber, currentPhase, previousPhase));

        // If the phase doesn't require input, execute it immediately
        if (!currentPhase.requiresInput()) {
            executeCurrentPhase();
        }
    }

    public void executeCurrentPhase() {
        LOGGER.info("Executing phase: " + currentPhase.getName());

        switch (currentPhase) {
            case PLANNING:
                // Planning phase is for player decisions
                // Nothing to execute automatically
                break;

            case BUILDING:
                // Process construction progress
                if (game.getBuildingManager() != null) {
                    game.getBuildingManager().updateConstructionQueue();
                }
                break;

            case PRODUCTION:
                // Execute production phase logic
                if (game.getResourceManager() != null) {
                    game.getResourceManager().processTurn();
                }
                break;

            case EVENTS:
                // Execute events phase logic
                // We'll implement this later
                break;

            case END_TURN:
                // Execute end turn logic
                // If we're at the end turn phase, advance to the next turn
                advanceTurn();
                break;
        }

        // Mark phase as completed
        phaseCompleted = true;

        System.out.println(currentPhase.requiresInput() + " " + currentPhase.getName() + " " + currentPhase + " " + (!currentPhase.requiresInput() && currentPhase != TurnPhase.END_TURN));
        // After executing the phase, if it doesn't require input, move to the next one
        if (!currentPhase.requiresInput() && currentPhase != TurnPhase.END_TURN) {
            System.out.println(currentPhase.requiresInput() + " " + currentPhase.getName() + " " + currentPhase);
            advancePhase();
        }
    }

    public TurnPhase getCurrentPhase() {
        return currentPhase;
    }

    public int getTurnNumber() {
        return turnNumber;
    }

    public boolean isPhaseCompleted() {
        return phaseCompleted;
    }

    public void setPhaseCompleted(boolean completed) {
        this.phaseCompleted = completed;
    }
}