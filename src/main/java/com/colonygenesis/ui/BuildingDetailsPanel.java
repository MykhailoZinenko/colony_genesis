package com.colonygenesis.ui;

import com.colonygenesis.building.Building;
import com.colonygenesis.resource.ResourceType;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.kordamp.bootstrapfx.scene.layout.Panel;

import java.util.Map;

public class BuildingDetailsPanel extends Panel {
    private final Label titleLabel;
    private final Label typeLabel;
    private final Label statusLabel;
    private final GridPane productionGrid;
    private final ProgressBar constructionProgress;
    private final Label constructionLabel;

    public BuildingDetailsPanel() {
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        titleLabel = new Label("Building Details");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

        typeLabel = new Label("Type: None");
        statusLabel = new Label("Status: None");

        constructionProgress = new ProgressBar(0);
        constructionProgress.setPrefWidth(200);
        constructionLabel = new Label("Construction: 0%");

        productionGrid = new GridPane();
        productionGrid.setHgap(10);
        productionGrid.setVgap(5);

        content.getChildren().addAll(
                titleLabel, typeLabel, statusLabel,
                constructionLabel, constructionProgress,
                new Label("Production:"), productionGrid
        );

        setBody(content);
        getStyleClass().add("panel-info");

        // Hide construction progress by default
        constructionProgress.setVisible(false);
        constructionLabel.setVisible(false);
    }

    public void update(Building building) {
        if (building == null) {
            clear();
            return;
        }

        titleLabel.setText(building.getName());
        typeLabel.setText("Type: " + building.getType().getName());

        if (!building.isCompleted()) {
            statusLabel.setText("Status: Under Construction");

            // Show construction progress
            constructionProgress.setVisible(true);
            constructionLabel.setVisible(true);

            double totalTime = building.getConstructionTime();
            double remainingTime = building.getRemainingConstructionTime();
            double progress = (totalTime - remainingTime) / totalTime;

            constructionProgress.setProgress(progress);
            constructionLabel.setText(String.format("Construction: %.0f%%", progress * 100));
        } else {
            boolean active = building.isActive();
            statusLabel.setText("Status: " + (active ? "Active" : "Inactive"));
            statusLabel.setTextFill(active ? Color.GREEN : Color.RED);

            // Hide construction progress
            constructionProgress.setVisible(false);
            constructionLabel.setVisible(false);
        }

        // Update production information
        updateProductionInfo(building);
    }

    private void updateProductionInfo(Building building) {
        productionGrid.getChildren().clear();

        int row = 0;
        Map<ResourceType, Integer> production = building.getProduction();

        if (production.isEmpty()) {
            Label noProductionLabel = new Label("No production");
            productionGrid.add(noProductionLabel, 0, row, 2, 1);
        } else {
            for (Map.Entry<ResourceType, Integer> entry : production.entrySet()) {
                ResourceType type = entry.getKey();
                int amount = entry.getValue();

                Label resourceLabel = new Label(type.getName() + ":");
                resourceLabel.setTextFill(type.getColor());

                Label amountLabel = new Label(
                        amount > 0 ? "+" + amount : String.valueOf(amount)
                );

                if (amount > 0) {
                    amountLabel.setTextFill(Color.GREEN);
                } else if (amount < 0) {
                    amountLabel.setTextFill(Color.RED);
                }

                productionGrid.add(resourceLabel, 0, row);
                productionGrid.add(amountLabel, 1, row);
                row++;
            }
        }

        // Add maintenance costs
        if (!building.getMaintenanceCost().isEmpty()) {
            Label maintenanceLabel = new Label("Maintenance:");
            maintenanceLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
            productionGrid.add(maintenanceLabel, 0, row, 2, 1);
            row++;

            for (Map.Entry<ResourceType, Integer> entry : building.getMaintenanceCost().entrySet()) {
                ResourceType type = entry.getKey();
                int amount = entry.getValue();

                Label resourceLabel = new Label(type.getName() + ":");
                resourceLabel.setTextFill(type.getColor());

                Label amountLabel = new Label("-" + amount);
                amountLabel.setTextFill(Color.RED);

                productionGrid.add(resourceLabel, 0, row);
                productionGrid.add(amountLabel, 1, row);
                row++;
            }
        }
    }

    public void clear() {
        titleLabel.setText("Building Details");
        typeLabel.setText("Type: None");
        statusLabel.setText("Status: None");
        productionGrid.getChildren().clear();
        constructionProgress.setVisible(false);
        constructionLabel.setVisible(false);
    }
}