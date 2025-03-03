package com.colonygenesis.resource;

import javafx.scene.paint.Color;

public enum ResourceType {
    // Basic resources
    FOOD("Food", "Sustains your colony population", Color.GREEN, true, true),
    ENERGY("Energy", "Powers buildings and operations", Color.YELLOW, true, false),
    MATERIALS("Materials", "Used for construction and maintenance", Color.BROWN, true, true),
    WATER("Water", "Essential for life support and agriculture", Color.LIGHTBLUE, true, true),
    RESEARCH("Research", "Advances technology", Color.PURPLE, true, false),

    // Advanced resources
    RARE_MINERALS("Rare Minerals", "Advanced construction material", Color.SILVER, false, true),
    ALIEN_COMPOUNDS("Alien Compounds", "Mysterious alien substances", Color.MAGENTA, false, true);

    private final String name;
    private final String description;
    private final Color color;
    private final boolean basic; // Is this a basic resource type?
    private final boolean storable; // Can this resource be stored?
    private final int baseStorage; // Base storage capacity

    ResourceType(String name, String description, Color color, boolean basic, boolean storable) {
        this.name = name;
        this.description = description;
        this.color = color;
        this.basic = basic;
        this.storable = storable;

        // Set base storage capacities
        if (storable) {
            if (basic) {
                this.baseStorage = 1000; // Basic resources have higher initial storage
            } else {
                this.baseStorage = 500; // Advanced resources have lower initial storage
            }
        } else {
            this.baseStorage = 0; // Non-storable resources (like energy) consumed each turn
        }
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Color getColor() {
        return color;
    }

    public boolean isBasic() {
        return basic;
    }

    public boolean isStorable() {
        return storable;
    }

    public int getBaseStorage() {
        return baseStorage;
    }

    // Get the building type that increases storage for this resource
    public String getStorageBuilding() {
        switch (this) {
            case FOOD:
                return "Food Silo";
            case MATERIALS:
                return "Warehouse";
            case WATER:
                return "Water Tank";
            case RARE_MINERALS:
                return "Secure Vault";
            case ALIEN_COMPOUNDS:
                return "Containment Facility";
            default:
                return "No storage building";
        }
    }
}