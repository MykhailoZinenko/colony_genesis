package com.colonygenesis.ui;

import com.colonygenesis.building.Building;
import com.colonygenesis.controller.GameController;
import com.colonygenesis.core.Game;
import com.colonygenesis.core.TurnPhase;
import com.colonygenesis.event.EventBus;
import com.colonygenesis.event.EventListener;
import com.colonygenesis.event.GameEvent;
import com.colonygenesis.event.events.BuildingEvent;
import com.colonygenesis.event.events.ResourceEvent;
import com.colonygenesis.event.events.TileEvent;
import com.colonygenesis.event.events.TurnEvent;
import com.colonygenesis.map.Tile;
import com.colonygenesis.resource.ResourceType;
import com.colonygenesis.ui.notification.NotificationManager;
import com.colonygenesis.ui.notification.NotificationType;
import com.colonygenesis.util.LoggerUtils;
import com.colonygenesis.util.Result;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Polygon;
import org.kordamp.bootstrapfx.BootstrapFX;

import java.util.Map;
import java.util.logging.Logger;

public class UserInterface extends BorderPane implements EventListener {
    private static final Logger LOGGER = LoggerUtils.getLogger(UserInterface.class);

    private final Game game;
    private final GameController gameController;
    private final EventBus eventBus;
    private final NotificationManager notificationManager;

    // UI Components
    private MapView mapView;
    private ResourcePanel resourcePanel;
    private BuildingPanel buildingPanel;
    private BuildingDetailsPanel buildingDetailsPanel;
    private InfoPanel infoPanel;
    private Label turnLabel;
    private Label phaseLabel;
    private Button nextPhaseButton;
    private Button undoButton;
    private Button redoButton;

    private Building selectedBuilding;

    public UserInterface(Game game) {
        this.game = game;
        this.gameController = new GameController(game);
        this.eventBus = EventBus.getInstance();
        this.notificationManager = new NotificationManager();

        // Register as an event listener
        eventBus.register(this,
                GameEvent.EventType.RESOURCE_CHANGED,
                GameEvent.EventType.BUILDING_PLACED,
                GameEvent.EventType.BUILDING_COMPLETED,
                GameEvent.EventType.BUILDING_ACTIVATED,
                GameEvent.EventType.BUILDING_DEACTIVATED,
                GameEvent.EventType.TURN_ADVANCED,
                GameEvent.EventType.PHASE_CHANGED,
                GameEvent.EventType.TILE_UPDATED,
                GameEvent.EventType.GAME_STATE_CHANGED
        );

        // Apply Bootstrap styling
        this.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());

        // Initialize all UI components
        initialize();

        LOGGER.info("UserInterface initialized");
    }

    public void initialize() {
        // Create map view
        mapView = new MapView(game.getPlanet().getGrid());
        mapView.addEventHandler(com.colonygenesis.ui.TileEvent.TILE_SELECTED, this::handleTileSelected);
        mapView.addEventHandler(com.colonygenesis.ui.TileEvent.TILE_HOVER, this::handleTileHover);
        mapView.addEventHandler(com.colonygenesis.ui.TileEvent.TILE_ACTION, this::handleTileAction);
        mapView.addEventHandler(BuildingActionEvent.TOGGLE_ACTIVE, this::handleToggleActive);
        mapView.addEventHandler(BuildingActionEvent.DEMOLISH, this::handleDemolishBuilding);
        mapView.addEventHandler(BuildingActionEvent.SHOW_INFO, this::handleShowInfo);

        // Create panels
        resourcePanel = new ResourcePanel(game);
        infoPanel = new InfoPanel();
        buildingPanel = new BuildingPanel(game);
        buildingDetailsPanel = new BuildingDetailsPanel();

        buildingPanel.setOnBuildingSelected(this::handleBuildingSelected);

        // Create turn controls
        HBox turnControls = createTurnControls();

        // Create the right panel containing info and building panels
        VBox rightPanel = createRightPanel();

        // Add notification area
        VBox notificationArea = notificationManager.getNotificationArea();

        // Layout components
        setCenter(mapView);
        setTop(resourcePanel);
        setRight(rightPanel);
        setBottom(turnControls);

        // Add notification area to the bottom of the right panel
        rightPanel.getChildren().add(notificationArea);

        // Set margins for better spacing
        setMargin(resourcePanel, new Insets(10));
        setMargin(rightPanel, new Insets(10));
        setMargin(turnControls, new Insets(10));

        // Initial UI update
        updateDisplay();
    }

    private VBox createRightPanel() {
        VBox rightPanel = new VBox(10);
        rightPanel.getChildren().addAll(infoPanel, buildingDetailsPanel, buildingPanel);
        return rightPanel;
    }

    private void handleBuildingSelected(Building building) {
        // Make sure we have a new instance instead of a template building
        this.selectedBuilding = building; // The building from BuildingPanel is already a new instance

        // Log building info for debugging
        LOGGER.info("Selected " + building.getName() + " for placement");
        LOGGER.info("Construction time: " + building.getConstructionTime() + " turns");

        showNotification("Select a tile to place " + building.getName(), NotificationType.INFO);
    }

    // Add this method to UserInterface.java
    private void handleTileSelected(com.colonygenesis.ui.TileEvent event) {
        Tile tile = event.getTile();

        // Update the info panel
        infoPanel.update(tile);

        // Update building details if there's a building
        if (tile != null && tile.hasBuilding()) {
            buildingDetailsPanel.update(tile.getBuilding());
        } else {
            buildingDetailsPanel.clear();
        }

        // If we have a building selected, try to place it
        if (selectedBuilding != null) {
            Result<?> result = gameController.placeBuilding(selectedBuilding, tile);

            if (result.isSuccess()) {
                showNotification(selectedBuilding.getName() + " placed successfully", NotificationType.SUCCESS);
            } else {
                showNotification(result.getErrorMessage(), NotificationType.ERROR);
            }

            // Clear selection regardless of result
            selectedBuilding = null;
            buildingPanel.clearSelection();
        }
    }

    private HBox createTurnControls() {
        HBox controls = new HBox(10);
        controls.setPadding(new Insets(10));

        turnLabel = new Label("Turn: " + game.getCurrentTurn());
        turnLabel.getStyleClass().add("h4");

        phaseLabel = new Label("Phase: " + game.getTurnManager().getCurrentPhase().getName());
        phaseLabel.getStyleClass().add("h4");

        nextPhaseButton = new Button("Next Phase");
        nextPhaseButton.getStyleClass().addAll("btn", "btn-primary");
        nextPhaseButton.setOnAction(e -> {
            Result<TurnPhase> result = gameController.advancePhase();

            if (result.isSuccess()) {
                TurnPhase newPhase = result.getValueOrNull();
                showNotification("Advanced to " + newPhase.getName() + " phase", NotificationType.INFO);

                // Explicitly update UI to reflect the new phase
                phaseLabel.setText("Phase: " + newPhase.getName());
            } else {
                showNotification(result.getErrorMessage(), NotificationType.ERROR);
            }
        });

        undoButton = new Button("Undo");
        undoButton.getStyleClass().addAll("btn", "btn-secondary");
        undoButton.setOnAction(e -> {
            Result<?> result = gameController.undo();

            if (result.isSuccess()) {
                showNotification("Action undone", NotificationType.INFO);
            } else {
                showNotification(result.getErrorMessage(), NotificationType.WARNING);
            }
        });
        undoButton.setDisable(!gameController.canUndo());

        redoButton = new Button("Redo");
        redoButton.getStyleClass().addAll("btn", "btn-secondary");
        redoButton.setOnAction(e -> {
            Result<?> result = gameController.redo();

            if (result.isSuccess()) {
                showNotification("Action redone", NotificationType.INFO);
            } else {
                showNotification(result.getErrorMessage(), NotificationType.WARNING);
            }
        });
        redoButton.setDisable(!gameController.canRedo());

        controls.getChildren().addAll(turnLabel, phaseLabel, nextPhaseButton, undoButton, redoButton);

        return controls;
    }

    public void showNotification(String message, NotificationType type) {
        LOGGER.info("Notification: " + type + " - " + message);
        notificationManager.showNotification(message, type);
    }

    public void updateDisplay() {
        // Update turn and phase labels
        turnLabel.setText("Turn: " + game.getCurrentTurn());
        phaseLabel.setText("Phase: " + game.getTurnManager().getCurrentPhase().getName());

        // Update undo/redo buttons
        undoButton.setDisable(!gameController.canUndo());
        redoButton.setDisable(!gameController.canRedo());

        // Update resource panel
        resourcePanel.update(
                game.getResourceManager().getAllResources(),
                game.getResourceManager().getAllNetProduction()
        );

        // Refresh map display
        mapView.renderGrid();
    }

    private void handleTileHover(com.colonygenesis.ui.TileEvent event) {
        Tile tile = event.getTile();
        // Quick info or tooltip logic would go here
    }

    private void handleTileAction(com.colonygenesis.ui.TileEvent event) {
        Tile tile = event.getTile();
        if (tile.hasBuilding() && tile.getBuilding().isCompleted()) {
            Building building = tile.getBuilding();
            boolean newActiveState = !building.isActive();

            Result<?> result = gameController.setActivateBuilding(building, newActiveState);

            if (result.isSuccess()) {
                if (newActiveState) {
                    showNotification(building.getName() + " activated", NotificationType.SUCCESS);
                } else {
                    showNotification(building.getName() + " deactivated", NotificationType.INFO);
                }
            } else {
                showNotification(result.getErrorMessage(), NotificationType.ERROR);
            }
        }
    }

    // Add this method to UserInterface.java
    private void handleDemolishBuilding(BuildingActionEvent event) {
        Building building = event.getBuilding();

        // Create a confirmation dialog
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Demolish");
        alert.setHeaderText("Demolish " + building.getName());
        alert.setContentText("Are you sure you want to demolish this building?");

        // Show dialog and wait for response
        alert.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                Result<?> cmdResult = gameController.demolishBuilding(building);

                if (cmdResult.isSuccess()) {
                    showNotification(building.getName() + " demolished", NotificationType.SUCCESS);

                    // Force update the UI
                    resourcePanel.update(
                            game.getResourceManager().getAllResources(),
                            game.getBuildingManager().calculateTotalProduction()
                    );
                } else {
                    showNotification("Failed to demolish: " + cmdResult.getErrorMessage(), NotificationType.ERROR);
                }
            }
        });
    }

    private void handleShowInfo(BuildingActionEvent event) {
        Building building = event.getBuilding();

        // Show detailed building info
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Building Information");
        alert.setHeaderText(building.getName());

        StringBuilder content = new StringBuilder();
        content.append("Type: ").append(building.getType()).append("\n");
        content.append("Status: ").append(building.isCompleted() ?
                (building.isActive() ? "Active" : "Inactive") :
                "Under Construction").append("\n");

        if (!building.isCompleted()) {
            content.append("Construction time remaining: ")
                    .append(building.getRemainingConstructionTime())
                    .append(" turns\n");
        }

        content.append("\nProduction:\n");
        Map<ResourceType, Integer> production = building.getProduction();
        if (production.isEmpty()) {
            content.append("None\n");
        } else {
            for (Map.Entry<ResourceType, Integer> entry : production.entrySet()) {
                ResourceType type = entry.getKey();
                int amount = entry.getValue();

                if (amount > 0) {
                    content.append("+").append(amount).append(" ").append(type.getName()).append("\n");
                } else if (amount < 0) {
                    content.append(amount).append(" ").append(type.getName()).append("\n");
                }
            }
        }

        alert.setContentText(content.toString());
        alert.showAndWait();
    }

    private void handleToggleActive(BuildingActionEvent event) {
        Building building = event.getBuilding();
        boolean newState = !building.isActive();

        Result<?> result = gameController.setActivateBuilding(building, newState);

        if (result.isSuccess()) {
            if (building.isActive()) {
                showNotification(building.getName() + " activated", NotificationType.SUCCESS);
            } else {
                showNotification(building.getName() + " deactivated", NotificationType.INFO);
            }
        } else {
            showNotification(result.getErrorMessage(), NotificationType.ERROR);
        }

        // Force update of resource display
        resourcePanel.update(
                game.getResourceManager().getAllResources(),
                game.getBuildingManager().calculateTotalProduction()
        );
    }

    @Override
    public void onEvent(GameEvent event) {
        // Ensure UI updates happen on the JavaFX thread
        Platform.runLater(() -> {
            switch (event.getType()) {
                case RESOURCE_CHANGED:
                    handleResourceChanged((ResourceEvent) event);
                    break;
                case BUILDING_PLACED:
                    handleBuildingPlaced((BuildingEvent) event);
                    break;
                case BUILDING_COMPLETED:
                    handleBuildingCompleted((BuildingEvent) event);
                    break;
                case BUILDING_ACTIVATED:
                case BUILDING_DEACTIVATED:
                    handleBuildingStatusChanged((BuildingEvent) event);
                    break;
                case TURN_ADVANCED:
                    handleTurnAdvanced((TurnEvent) event);
                    break;
                case PHASE_CHANGED:
                    handlePhaseChanged((TurnEvent) event);
                    break;
                case TILE_UPDATED:
                    handleTileUpdated((TileEvent) event);
                    break;
                case GAME_STATE_CHANGED:
                    // Handle game state changes
                    updateDisplay();
                    break;
            }
        });
    }

    private void handleResourceChanged(ResourceEvent event) {
        // Update resource display
        resourcePanel.update(
                game.getResourceManager().getAllResources(),
                game.getResourceManager().getAllNetProduction()
        );

        // Show notification for significant changes
        if (!event.isBulkUpdate() && event.getDelta() != 0) {
            ResourceType type = event.getResourceType();
            int delta = event.getDelta();

            if (delta < 0 && Math.abs(delta) > 100) {
                showNotification("oLost " + Math.abs(delta) + " " + type.getName(),
                        NotificationType.WARNING);
            } else if (delta > 100) {
                showNotification("Gained " + delta + " " + type.getName(),
                        NotificationType.SUCCESS);
            }
        }
    }

    private void handleBuildingPlaced(BuildingEvent event) {
        showNotification(event.getBuilding().getName() + " placed at " + event.getTile(),
                NotificationType.SUCCESS);
        mapView.renderGrid();
    }

    private void handleBuildingCompleted(BuildingEvent event) {
        showNotification(event.getBuilding().getName() + " construction completed!",
                NotificationType.SUCCESS);
        mapView.renderTile(event.getTile());
    }

    private void handleBuildingStatusChanged(BuildingEvent event) {
        Building building = event.getBuilding();
        boolean isActive = building.isActive();

        if (isActive) {
            showNotification(building.getName() + " activated", NotificationType.INFO);
        } else {
            showNotification(building.getName() + " deactivated", NotificationType.WARNING);
        }
        mapView.renderTile(event.getTile());
    }

    private void handleTurnAdvanced(TurnEvent event) {
        LOGGER.info("UI handling turn advanced event: " + event.getPreviousTurn() +
                " → " + event.getTurnNumber());

        Platform.runLater(() -> {
            turnLabel.setText("Turn: " + event.getTurnNumber());
            showNotification("Turn " + event.getTurnNumber() + " started", NotificationType.INFO);
        });
    }

    private void handlePhaseChanged(TurnEvent event) {
        phaseLabel.setText("Phase: " + event.getPhase().getName());
    }

    private void handleTileUpdated(TileEvent event) {
        mapView.renderTile(event.getTile());
    }

    @Override
    public boolean isInterestedIn(GameEvent.EventType eventType) {
        return true; // Interested in all events
    }

    private void setupBuildingContextMenu(Building building, Polygon hexagon) {
        if (building == null) return;

        ContextMenu contextMenu = new ContextMenu();

        MenuItem activateItem = new MenuItem(building.isActive() ? "Deactivate" : "Activate");
        activateItem.setOnAction(e -> {
            fireEvent(new BuildingActionEvent(BuildingActionEvent.TOGGLE_ACTIVE, building));
        });

        MenuItem infoItem = new MenuItem("Info");
        infoItem.setOnAction(e -> {
            fireEvent(new BuildingActionEvent(BuildingActionEvent.SHOW_INFO, building));
        });

        MenuItem demolishItem = new MenuItem("Demolish");
        demolishItem.setOnAction(e -> {
            fireEvent(new BuildingActionEvent(BuildingActionEvent.DEMOLISH, building));
        });

        contextMenu.getItems().addAll(activateItem, infoItem, demolishItem);

        // Show context menu on right-click
        hexagon.setOnContextMenuRequested(e -> {
            contextMenu.show(hexagon, e.getScreenX(), e.getScreenY());
        });
    }
}