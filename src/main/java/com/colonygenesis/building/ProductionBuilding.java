package com.colonygenesis.building;

import com.colonygenesis.map.ResourceDeposit;
import com.colonygenesis.resource.ResourceType;
import com.colonygenesis.util.LoggerUtils;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

public class ProductionBuilding extends Building {
    private static final Logger LOGGER = LoggerUtils.getLogger(ProductionBuilding.class);

    private final ResourceType primaryOutput;
    private final int baseOutput;
    private final Map<String, Float> productionModifiers;

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

        if (!isActive()) {
            return production;  // Empty map if not active
        }

        // Calculate output with modifiers
        float totalModifier = 1.0f;

        // Apply production modifiers from effects, etc.
        for (float modifier : productionModifiers.values()) {
            totalModifier *= modifier;
        }

        // Apply terrain modifier from location (safely)
        if (location != null) {
            totalModifier *= location.getTerrainType().getResourceModifier(primaryOutput);

            // Apply resource deposit modifier (safely)
            if (location.hasResourceDeposit()) {
                ResourceDeposit deposit = location.getResourceDeposit();
                if (deposit != null && deposit.getResourceType() == primaryOutput) {
                    totalModifier *= deposit.getYield();
                    LOGGER.fine("Applied resource deposit yield: " + deposit.getYield());
                }
            }
        }

        int finalOutput = Math.round(baseOutput * totalModifier);

        LOGGER.fine(String.format("%s producing %d %s (base: %d, modifier: %.2f)",
                getName(), finalOutput, primaryOutput.getName(), baseOutput, totalModifier));

        production.put(primaryOutput, finalOutput);
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
        LOGGER.fine(String.format("Applied modifier to %s: %s = %.2f",
                getName(), source, factor));
    }

    public void removeModifier(String source) {
        productionModifiers.remove(source);
        LOGGER.fine("Removed modifier from " + getName() + ": " + source);
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