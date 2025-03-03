package com.colonygenesis.ui;

import com.colonygenesis.building.Building;
import com.colonygenesis.event.EventBus;
import com.colonygenesis.event.EventListener;
import com.colonygenesis.event.GameEvent;
import com.colonygenesis.event.events.BuildingEvent;
import com.colonygenesis.event.events.TileEvent;
import com.colonygenesis.map.ResourceDeposit;
import com.colonygenesis.map.Tile;
import com.colonygenesis.resource.ResourceType;
import com.colonygenesis.util.LoggerUtils;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.kordamp.bootstrapfx.scene.layout.Panel;

import java.util.Map;
import java.util.logging.Logger;

public class InfoPanel extends Panel implements EventListener {
    private static final Logger LOGGER = LoggerUtils.getLogger(InfoPanel.class);

    private final Label titleLabel;
    private final Label terrainLabel;
    private final Label resourcesLabel;
    private final Label buildingLabel;
    private final Label effectsLabel;
    private Tile currentTile;
    private final EventBus eventBus;

    public InfoPanel() {
        this.eventBus = EventBus.getInstance();

        // Register for events
        eventBus.register(this,
                GameEvent.EventType.TILE_UPDATED,
                GameEvent.EventType.BUILDING_PLACED,
                GameEvent.EventType.BUILDING_COMPLETED,
                GameEvent.EventType.BUILDING_ACTIVATED,
                GameEvent.EventType.BUILDING_DEACTIVATED,
                GameEvent.EventType.BUILDING_REMOVED
        );

        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        titleLabel = new Label("Select a tile");
        titleLabel.getStyleClass().add("heading");
        terrainLabel = new Label("Terrain: None");
        resourcesLabel = new Label("Resources: None");
        buildingLabel = new Label("Building: None");
        effectsLabel = new Label("Effects: None");

        content.getChildren().addAll(
                titleLabel, terrainLabel, resourcesLabel, buildingLabel, effectsLabel
        );

        setBody(content);
        getStyleClass().add("panel-info");

        LOGGER.info("InfoPanel initialized");
    }

    public void update(Tile tile) {
        if (tile == null) {
            clearInfo();
            return;
        }

        currentTile = tile;
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
            updateBuildingInfo(tile.getBuilding());
        } else {
            buildingLabel.setText("Building: None");
            effectsLabel.setText("Effects: None");
        }

        LOGGER.fine("Updated info for tile " + tile);
    }

    private void updateBuildingInfo(Building building) {
        if (building == null) {
            buildingLabel.setText("Building: None");
            effectsLabel.setText("Effects: None");
            return;
        }

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
    }

    private void clearInfo() {
        currentTile = null;
        titleLabel.setText("Select a tile");
        terrainLabel.setText("Terrain: None");
        resourcesLabel.setText("Resources: None");
        buildingLabel.setText("Building: None");
        effectsLabel.setText("Effects: None");
    }

    @Override
    public void onEvent(GameEvent event) {
        // Skip if we don't have a selected tile
        if (currentTile == null) return;

        Platform.runLater(() -> {
            switch (event.getType()) {
                case TILE_UPDATED:
                    handleTileUpdate((TileEvent) event);
                    break;

                case BUILDING_PLACED:
                case BUILDING_COMPLETED:
                case BUILDING_ACTIVATED:
                case BUILDING_DEACTIVATED:
                    handleBuildingEvent((BuildingEvent) event);
                    break;

                case BUILDING_REMOVED:
                    handleBuildingRemoved((BuildingEvent) event);
                    break;
            }
        });
    }

    private void handleTileUpdate(TileEvent event) {
        Tile tile = event.getTile();
        // Update if this is our current tile
        if (currentTile != null &&
                tile.getX() == currentTile.getX() &&
                tile.getY() == currentTile.getY()) {
            update(tile);
        }
    }

    private void handleBuildingEvent(BuildingEvent event) {
        Tile tile = event.getTile();
        // Update if this is our current tile
        if (currentTile != null &&
                tile != null &&
                tile.getX() == currentTile.getX() &&
                tile.getY() == currentTile.getY()) {
            update(tile);
        }
    }

    private void handleBuildingRemoved(BuildingEvent event) {
        Tile tile = event.getTile();
        // Update if this is our current tile
        if (currentTile != null &&
                tile != null &&
                tile.getX() == currentTile.getX() &&
                tile.getY() == currentTile.getY()) {
            // Special handling: the building is already removed from the tile,
            // so just update with the current tile
            update(currentTile);
        }
    }

    @Override
    public boolean isInterestedIn(GameEvent.EventType eventType) {
        switch (eventType) {
            case TILE_UPDATED:
            case BUILDING_PLACED:
            case BUILDING_COMPLETED:
            case BUILDING_ACTIVATED:
            case BUILDING_DEACTIVATED:
            case BUILDING_REMOVED:
                return true;
            default:
                return false;
        }
    }
}