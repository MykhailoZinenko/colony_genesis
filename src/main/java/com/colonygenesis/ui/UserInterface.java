package com.colonygenesis.ui;

import com.colonygenesis.building.Building;
import com.colonygenesis.building.BuildingFactory;
import com.colonygenesis.core.Game;
import com.colonygenesis.core.TurnPhase;
import com.colonygenesis.map.Tile;

import com.colonygenesis.resource.ResourceType;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.kordamp.bootstrapfx.BootstrapFX;

import java.util.Map;

public class UserInterface extends BorderPane {
    private Game game;

    // UI Components
    private MapView mapView;
    private ResourcePanel resourcePanel;
    private BuildingPanel buildingPanel;
    private InfoPanel infoPanel;
    private Label turnLabel;
    private Label phaseLabel;

    private Building selectedBuilding;

    public UserInterface(Game game) {
        this.game = game;

        // Apply Bootstrap styling
        this.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());

        // Initialize all UI components
        initialize();
    }

    // Update the initialize method (fix the panel setup):
    public void initialize() {
        // Create map view
        mapView = new MapView(game.getPlanet().getGrid());

        // Set up event handling
        mapView.addEventHandler(TileEvent.TILE_SELECTED, this::handleTileSelected);
        mapView.addEventHandler(TileEvent.TILE_HOVER, this::handleTileHover);

        // Create panels
        resourcePanel = new ResourcePanel(game);
        infoPanel = new InfoPanel();
        buildingPanel = new BuildingPanel(game);

        // Set up building selection
        buildingPanel.setOnBuildingSelected(this::handleBuildingSelected);

        // Create turn controls
        HBox turnControls = createTurnControls();

        // Create the right panel containing info and building panels
        VBox rightPanel = createRightPanel();

        // Layout components
        setCenter(mapView);
        setTop(resourcePanel);
        setRight(rightPanel);
        setBottom(turnControls);

        // Set margins for better spacing
        setMargin(resourcePanel, new Insets(10));
        setMargin(rightPanel, new Insets(10));
        setMargin(turnControls, new Insets(10));
    }

    // Create a method to stack the info and building panels:
    private VBox createRightPanel() {
        VBox rightPanel = new VBox(10);
        rightPanel.getChildren().addAll(infoPanel, buildingPanel);
        return rightPanel;
    }

    // Add a method to handle building selection:
    private void handleBuildingSelected(Building building) {
        this.selectedBuilding = building;

        // Update cursor or highlight to show building placement mode
        showMessage("Select a tile to place " + building.getName(), "info");
    }

    // Update the handleTileSelected method to handle building placement:
    private void handleTileSelected(TileEvent event) {
        Tile tile = event.getTile();
        infoPanel.update(tile);

        // If we have a building selected, try to place it
        if (selectedBuilding != null) {
            // Create a new instance of the building (so we don't reuse the template)
            Building buildingToBuild = null;

            // This is a bit hacky, but we're creating a new instance based on the name
            // In a real implementation, we'd have a proper factory method
            String name = selectedBuilding.getName();
            if (name.equals("Farm")) {
                buildingToBuild = BuildingFactory.createFarm();
            } else if (name.equals("Mine")) {
                buildingToBuild = BuildingFactory.createMine();
            } else if (name.equals("Solar Panel")) {
                buildingToBuild = BuildingFactory.createSolarPanel();
            } else if (name.equals("Water Extractor")) {
                buildingToBuild = BuildingFactory.createWaterExtractor();
            } else if (name.equals("Habitation Dome")) {
                buildingToBuild = BuildingFactory.createHabitationDome();
            } else if (name.equals("Luxury Apartments")) {
                buildingToBuild = BuildingFactory.createLuxuryApartments();
            }

            if (buildingToBuild != null) {
                System.out.println("Building: " + buildingToBuild.getName() + " : Tile: -" + tile);
                boolean success = game.getBuildingManager().placeBuilding(buildingToBuild, tile);

                if (success) {
                    showMessage(buildingToBuild.getName() + " placed successfully.", "success");
                    mapView.renderGrid(); // Update the map view

                    // Update resource display
                    updateDisplay();
                } else {
                    showMessage("Cannot place " + buildingToBuild.getName() + " here.", "error");
                }
            }

            // Clear selection
            selectedBuilding = null;
            buildingPanel.clearSelection();
        }
    }

    private Label statusLabel;

    // In UserInterface.java, update the createTurnControls method:
    private HBox createTurnControls() {
        HBox controls = new HBox(10);
        controls.setPadding(new Insets(10));

        turnLabel = new Label("Turn: " + game.getCurrentTurn());
        turnLabel.getStyleClass().add("h4");

        phaseLabel = new Label("Phase: " + game.getTurnManager().getCurrentPhase().getName());
        phaseLabel.getStyleClass().add("h4");

        statusLabel = new Label("");
        statusLabel.getStyleClass().add("text-info");

        Button nextPhaseButton = new Button("Next Phase");
        nextPhaseButton.getStyleClass().addAll("btn", "btn-primary");
        nextPhaseButton.setOnAction(e -> {
            // Mark current phase as completed
            TurnPhase currentPhase = game.getTurnManager().getCurrentPhase();

            System.out.println("Moving from phase: " + currentPhase.getName());
            game.getTurnManager().setPhaseCompleted(true);

            // Explicitly execute the current phase before advancing
            game.getTurnManager().executeCurrentPhase();

            // If we're NOT already at END_TURN (which resets to PLANNING itself)
            if (currentPhase != TurnPhase.END_TURN) {
                game.getTurnManager().advancePhase();
            }

            // Update the UI
            updateDisplay();
        });

        controls.getChildren().addAll(turnLabel, phaseLabel, statusLabel, nextPhaseButton);

        return controls;
    }

    // Replace the simple showMessage method with a more flexible one:
    public void showMessage(String message, String type) {
        System.out.println(message);

        // Update status label
        statusLabel.setText(message);

        // Set style based on message type
        switch (type) {
            case "error":
                statusLabel.setTextFill(Color.RED);
                break;
            case "success":
                statusLabel.setTextFill(Color.GREEN);
                break;
            case "info":
            default:
                statusLabel.setTextFill(Color.BLUE);
                break;
        }

        // For important messages, show an alert
        if ("error".equals(type)) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        }
    }

    // Update the updateDisplay method to account for buildings:
    public void updateDisplay() {
        // Update turn and phase labels
        turnLabel.setText("Turn: " + game.getCurrentTurn());
        phaseLabel.setText("Phase: " + game.getTurnManager().getCurrentPhase().getName());

        // Update resource panel with production from buildings
        Map<ResourceType, Integer> buildingProduction =
                game.getBuildingManager().calculateTotalProduction();

        resourcePanel.update(
                game.getResourceManager().getAllResources(),
                buildingProduction
        );

        // Refresh map display
        mapView.renderGrid();
    }

    private void handleTileHover(TileEvent event) {
        Tile tile = event.getTile();
        // Show quick info, maybe a tooltip
        // This is a placeholder for now
    }

    public void showDialog(String title, String content) {
        // We'll implement dialog display later
        System.out.println("DIALOG: " + title + " - " + content);
    }
}