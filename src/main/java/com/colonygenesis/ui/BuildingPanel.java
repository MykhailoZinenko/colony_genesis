package com.colonygenesis.ui;

import com.colonygenesis.building.Building;
import com.colonygenesis.building.BuildingFactory;
import com.colonygenesis.building.BuildingType;
import com.colonygenesis.core.Game;
import com.colonygenesis.resource.ResourceType;

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

public class BuildingPanel extends Panel {
    private Game game;
    private VBox buildingsContainer;
    private List<Building> availableBuildings;
    private Building selectedBuilding;
    private Consumer<Building> onBuildingSelected;

    public BuildingPanel(Game game) {
        this.game = game;
        this.availableBuildings = new ArrayList<>();

        //setTitle("Construction");

        buildingsContainer = new VBox(10);
        buildingsContainer.setPadding(new Insets(10));

        ScrollPane scrollPane = new ScrollPane(buildingsContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(400);

        setBody(scrollPane);
        getStyleClass().add("panel-success");

        // Initialize with available buildings
        initializeBuildings();
    }

    private void initializeBuildings() {
        // Add production buildings
        availableBuildings.add(BuildingFactory.createFarm());
        availableBuildings.add(BuildingFactory.createMine());
        availableBuildings.add(BuildingFactory.createSolarPanel());
        availableBuildings.add(BuildingFactory.createWaterExtractor());

        // Add habitation buildings
        availableBuildings.add(BuildingFactory.createHabitationDome());
        availableBuildings.add(BuildingFactory.createLuxuryApartments());

        // Update the UI
        updateBuildingsList();
    }

    private void updateBuildingsList() {
        buildingsContainer.getChildren().clear();

        for (Building building : availableBuildings) {
            // Create a panel for each building
            VBox buildingItem = createBuildingItem(building);
            buildingsContainer.getChildren().add(buildingItem);
        }
    }

    private VBox createBuildingItem(Building building) {
        VBox item = new VBox(5);
        item.setPadding(new Insets(5));
        item.setStyle("-fx-border-color: #ddd; -fx-border-radius: 5;");

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
            costBox.getChildren().add(costLabel);
        }

        // Construction time
        Label timeLabel = new Label("Construction Time: " + building.getConstructionTime() + " turns");

        // Select button
        Button selectButton = new Button("Select");
        selectButton.getStyleClass().addAll("btn", "btn-sm", "btn-primary");
        selectButton.setOnAction(e -> {
            selectedBuilding = building;
            if (onBuildingSelected != null) {
                onBuildingSelected.accept(building);
            }
        });

        // Add to item
        item.getChildren().addAll(nameLabel, descLabel, new Label("Cost:"), costBox, timeLabel, selectButton);

        return item;
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
}