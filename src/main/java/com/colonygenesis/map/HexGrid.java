package com.colonygenesis.map;

import java.util.ArrayList;
import java.util.List;

public class HexGrid {
    private final int width;
    private final int height;
    private final Tile[][] tiles;
    private final boolean[][] revealed;

    public HexGrid(int width, int height) {
        this.width = width;
        this.height = height;
        this.tiles = new Tile[width][height];
        this.revealed = new boolean[width][height];
    }

    public void generateGrid() {
        // Create empty tiles with default terrain
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                // For now, just use PLAINS for all tiles
                // We'll implement proper generation later
                tiles[x][y] = new Tile(x, y, TerrainType.PLAINS);
            }
        }
    }

    public boolean isValidCoordinate(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }

    public Tile getTileAt(int x, int y) {
        if (isValidCoordinate(x, y)) {
            return tiles[x][y];
        }
        return null;
    }

    /**
     * Gets all neighboring tiles around the given coordinates.
     * Uses axial coordinates for hexagonal grid.
     */
    public List<Tile> getNeighbors(int x, int y) {
        List<Tile> neighbors = new ArrayList<>();

        // In a hex grid using axial coordinates, these are the 6 neighbors
        int[][] directions = {
                {1, 0}, {1, -1}, {0, -1},
                {-1, 0}, {-1, 1}, {0, 1}
        };

        for (int[] dir : directions) {
            int nx = x + dir[0];
            int ny = y + dir[1];

            if (isValidCoordinate(nx, ny)) {
                neighbors.add(tiles[nx][ny]);
            }
        }

        return neighbors;
    }

    public List<Tile> getNeighbors(Tile tile) {
        return getNeighbors(tile.getX(), tile.getY());
    }

    public void revealTile(int x, int y) {
        if (isValidCoordinate(x, y)) {
            tiles[x][y].reveal();
            revealed[x][y] = true;
        }
    }

    public void revealArea(int centerX, int centerY, int radius) {
        // Reveal tiles in a circular area
        for (int x = centerX - radius; x <= centerX + radius; x++) {
            for (int y = centerY - radius; y <= centerY + radius; y++) {
                // Calculate hex distance (different from Euclidean)
                if (calculateHexDistance(centerX, centerY, x, y) <= radius) {
                    if (isValidCoordinate(x, y)) {
                        revealTile(x, y);
                    }
                }
            }
        }
    }

    // Helper method to calculate distance between hexes in axial coordinates
    private int calculateHexDistance(int x1, int y1, int x2, int y2) {
        return (Math.abs(x1 - x2) + Math.abs(y1 - y2) + Math.abs(x1 + y1 - x2 - y2)) / 2;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}