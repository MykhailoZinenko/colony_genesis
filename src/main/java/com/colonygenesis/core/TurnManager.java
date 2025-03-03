// Update TurnManager.java
package com.colonygenesis.core;

import javafx.scene.control.Alert;

public class TurnManager {
    private final Game game;
    private int turnNumber;
    private TurnPhase currentPhase;
    private boolean phaseCompleted;

    public TurnManager(Game game) {
        this.game = game;
        this.turnNumber = 1;
        this.currentPhase = TurnPhase.PLANNING;
        this.phaseCompleted = false;
    }

    public void advanceTurn() {
        turnNumber++;
        currentPhase = TurnPhase.PLANNING;
        phaseCompleted = false;

        System.out.println("Starting turn " + turnNumber);

        game.setCurrentTurn(turnNumber);

        // Notify player of new turn
        if (game.getUserInterface() != null) {
            game.getUserInterface().showMessage("Turn " + turnNumber + " started.", "info");
        }
    }

    public void advancePhase() {
        // Make sure the current phase is completed if it requires input
        if (currentPhase.requiresInput() && !phaseCompleted) {
            System.out.println("Warning: Attempting to advance from " + currentPhase.getName() + " which was not completed");

            // Allow advancement in development for testing
            // In final game, this would prevent advancement until phase is completed
            // return;
        }

        // Get the ordinal value of the current phase
        int ordinal = currentPhase.ordinal();

        // Get the next phase (or cycle back to PLANNING)
        TurnPhase[] phases = TurnPhase.values();
        currentPhase = phases[(ordinal + 1) % phases.length];
        phaseCompleted = false;

        System.out.println("Phase changed to: " + currentPhase.getName());

        // If the phase doesn't require input, execute it immediately
        if (!currentPhase.requiresInput()) {
            executeCurrentPhase();
        }

        // Update the UI
        if (game.getUserInterface() != null) {
            game.getUserInterface().updateDisplay();
            game.getUserInterface().showMessage("Now in " + currentPhase.getName() + " phase.", "info");
        }
    }

    public void executeCurrentPhase() {
        System.out.println("Executing phase: " + currentPhase.getName());

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

        // After executing the phase, if it doesn't require input, move to the next one
        if (!currentPhase.requiresInput() && currentPhase != TurnPhase.END_TURN) {
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