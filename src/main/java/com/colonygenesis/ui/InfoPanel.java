package com.colonygenesis.ui;

import com.colonygenesis.building.Building;
import com.colonygenesis.map.ResourceDeposit;
import com.colonygenesis.map.Tile;
import com.colonygenesis.resource.ResourceType;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.kordamp.bootstrapfx.scene.layout.Panel;

import java.util.Map;

public class InfoPanel extends Panel {
    private Label titleLabel;
    private Label terrainLabel;
    private Label resourcesLabel;
    private Label buildingLabel;
    private Label effectsLabel;

    public InfoPanel() {
        //setTitle("Tile Information");

        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        titleLabel = new Label("Select a tile");
        terrainLabel = new Label("Terrain: None");
        resourcesLabel = new Label("Resources: None");
        buildingLabel = new Label("Building: None");
        effectsLabel = new Label("Effects: None");

        content.getChildren().addAll(
                titleLabel, terrainLabel, resourcesLabel, buildingLabel, effectsLabel
        );

        setBody(content);
        getStyleClass().add("panel-info");
    }

    // Update the update method in InfoPanel:

    public void update(Tile tile) {
        if (tile == null) {
            titleLabel.setText("Select a tile");
            terrainLabel.setText("Terrain: None");
            resourcesLabel.setText("Resources: None");
            buildingLabel.setText("Building: None");
            effectsLabel.setText("Effects: None");
            return;
        }

        titleLabel.setText("Tile [" + tile.getX() + ", " + tile.getY() + "]");
        terrainLabel.setText("Terrain: " + tile.getTerrainType().getName());

        if (tile.hasResourceDeposit()) {
            ResourceDeposit deposit = tile.getResourceDeposit();
            resourcesLabel.setText("Resources: " + deposit.getName() + " (+" + deposit.getYield() + " " +
                    deposit.getResourceType().getName() + ")");
        } else {
            resourcesLabel.setText("Resources: None");
        }

        if (tile.hasBuilding()) {
            Building building = tile.getBuilding();
            StringBuilder buildingInfo = new StringBuilder("Building: " + building.getName());

            if (!building.isCompleted()) {
                buildingInfo.append(" (Under construction: ")
                        .append(building.getRemainingConstructionTime())
                        .append(" turns left)");
            } else if (!building.isActive()) {
                buildingInfo.append(" (Inactive)");
            } else {
                buildingInfo.append(" (Active)");
            }

            buildingLabel.setText(buildingInfo.toString());

            // Add production/consumption info
            if (building.isCompleted()) {
                StringBuilder productionInfo = new StringBuilder("Production: ");
                Map<ResourceType, Integer> production = building.getProduction();

                if (production.isEmpty()) {
                    productionInfo.append("None");
                } else {
                    boolean first = true;
                    for (Map.Entry<ResourceType, Integer> entry : production.entrySet()) {
                        if (!first) {
                            productionInfo.append(", ");
                        }
                        first = false;

                        ResourceType type = entry.getKey();
                        int amount = entry.getValue();

                        if (amount > 0) {
                            productionInfo.append("+").append(amount).append(" ").append(type.getName());
                        } else if (amount < 0) {
                            productionInfo.append(amount).append(" ").append(type.getName());
                        }
                    }
                }

                effectsLabel.setText(productionInfo.toString());
            } else {
                effectsLabel.setText("Effects: None (under construction)");
            }
        } else {
            buildingLabel.setText("Building: None");
            effectsLabel.setText("Effects: None");
        }
    }
}