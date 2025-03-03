package com.colonygenesis.core;

import com.colonygenesis.building.BuildingManager;
import com.colonygenesis.map.Planet;
import com.colonygenesis.map.PlanetType;
import com.colonygenesis.map.TerrainType;
import com.colonygenesis.resource.ResourceManager;
import com.colonygenesis.ui.UserInterface;

public class Game {
    // Game state
    private boolean initialized = false;
    private boolean running = false;
    private boolean paused = false;

    // Game components
    private int currentTurn = 0;
    private Planet planet;
    private ResourceManager resourceManager;
    private TurnManager turnManager;
    private UserInterface userInterface;
    private BuildingManager buildingManager;

    public Game() {
        // Empty constructor
    }

    // In the Game class's initializeGame method:

    // Update initializeGame method:
    public void initializeGame() {
        // Initialize managers
        this.turnManager = new TurnManager(this);
        this.resourceManager = new ResourceManager(this);
        this.buildingManager = new BuildingManager(this);

        // Create a default planet
        this.planet = new Planet("New Colony", PlanetType.TEMPERATE, 30, 20);
        this.planet.generateTerrain();

        // Initialize terrain resource modifiers
        for (TerrainType terrain : TerrainType.values()) {
            terrain.initializeResourceModifiers();
        }

        // Set initial game state
        this.currentTurn = 1;
        this.initialized = true;
    }

    public void newGame() {
        // Reset game state
        this.currentTurn = 1;

        // Generate new planet
        // We'll implement planet generation later

        // Start the game
        this.running = true;
        this.paused = false;
    }

    public void processTurn() {
        if (!running || paused) return;

        // Make sure the current phase is marked as completed
        turnManager.setPhaseCompleted(true);

        // Process the current phase
        turnManager.executeCurrentPhase();
    }

    // Add a method to complete the current phase:
    public void completeCurrentPhase() {
        if (!running || paused) return;

        // Mark the current phase as completed
        turnManager.setPhaseCompleted(true);

        // Advance to the next phase
        turnManager.advancePhase();

        // Update UI
        if (userInterface != null) {
            userInterface.updateDisplay();
        }
    }

    // Getters and setters
    public int getCurrentTurn() {
        return currentTurn;
    }

    // Add to Game.java:
    public void setCurrentTurn(int turn) {
        this.currentTurn = turn;
        System.out.println("Game turn set to: " + currentTurn);
    }

    public boolean isInitialized() {
        return initialized;
    }

    public boolean isRunning() {
        return running;
    }

    public boolean isPaused() {
        return paused;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public TurnManager getTurnManager() {
        return turnManager;
    }

    public ResourceManager getResourceManager() {
        return resourceManager;
    }

    public BuildingManager getBuildingManager() {
        return buildingManager;
    }

    public Planet getPlanet() {
        return planet;
    }

    // Add this method:
    public void setUserInterface(UserInterface ui) {
        this.userInterface = ui;
    }

    public UserInterface getUserInterface() {
        return userInterface;
    }
}