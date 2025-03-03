package com.colonygenesis.ui;

import com.colonygenesis.building.Building;
import com.colonygenesis.event.EventBus;
import com.colonygenesis.event.EventListener;
import com.colonygenesis.event.GameEvent;
import com.colonygenesis.event.events.BuildingEvent;
import com.colonygenesis.event.events.TileEvent;
import com.colonygenesis.map.HexGrid;
import com.colonygenesis.map.Tile;
import com.colonygenesis.map.TerrainType;
import com.colonygenesis.util.LoggerUtils;

import javafx.application.Platform;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class MapView extends Pane implements EventListener {
    private static final Logger LOGGER = LoggerUtils.getLogger(MapView.class);

    private final HexGrid grid;
    private double hexSize = 30.0;
    private double offsetX = 50.0;
    private double offsetY = 50.0;

    private final Map<Tile, Polygon> hexagonMap = new HashMap<>();
    private final EventBus eventBus;

    public MapView(HexGrid grid) {
        this.grid = grid;
        this.eventBus = EventBus.getInstance();

        // Register for tile and building events
        eventBus.register(this,
                GameEvent.EventType.TILE_UPDATED,
                GameEvent.EventType.BUILDING_PLACED,
                GameEvent.EventType.BUILDING_COMPLETED,
                GameEvent.EventType.BUILDING_ACTIVATED,
                GameEvent.EventType.BUILDING_DEACTIVATED,
                GameEvent.EventType.BUILDING_REMOVED
        );

        this.setMinSize(800, 600);
        this.setPrefSize(1000, 800);

        renderGrid();

        LOGGER.info("MapView initialized with grid " + grid.getWidth() + "x" + grid.getHeight());
    }

    public void renderGrid() {
        getChildren().clear();
        hexagonMap.clear();

        for (int x = 0; x < grid.getWidth(); x++) {
            for (int y = 0; y < grid.getHeight(); y++) {
                Tile tile = grid.getTileAt(x, y);
                if (tile != null) {
                    renderTile(tile);
                }
            }
        }
    }

    public void renderTile(Tile tile) {
        if (tile == null) return;

        // Only render revealed tiles, or all tiles during development
        if (!tile.isRevealed() && !isDebugMode()) {
            return;
        }

        // Remove any existing rendering of this tile
        Polygon existingHexagon = hexagonMap.get(tile);
        if (existingHexagon != null) {
            getChildren().remove(existingHexagon);

            // Also remove any building or resource indicators on this tile
            getChildren().removeIf(node -> {
                if (node.getUserData() instanceof String) {
                    String userData = (String) node.getUserData();
                    return userData.startsWith("tile_" + tile.getX() + "_" + tile.getY());
                }
                return false;
            });
        }

        // Create a hexagon for the tile
        Polygon hexagon = createHexagon(tile.getX(), tile.getY());

        // Set the fill color based on terrain
        TerrainType terrain = tile.getTerrainType();
        hexagon.setFill(terrain.getDisplayColor());

        // Add stroke to see boundaries
        hexagon.setStroke(Color.BLACK);
        hexagon.setStrokeWidth(1.0);

        // Add event handling
        setupHexagonEvents(hexagon, tile);

        // Add to our pane and maintain reference
        getChildren().add(hexagon);
        hexagonMap.put(tile, hexagon);

        // If there's a resource deposit, render an indicator
        if (tile.hasResourceDeposit()) {
            // Render resource indicator
            double centerX = hexagon.getBoundsInParent().getCenterX();
            double centerY = hexagon.getBoundsInParent().getCenterY() + hexSize/2;

            Circle resourceMarker = new Circle(centerX, centerY, hexSize/5);
            resourceMarker.setFill(tile.getResourceDeposit().getResourceType().getColor());
            resourceMarker.setStroke(Color.BLACK);
            resourceMarker.setStrokeWidth(1.0);
            resourceMarker.setUserData("tile_" + tile.getX() + "_" + tile.getY() + "_resource");

            getChildren().add(resourceMarker);
        }

        // If there's a building, render it
        if (tile.hasBuilding()) {
            renderBuilding(tile, hexagon);
        }
    }

    private void renderBuilding(Tile tile, Polygon hexagon) {
        if (!tile.hasBuilding()) {
            return;
        }

        Building building = tile.getBuilding();
        double centerX = hexagon.getBoundsInParent().getCenterX();
        double centerY = hexagon.getBoundsInParent().getCenterY();

        if (building.isCompleted()) {
            // Create a building representation
            Rectangle buildingRect = new Rectangle(
                    centerX - hexSize/3, centerY - hexSize/3,
                    hexSize*2/3, hexSize*2/3
            );

            // Color based on building type
            switch (building.getType()) {
                case PRODUCTION:
                    buildingRect.setFill(Color.BROWN);
                    break;
                case HABITATION:
                    buildingRect.setFill(Color.PALETURQUOISE);
                    break;
                case RESEARCH:
                    buildingRect.setFill(Color.MEDIUMPURPLE);
                    break;
                case STORAGE:
                    buildingRect.setFill(Color.GOLD);
                    break;
                case INFRASTRUCTURE:
                    buildingRect.setFill(Color.LIGHTGRAY);
                    break;
                case DEFENSE:
                    buildingRect.setFill(Color.DARKRED);
                    break;
                case SPECIAL:
                    buildingRect.setFill(Color.MAGENTA);
                    break;
            }

            // Add a slight darkening effect if inactive
            if (!building.isActive()) {
                buildingRect.setOpacity(0.5);
            }

            buildingRect.setStroke(Color.BLACK);
            buildingRect.setStrokeWidth(1.0);
            buildingRect.setUserData("tile_" + tile.getX() + "_" + tile.getY() + "_building");

            getChildren().add(buildingRect);
        } else {
            // Show under construction indicator
            Rectangle construction = new Rectangle(
                    centerX - hexSize/3, centerY - hexSize/3,
                    hexSize*2/3, hexSize*2/3
            );

            construction.setFill(Color.LIGHTGRAY);
            construction.setStroke(Color.BLACK);
            construction.setStrokeWidth(1.0);
            construction.getStrokeDashArray().addAll(5.0, 5.0); // Dashed line
            construction.setUserData("tile_" + tile.getX() + "_" + tile.getY() + "_construction");

            getChildren().add(construction);
        }
    }

    /**
     * Creates a hexagon polygon for a tile at the specified grid coordinates.
     */
    private Polygon createHexagon(int gridX, int gridY) {
        // Convert axial coordinates to pixel coordinates
        double height = hexSize * 2;
        double width = Math.sqrt(3) * hexSize;

        // Axial to pixel conversion for hexagonal grid
        double pixelX = offsetX + gridX * width * 0.75;
        double pixelY = offsetY + gridY * height * 0.866;

        // Offset every other column
        if (gridX % 2 != 0) {
            pixelY += height * 0.433;
        }

        // Create the hexagon points
        Polygon hexagon = new Polygon();
        for (int i = 0; i < 6; i++) {
            double angle = 2 * Math.PI / 6 * i;
            double x = pixelX + hexSize * Math.cos(angle);
            double y = pixelY + hexSize * Math.sin(angle);
            hexagon.getPoints().addAll(x, y);
        }

        return hexagon;
    }

    /**
     * Sets up event handling for a hexagon representing a tile.
     */
    private void setupHexagonEvents(Polygon hexagon, Tile tile) {
        // Mouse hover effect
        hexagon.setOnMouseEntered(e -> {
            hexagon.setStrokeWidth(2.0);
            hexagon.setStroke(Color.WHITE);

            // Display tile info
            fireEvent(new com.colonygenesis.ui.TileEvent(com.colonygenesis.ui.TileEvent.TILE_HOVER, tile));
        });

        hexagon.setOnMouseExited(e -> {
            hexagon.setStrokeWidth(1.0);
            hexagon.setStroke(Color.BLACK);
        });

        // Click handling
        hexagon.setOnMouseClicked(e -> {
            // Left-click selects the tile
            if (e.getButton() == MouseButton.PRIMARY) {
                fireEvent(new com.colonygenesis.ui.TileEvent(com.colonygenesis.ui.TileEvent.TILE_SELECTED, tile));
            }
            // Right-click shows context menu for buildings
            else if (e.getButton() == MouseButton.SECONDARY && tile.hasBuilding()) {
                showBuildingContextMenu(tile.getBuilding(), hexagon, e.getScreenX(), e.getScreenY());
            }
        });
    }

    private void showBuildingContextMenu(Building building, Polygon hexagon, double x, double y) {
        ContextMenu contextMenu = new ContextMenu();

        // Toggle active state
        MenuItem toggleItem = new MenuItem(building.isActive() ? "Deactivate" : "Activate");
        toggleItem.setOnAction(e -> {
            fireEvent(new BuildingActionEvent(BuildingActionEvent.TOGGLE_ACTIVE, building));
        });

        // Demolish building
        MenuItem demolishItem = new MenuItem("Demolish");
        demolishItem.setOnAction(e -> {
            fireEvent(new BuildingActionEvent(BuildingActionEvent.DEMOLISH, building));
        });

        // Add all items to menu
        contextMenu.getItems().addAll(toggleItem, demolishItem);

        // Show the context menu
        contextMenu.show(hexagon, x, y);
    }

    @Override
    public void onEvent(GameEvent event) {
        Platform.runLater(() -> {
            switch (event.getType()) {
                case TILE_UPDATED:
                    TileEvent tileEvent = (TileEvent) event;
                    renderTile(tileEvent.getTile());
                    break;

                case BUILDING_PLACED:
                case BUILDING_COMPLETED:
                case BUILDING_ACTIVATED:
                case BUILDING_DEACTIVATED:
                    BuildingEvent buildingEvent = (BuildingEvent) event;
                    if (buildingEvent.getTile() != null) {
                        renderTile(buildingEvent.getTile());
                    }
                    break;

                case BUILDING_REMOVED:
                    // Special handling for building removal
                    BuildingEvent removedEvent = (BuildingEvent) event;
                    if (removedEvent.getTile() != null) {
                        // Force a clean rendering of the tile without the building
                        renderTile(removedEvent.getTile());
                    }
                    break;
            }
        });
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

    // For development, show all tiles regardless of revealed status
    private boolean isDebugMode() {
        return true; // During development
    }
}