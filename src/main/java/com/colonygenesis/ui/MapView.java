package com.colonygenesis.ui;

import com.colonygenesis.building.Building;
import com.colonygenesis.map.HexGrid;
import com.colonygenesis.map.Tile;
import com.colonygenesis.map.TerrainType;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.transform.Affine;
import javafx.scene.shape.Polygon;

import java.util.HashMap;
import java.util.Map;

public class MapView extends Pane {
    private HexGrid grid;
    private double hexSize = 30.0; // Size of a hexagon (distance from center to vertex)
    private double offsetX = 50.0; // Horizontal offset
    private double offsetY = 50.0; // Vertical offset

    private Map<Tile, Polygon> hexagonMap = new HashMap<>();

    public MapView(HexGrid grid) {
        this.grid = grid;
        this.setMinSize(800, 600);
        this.setPrefSize(1000, 800);

        renderGrid();
    }

    public void renderGrid() {
        getChildren().clear(); // Clear existing hexagons
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

    // Add this method to the MapView class:

    private void renderBuilding(Tile tile, Polygon hexagon) {
        if (tile.hasBuilding() && tile.getBuilding().isCompleted()) {
            // Get the center of the hexagon
            double centerX = hexagon.getBoundsInParent().getCenterX();
            double centerY = hexagon.getBoundsInParent().getCenterY();

            // Create a building representation
            javafx.scene.shape.Rectangle building = new javafx.scene.shape.Rectangle(
                    centerX - hexSize/3, centerY - hexSize/3,
                    hexSize*2/3, hexSize*2/3
            );

            // Color based on building type
            Building b = tile.getBuilding();
            switch (b.getType()) {
                case PRODUCTION:
                    building.setFill(Color.BROWN);
                    break;
                case HABITATION:
                    building.setFill(Color.PALETURQUOISE);
                    break;
                case RESEARCH:
                    building.setFill(Color.MEDIUMPURPLE);
                    break;
                case STORAGE:
                    building.setFill(Color.GOLD);
                    break;
                case INFRASTRUCTURE:
                    building.setFill(Color.LIGHTGRAY);
                    break;
                case DEFENSE:
                    building.setFill(Color.DARKRED);
                    break;
                case SPECIAL:
                    building.setFill(Color.MAGENTA);
                    break;
            }

            building.setStroke(Color.BLACK);
            building.setStrokeWidth(1.0);

            getChildren().add(building);

        } else if (tile.hasBuilding() && !tile.getBuilding().isCompleted()) {
            // Show under construction indicator
            double centerX = hexagon.getBoundsInParent().getCenterX();
            double centerY = hexagon.getBoundsInParent().getCenterY();

            javafx.scene.shape.Rectangle construction = new javafx.scene.shape.Rectangle(
                    centerX - hexSize/3, centerY - hexSize/3,
                    hexSize*2/3, hexSize*2/3
            );

            construction.setFill(Color.LIGHTGRAY);
            construction.setStroke(Color.BLACK);
            construction.setStrokeWidth(1.0);
            construction.getStrokeDashArray().addAll(5.0, 5.0); // Dashed line

            getChildren().add(construction);
        }
    }

// Update the renderTile method to call renderBuilding:

    private void renderTile(Tile tile) {
        // Only render revealed tiles, or all tiles during development
        if (!tile.isRevealed() && !isDebugMode()) {
            return;
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

            javafx.scene.shape.Circle resourceMarker = new javafx.scene.shape.Circle(
                    centerX, centerY, hexSize/5
            );

            resourceMarker.setFill(tile.getResourceDeposit().getResourceType().getColor());
            resourceMarker.setStroke(Color.BLACK);
            resourceMarker.setStrokeWidth(1.0);

            getChildren().add(resourceMarker);
        }

        // If there's a building, render it
        if (tile.hasBuilding()) {
            renderBuilding(tile, hexagon);
        }
    }

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

    private void setupHexagonEvents(Polygon hexagon, Tile tile) {
        // Mouse hover effect
        hexagon.setOnMouseEntered(e -> {
            hexagon.setStrokeWidth(2.0);
            hexagon.setStroke(Color.WHITE);

            // Display tile info
            fireEvent(new TileEvent(TileEvent.TILE_HOVER, tile));
        });

        hexagon.setOnMouseExited(e -> {
            hexagon.setStrokeWidth(1.0);
            hexagon.setStroke(Color.BLACK);
        });

        // Click handling
        hexagon.setOnMouseClicked(e -> {
            // Select this tile
            fireEvent(new TileEvent(TileEvent.TILE_SELECTED, tile));
        });
    }

    // Update the view when tile changes (e.g., building placed, terrain changed)
    public void updateTile(Tile tile) {
        Polygon hexagon = hexagonMap.get(tile);
        if (hexagon != null) {
            // Update visual properties based on tile state
            TerrainType terrain = tile.getTerrainType();
            hexagon.setFill(terrain.getDisplayColor());

            // For now, this is simple, but we'll add more visual indicators later
        }
    }

    // For development, show all tiles regardless of revealed status
    private boolean isDebugMode() {
        return true; // During development
    }

    // Methods to adjust the view
    public void zoomIn() {
        hexSize *= 1.1;
        renderGrid();
    }

    public void zoomOut() {
        hexSize *= 0.9;
        renderGrid();
    }

    public void pan(double deltaX, double deltaY) {
        offsetX += deltaX;
        offsetY += deltaY;
        renderGrid();
    }
}