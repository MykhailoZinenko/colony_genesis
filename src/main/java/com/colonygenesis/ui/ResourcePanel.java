package com.colonygenesis.ui;

import com.colonygenesis.core.Game;
import com.colonygenesis.resource.ResourceType;
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

public class ResourcePanel extends Panel {
    private Game game;
    private GridPane resourceGrid;
    private Map<ResourceType, Label> resourceLabels;

    // Update the ResourcePanel class:

    public ResourcePanel(Game game) {
        this.game = game;
        //setTitle("Colony Resources");

        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        resourceGrid = new GridPane();
        resourceGrid.setHgap(10);
        resourceGrid.setVgap(5);

        // Initialize resource labels
        resourceLabels = new EnumMap<>(ResourceType.class);

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
    }

    // Update ResourcePanel:

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
}