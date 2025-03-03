package com.colonygenesis.building;

import com.colonygenesis.resource.ResourceType;

import java.util.EnumMap;
import java.util.Map;

public class BuildingFactory {

    // Production buildings
    public static Building createFarm() {
        ProductionBuilding farm = new ProductionBuilding(
                "Farm", "Produces food for your colony", 2, ResourceType.FOOD, 10
        );

        // Set construction costs
        Map<ResourceType, Integer> cost = new EnumMap<>(ResourceType.class);
        cost.put(ResourceType.MATERIALS, 100);
        cost.put(ResourceType.ENERGY, 50);

        for (Map.Entry<ResourceType, Integer> entry : cost.entrySet()) {
            farm.constructionCost.put(entry.getKey(), entry.getValue());
        }

        // Set maintenance costs
        farm.maintenanceCost.put(ResourceType.WATER, 2);
        farm.maintenanceCost.put(ResourceType.ENERGY, 1);

        return farm;
    }

    public static Building createMine() {
        ProductionBuilding mine = new ProductionBuilding(
                "Mine", "Extracts materials from the ground", 3, ResourceType.MATERIALS, 8
        );

        // Set construction costs
        Map<ResourceType, Integer> cost = new EnumMap<>(ResourceType.class);
        cost.put(ResourceType.MATERIALS, 150);
        cost.put(ResourceType.ENERGY, 100);

        for (Map.Entry<ResourceType, Integer> entry : cost.entrySet()) {
            mine.constructionCost.put(entry.getKey(), entry.getValue());
        }

        // Set maintenance costs
        mine.maintenanceCost.put(ResourceType.ENERGY, 3);

        return mine;
    }

    public static Building createSolarPanel() {
        ProductionBuilding solarPanel = new ProductionBuilding(
                "Solar Panel", "Generates energy from sunlight", 2, ResourceType.ENERGY, 15
        );

        // Set construction costs
        Map<ResourceType, Integer> cost = new EnumMap<>(ResourceType.class);
        cost.put(ResourceType.MATERIALS, 120);

        for (Map.Entry<ResourceType, Integer> entry : cost.entrySet()) {
            solarPanel.constructionCost.put(entry.getKey(), entry.getValue());
        }

        // Minimal maintenance
        solarPanel.maintenanceCost.put(ResourceType.MATERIALS, 1);

        return solarPanel;
    }

    public static Building createWaterExtractor() {
        ProductionBuilding waterExtractor = new ProductionBuilding(
                "Water Extractor", "Extracts and purifies water", 2, ResourceType.WATER, 12
        );

        // Set construction costs
        Map<ResourceType, Integer> cost = new EnumMap<>(ResourceType.class);
        cost.put(ResourceType.MATERIALS, 130);
        cost.put(ResourceType.ENERGY, 80);

        for (Map.Entry<ResourceType, Integer> entry : cost.entrySet()) {
            waterExtractor.constructionCost.put(entry.getKey(), entry.getValue());
        }

        // Set maintenance costs
        waterExtractor.maintenanceCost.put(ResourceType.ENERGY, 2);

        return waterExtractor;
    }

    // Habitation buildings
    public static Building createHabitationDome() {
        HabitationBuilding dome = new HabitationBuilding(
                "Habitation Dome", "Basic housing for colonists", 4, 10, 0.5f
        );

        // Set construction costs
        Map<ResourceType, Integer> cost = new EnumMap<>(ResourceType.class);
        cost.put(ResourceType.MATERIALS, 200);
        cost.put(ResourceType.ENERGY, 100);

        for (Map.Entry<ResourceType, Integer> entry : cost.entrySet()) {
            dome.constructionCost.put(entry.getKey(), entry.getValue());
        }

        return dome;
    }

    public static Building createLuxuryApartments() {
        HabitationBuilding apartments = new HabitationBuilding(
                "Luxury Apartments", "High-quality housing", 6, 8, 0.9f
        );

        // Set construction costs
        Map<ResourceType, Integer> cost = new EnumMap<>(ResourceType.class);
        cost.put(ResourceType.MATERIALS, 350);
        cost.put(ResourceType.ENERGY, 150);
        cost.put(ResourceType.RARE_MINERALS, 20);

        for (Map.Entry<ResourceType, Integer> entry : cost.entrySet()) {
            apartments.constructionCost.put(entry.getKey(), entry.getValue());
        }

        // Higher maintenance for luxury
        apartments.maintenanceCost.put(ResourceType.ENERGY, 4);
        apartments.maintenanceCost.put(ResourceType.WATER, 2);

        return apartments;
    }
}