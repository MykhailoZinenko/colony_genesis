package com.colonygenesis.core;

import com.colonygenesis.building.BuildingManager;
import com.colonygenesis.event.EventBus;
import com.colonygenesis.event.events.GameStateEvent;
import com.colonygenesis.map.Planet;
import com.colonygenesis.map.PlanetType;
import com.colonygenesis.map.TerrainType;
import com.colonygenesis.resource.ResourceManager;
import com.colonygenesis.ui.UserInterface;
import com.colonygenesis.util.LoggerUtils;

import java.util.logging.Logger;

public class Game {
    private static final Logger LOGGER = LoggerUtils.getLogger(Game.class);

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
    private final EventBus eventBus;

    public Game() {
        eventBus = EventBus.getInstance();
        LOGGER.info("Game instance created");
    }

    public void initializeGame() {
        LOGGER.info("Initializing game");

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
        this.running = true;

        // Publish game initialized event
        eventBus.publish(new GameStateEvent(this, GameStateEvent.GameStateType.GAME_INITIALIZED, this));

        LOGGER.info("Game initialized successfully");
    }

    public void newGame() {
        LOGGER.info("Starting new game");

        // Reset game state
        this.currentTurn = 1;

        // Generate new planet - we'll implement planet generation later
        this.planet = new Planet("New Colony", PlanetType.TEMPERATE, 30, 20);
        this.planet.generateTerrain();

        // Reset managers
        this.turnManager = new TurnManager(this);
        this.resourceManager = new ResourceManager(this);
        this.buildingManager = new BuildingManager(this);

        // Start the game
        this.running = true;
        this.paused = false;

        // Publish game started event
        eventBus.publish(new GameStateEvent(this, GameStateEvent.GameStateType.GAME_STARTED, this));

        LOGGER.info("New game started successfully");
    }

    // Getters and setters
    public int getCurrentTurn() {
        return currentTurn;
    }

    public void setCurrentTurn(int turn) {
        if (this.currentTurn != turn) {
            LOGGER.info("Game turn changing: " + this.currentTurn + " → " + turn);
        }
        this.currentTurn = turn;
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
        boolean wasPaused = this.paused;
        this.paused = paused;

        if (wasPaused != paused) {
            if (paused) {
                LOGGER.info("Game paused");
                eventBus.publish(new GameStateEvent(this, GameStateEvent.GameStateType.GAME_PAUSED, null));
            } else {
                LOGGER.info("Game resumed");
                eventBus.publish(new GameStateEvent(this, GameStateEvent.GameStateType.GAME_RESUMED, null));
            }
        }
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

    public void setUserInterface(UserInterface ui) {
        this.userInterface = ui;
    }

    public UserInterface getUserInterface() {
        return userInterface;
    }
}