package com.colonygenesis.ui;

import com.colonygenesis.core.Game;
import com.colonygenesis.event.EventBus;
import com.colonygenesis.event.EventListener;
import com.colonygenesis.event.GameEvent;
import com.colonygenesis.event.events.BuildingEvent;
import com.colonygenesis.event.events.ResourceEvent;
import com.colonygenesis.resource.ResourceType;
import com.colonygenesis.util.LoggerUtils;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.kordamp.bootstrapfx.scene.layout.Panel;

import java.util.EnumMap;
import java.util.Map;
import java.util.logging.Logger;

public class ResourcePanel extends Panel implements EventListener {
    private static final Logger LOGGER = LoggerUtils.getLogger(ResourcePanel.class);

    private final Game game;
    private final GridPane resourceGrid;
    private final Map<ResourceType, Label> resourceLabels;
    private final EventBus eventBus;

    public ResourcePanel(Game game) {
        this.game = game;
        this.eventBus = EventBus.getInstance();
        this.resourceGrid = new GridPane();
        this.resourceLabels = new EnumMap<>(ResourceType.class);

        // Register for events that affect resources
        eventBus.register(this,
                GameEvent.EventType.RESOURCE_CHANGED,
                GameEvent.EventType.BUILDING_PLACED,
                GameEvent.EventType.BUILDING_COMPLETED,
                GameEvent.EventType.BUILDING_ACTIVATED,
                GameEvent.EventType.BUILDING_DEACTIVATED,
                GameEvent.EventType.BUILDING_REMOVED
        );

        initializePanel();

        LOGGER.info("ResourcePanel initialized");
    }

    private void initializePanel() {
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        resourceGrid.setHgap(10);
        resourceGrid.setVgap(5);

        int row = 0;
        for (ResourceType type : ResourceType.values()) {
            // Skip non-basic resources for now
            if (!type.isBasic()) continue;

            // Resource name
            Label nameLabel = new Label(type.getName() + ":");
            nameLabel.setTextFill(type.getColor());
            nameLabel.setFont(Font.font("System", FontWeight.BOLD, 12));

            // Resource amount
            Label amountLabel = new Label("0");
            resourceLabels.put(type, amountLabel);

            // Add to grid
            resourceGrid.add(nameLabel, 0, row);
            resourceGrid.add(amountLabel, 1, row);

            row++;
        }

        content.getChildren().add(resourceGrid);
        setBody(content);
        getStyleClass().add("panel-primary");

        // Initial update
        update(game.getResourceManager().getAllResources(), game.getResourceManager().getAllNetProduction());
    }

    public void update(Map<ResourceType, Integer> resources, Map<ResourceType, Integer> production) {
        for (ResourceType type : resourceLabels.keySet()) {
            Label label = resourceLabels.get(type);
            int amount = resources.getOrDefault(type, 0);
            int net = production.getOrDefault(type, 0);

            // Get capacity if applicable
            int capacity = game.getResourceManager().getCapacity(type);

            // Format: Amount/Capacity (Net change)
            String text;
            if (type.isStorable()) {
                text = amount + "/" + capacity + " ";
            } else {
                text = amount + " ";
            }

            if (net > 0) {
                text += "(+" + net + ")";
                label.setTextFill(Color.GREEN);
            } else if (net < 0) {
                text += "(" + net + ")";
                label.setTextFill(Color.RED);
            } else {
                text += "(0)";
                label.setTextFill(Color.BLACK);
            }

            label.setText(text);

            // Visual warning if close to capacity
            if (type.isStorable() && amount > capacity * 0.8) {
                label.setStyle("-fx-font-weight: bold; -fx-background-color: #ffe0e0;");
            } else {
                label.setStyle("");
            }
        }
    }

    @Override
    public void onEvent(GameEvent event) {
        Platform.runLater(() -> {
            // Always recalculate net production for these events
            boolean updateProduction = false;

            switch (event.getType()) {
                case RESOURCE_CHANGED:
                    // Just update for the changed resources
                    updateProduction = true;
                    break;

                case BUILDING_PLACED:
                case BUILDING_COMPLETED:
                case BUILDING_ACTIVATED:
                case BUILDING_DEACTIVATED:
                case BUILDING_REMOVED:
                    // Building changes affect production
                    updateProduction = true;
                    break;
            }

            if (updateProduction) {
                // Get the latest values
                Map<ResourceType, Integer> resources = game.getResourceManager().getAllResources();

                // Recalculate production based on current buildings
                Map<ResourceType, Integer> production = game.getBuildingManager().calculateTotalProduction();

                update(resources, production);
                LOGGER.fine("Updated resource display due to " + event.getType());
            }
        });
    }

    @Override
    public boolean isInterestedIn(GameEvent.EventType eventType) {
        switch (eventType) {
            case RESOURCE_CHANGED:
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