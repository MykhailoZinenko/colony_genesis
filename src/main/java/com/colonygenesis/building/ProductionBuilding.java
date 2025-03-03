package com.colonygenesis.building;

import com.colonygenesis.resource.ResourceType;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class ProductionBuilding extends Building {
    private ResourceType primaryOutput;
    private int baseOutput;
    private Map<String, Float> productionModifiers;

    public ProductionBuilding(String name, String description, int constructionTime,
                              ResourceType primaryOutput, int baseOutput) {
        super(name, description, BuildingType.PRODUCTION, constructionTime);
        this.primaryOutput = primaryOutput;
        this.baseOutput = baseOutput;
        this.productionModifiers = new HashMap<>();
    }

    @Override
    public Map<ResourceType, Integer> getProduction() {
        Map<ResourceType, Integer> production = new EnumMap<>(ResourceType.class);

        if (isActive()) {
            // Calculate output with modifiers
            float totalModifier = 1.0f;
            for (float modifier : productionModifiers.values()) {
                totalModifier *= modifier;
            }

            System.out.println(location.getResourceDeposit().getResourceType());

            // Apply terrain modifier from location
            if (location != null) {
                totalModifier *= location.getTerrainType().getResourceModifier(primaryOutput);
            }

            // Apply resource deposit modifier
            if (location != null && location.hasResourceDeposit() &&
                    location.getResourceDeposit().getResourceType() == primaryOutput) {
                totalModifier *= location.getResourceDeposit().getYield();
            }

            int finalOutput = Math.round(baseOutput * totalModifier);

            System.out.println("FINAL OUTPUT: " + finalOutput + " / " + totalModifier);

            production.put(primaryOutput, finalOutput);
        }

        return production;
    }

    @Override
    public double getProductionModifier(ResourceType type) {
        if (type == primaryOutput && isActive()) {
            return 1.5; // Boost production of other buildings for the same resource
        }
        return 1.0;
    }

    public void applyModifier(String source, float factor) {
        productionModifiers.put(source, factor);
    }

    public void removeModifier(String source) {
        productionModifiers.remove(source);
    }

    public float getEfficiency() {
        float efficiency = 1.0f;
        for (float modifier : productionModifiers.values()) {
            efficiency *= modifier;
        }
        return efficiency;
    }

    public ResourceType getPrimaryOutput() {
        return primaryOutput;
    }

    public int getBaseOutput() {
        return baseOutput;
    }
}