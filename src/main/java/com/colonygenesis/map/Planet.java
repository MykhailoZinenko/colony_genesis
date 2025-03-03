package com.colonygenesis.map;

//import com.colonygenesis.environment.EnvironmentManager;
import com.colonygenesis.resource.ResourceType;

import java.util.*;

public class Planet {
    private String name;
    private PlanetType type;
    private HexGrid grid;
    //private EnvironmentManager environment;
    private List<ResourceDeposit> resources;
    private Random random;

    public Planet(String name, PlanetType type, int width, int height) {
        this.name = name;
        this.type = type;
        this.grid = new HexGrid(width, height);
        this.resources = new ArrayList<>();
        this.random = new Random();

        // Environment manager will be implemented later
        // this.environment = new EnvironmentManager(this);
    }

    public void generateTerrain() {
        // First, create the empty grid
        grid.generateGrid();

        // Apply terrain generation based on planet type
        // This is a simplified version; we'll improve it later
        for (int x = 0; x < grid.getWidth(); x++) {
            for (int y = 0; y < grid.getHeight(); y++) {
                TerrainType terrainType = generateTerrainAt(x, y);
                Tile tile = grid.getTileAt(x, y);
                tile.setTerrainType(terrainType);
            }
        }

        // Place resource deposits
        placeResourceDeposits();
    }

    private TerrainType generateTerrainAt(int x, int y) {
        // Simplified terrain generation based on noise
        // We'll use a simple random approach for now
        double value = random.nextDouble();

        // Adjust probabilities based on planet type
        switch (type) {
            case TEMPERATE:
                if (value < 0.5) return TerrainType.PLAINS;
                if (value < 0.7) return TerrainType.FOREST;
                if (value < 0.8) return TerrainType.MOUNTAINS;
                if (value < 0.95) return TerrainType.WATER;
                return TerrainType.TUNDRA;

            case DESERT:
                if (value < 0.7) return TerrainType.DESERT;
                if (value < 0.85) return TerrainType.PLAINS;
                if (value < 0.95) return TerrainType.MOUNTAINS;
                return TerrainType.WATER; // Rare oases

            case TUNDRA:
                if (value < 0.6) return TerrainType.TUNDRA;
                if (value < 0.8) return TerrainType.PLAINS;
                if (value < 0.9) return TerrainType.MOUNTAINS;
                return TerrainType.WATER; // Frozen lakes

            case VOLCANIC:
                if (value < 0.5) return TerrainType.MOUNTAINS;
                if (value < 0.8) return TerrainType.PLAINS;
                if (value < 0.9) return TerrainType.DESERT;
                return TerrainType.WATER; // Lava lakes (represented as water for now)

            case OCEANIC:
                if (value < 0.7) return TerrainType.WATER;
                if (value < 0.9) return TerrainType.PLAINS;
                return TerrainType.FOREST; // Islands

            default:
                return TerrainType.PLAINS;
        }
    }

    // In the Planet class, update the placeResourceDeposits method:

    private void placeResourceDeposits() {
        // Clear existing resources
        resources.clear();

        // Number of deposits to create
        int gridSize = grid.getWidth() * grid.getHeight();
        int numDeposits = gridSize / 25; // Roughly 4% of tiles have deposits

        // Define probabilities for each resource type based on planet type
        Map<ResourceType, Double> depositProbabilities = new EnumMap<>(ResourceType.class);

        // Default probabilities
        depositProbabilities.put(ResourceType.FOOD, 0.1);
        depositProbabilities.put(ResourceType.WATER, 0.1);
        depositProbabilities.put(ResourceType.MATERIALS, 0.3);
        depositProbabilities.put(ResourceType.ENERGY, 0.2);
        depositProbabilities.put(ResourceType.RARE_MINERALS, 0.2);
        depositProbabilities.put(ResourceType.ALIEN_COMPOUNDS, 0.1);

        // Adjust based on planet type
        switch (type) {
            case DESERT:
                depositProbabilities.put(ResourceType.WATER, 0.05);
                depositProbabilities.put(ResourceType.ENERGY, 0.4); // More energy (solar potential)
                depositProbabilities.put(ResourceType.ALIEN_COMPOUNDS, 0.2); // More alien artifacts
                break;
            case TUNDRA:
                depositProbabilities.put(ResourceType.WATER, 0.2); // More water (ice)
                depositProbabilities.put(ResourceType.RARE_MINERALS, 0.3);
                depositProbabilities.put(ResourceType.ENERGY, 0.1);
                break;
            case VOLCANIC:
                depositProbabilities.put(ResourceType.RARE_MINERALS, 0.4);
                depositProbabilities.put(ResourceType.ENERGY, 0.3); // Geothermal
                depositProbabilities.put(ResourceType.FOOD, 0.05);
                break;
            case OCEANIC:
                depositProbabilities.put(ResourceType.WATER, 0.4);
                depositProbabilities.put(ResourceType.FOOD, 0.3); // Seafood
                depositProbabilities.put(ResourceType.RARE_MINERALS, 0.1);
                break;
            // TEMPERATE has balanced distribution
        }

        // Place deposits
        for (int i = 0; i < numDeposits; i++) {
            // Random position
            int x = random.nextInt(grid.getWidth());
            int y = random.nextInt(grid.getHeight());
            Tile tile = grid.getTileAt(x, y);

            // Skip if already has a deposit
            if (tile.hasResourceDeposit()) {
                continue;
            }

            // Choose resource type based on probabilities
            ResourceType selectedType = null;
            double rand = random.nextDouble();
            double cumulativeProbability = 0.0;

            for (Map.Entry<ResourceType, Double> entry : depositProbabilities.entrySet()) {
                cumulativeProbability += entry.getValue();
                if (rand <= cumulativeProbability) {
                    selectedType = entry.getKey();
                    break;
                }
            }

            if (selectedType == null) {
                selectedType = ResourceType.MATERIALS; // Fallback
            }

            // Create deposit with varying yield
            double yield = 1.5 + random.nextDouble() * 2.5; // 1.5 to 4.0
            String name = getDepositName(selectedType);

            ResourceDeposit deposit = new ResourceDeposit(selectedType, yield, name);
            tile.setResourceDeposit(deposit);
            resources.add(deposit);

            System.out.println(deposit);
        }
    }

    private String getDepositName(ResourceType type) {
        // Generate a flavorful name for the deposit
        String[] prefixes = {"Rich", "Abundant", "Promising", "Massive", "Trace"};
        String prefix = prefixes[random.nextInt(prefixes.length)];

        switch (type) {
            case FOOD:
                return prefix + " Fertile Soil";
            case WATER:
                return prefix + " Spring";
            case MATERIALS:
                return prefix + " Mineral Vein";
            case ENERGY:
                return prefix + " Energy Source";
            case RARE_MINERALS:
                return prefix + " Crystal Formation";
            case ALIEN_COMPOUNDS:
                return prefix + " Alien Ruins";
            default:
                return prefix + " Resource Deposit";
        }
    }

    public Tile getTileAt(int x, int y) {
        return grid.getTileAt(x, y);
    }

    public HexGrid getGrid() {
        return grid;
    }

    public String getName() {
        return name;
    }

    public PlanetType getType() {
        return type;
    }

    public List<ResourceDeposit> getResourceDeposits() {
        return new ArrayList<>(resources); // Return defensive copy
    }

    public double calculateHabitability() {
        // Calculate overall planet habitability
        // This will consider terrain distribution, resources, etc.
        // Just a placeholder for now
        return 0.75;
    }
}