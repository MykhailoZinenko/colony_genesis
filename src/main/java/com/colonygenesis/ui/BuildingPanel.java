package com.colonygenesis.ui;

import com.colonygenesis.building.Building;
import com.colonygenesis.building.BuildingFactory;
import com.colonygenesis.building.BuildingType;
import com.colonygenesis.core.Game;
import com.colonygenesis.event.EventBus;
import com.colonygenesis.event.EventListener;
import com.colonygenesis.event.GameEvent;
import com.colonygenesis.resource.ResourceType;
import com.colonygenesis.util.LoggerUtils;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import org.kordamp.bootstrapfx.scene.layout.Panel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class BuildingPanel extends Panel implements EventListener {
    private static final Logger LOGGER = LoggerUtils.getLogger(BuildingPanel.class);

    private final Game game;
    private final VBox buildingsContainer;
    private final EventBus eventBus;
    private Building selectedBuilding;
    private Consumer<Building> onBuildingSelected;

    public BuildingPanel(Game game) {
        this.game = game;
        this.eventBus = EventBus.getInstance();
        this.buildingsContainer = new VBox(10);

        // Register for events
        eventBus.register(this,
                GameEvent.EventType.RESOURCE_CHANGED,
                GameEvent.EventType.BUILDING_PLACED
        );

        initializePanel();

        LOGGER.info("BuildingPanel initialized");
    }

    private void initializePanel() {
        buildingsContainer.setPadding(new Insets(10));

        ScrollPane scrollPane = new ScrollPane(buildingsContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(400);

        setBody(scrollPane);
        getStyleClass().add("panel-success");

        // Initialize with available buildings
        updateBuildingsList();
    }

    private void updateBuildingsList() {
        buildingsContainer.getChildren().clear();

        // Add production buildings
        createBuildingItem(BuildingFactory.createFarm());
        createBuildingItem(BuildingFactory.createMine());
        createBuildingItem(BuildingFactory.createSolarPanel());
        createBuildingItem(BuildingFactory.createWaterExtractor());

        // Add habitation buildings
        createBuildingItem(BuildingFactory.createHabitationDome());
        createBuildingItem(BuildingFactory.createLuxuryApartments());
    }

    private void createBuildingItem(Building building) {
        // Create a panel for each building
        VBox buildingItem = new VBox(5);
        buildingItem.setPadding(new Insets(5));
        buildingItem.setStyle("-fx-border-color: #ddd; -fx-border-radius: 5;");

        // Building name and description
        Label nameLabel = new Label(building.getName());
        nameLabel.getStyleClass().add("b");

        Label descLabel = new Label(building.getDescription());
        descLabel.setWrapText(true);

        // Resource costs
        VBox costBox = new VBox(2);
        Map<ResourceType, Integer> costs = building.getConstructionCost();

        for (Map.Entry<ResourceType, Integer> entry : costs.entrySet()) {
            Label costLabel = new Label(entry.getKey().getName() + ": " + entry.getValue());

            // Check if we can afford it
            boolean canAfford = game.getResourceManager().getResource(entry.getKey()) >= entry.getValue();
            if (!canAfford) {
                costLabel.setStyle("-fx-text-fill: red;");
            }

            costBox.getChildren().add(costLabel);
        }

        // Construction time
        Label timeLabel = new Label("Construction Time: " + building.getConstructionTime() + " turns");

        // Select button
        Button selectButton = new Button("Select");
        selectButton.getStyleClass().addAll("btn", "btn-sm", "btn-primary");

        // Check if we can afford all costs
        boolean canAffordAll = true;
        for (Map.Entry<ResourceType, Integer> entry : costs.entrySet()) {
            if (game.getResourceManager().getResource(entry.getKey()) < entry.getValue()) {
                canAffordAll = false;
                break;
            }
        }

        selectButton.setDisable(!canAffordAll);

        selectButton.setOnAction(e -> {
            // Create a new instance of the building type
            Building newBuilding = null;
            String name = building.getName();

            // Create a new instance using the factory
            if (name.equals("Farm")) {
                newBuilding = BuildingFactory.createFarm();
            } else if (name.equals("Mine")) {
                newBuilding = BuildingFactory.createMine();
            } else if (name.equals("Solar Panel")) {
                newBuilding = BuildingFactory.createSolarPanel();
            } else if (name.equals("Water Extractor")) {
                newBuilding = BuildingFactory.createWaterExtractor();
            } else if (name.equals("Habitation Dome")) {
                newBuilding = BuildingFactory.createHabitationDome();
            } else if (name.equals("Luxury Apartments")) {
                newBuilding = BuildingFactory.createLuxuryApartments();
            }

            if (newBuilding != null) {
                selectedBuilding = newBuilding;
                LOGGER.info("Selected building: " + selectedBuilding.getName());

                if (onBuildingSelected != null) {
                    onBuildingSelected.accept(selectedBuilding);
                }
            }
        });

        // Add to item
        buildingItem.getChildren().addAll(nameLabel, descLabel, new Label("Cost:"), costBox, timeLabel, selectButton);
        buildingsContainer.getChildren().add(buildingItem);
    }

    public void setOnBuildingSelected(Consumer<Building> handler) {
        this.onBuildingSelected = handler;
    }

    public Building getSelectedBuilding() {
        return selectedBuilding;
    }

    public void clearSelection() {
        selectedBuilding = null;
    }

    @Override
    public void onEvent(GameEvent event) {
        if (event.getType() == GameEvent.EventType.RESOURCE_CHANGED ||
                event.getType() == GameEvent.EventType.BUILDING_PLACED) {
            // Update building list when resources or buildings change
            Platform.runLater(this::updateBuildingsList);
        }
    }

    @Override
    public boolean isInterestedIn(GameEvent.EventType eventType) {
        return eventType == GameEvent.EventType.RESOURCE_CHANGED ||
                eventType == GameEvent.EventType.BUILDING_PLACED;
    }
}