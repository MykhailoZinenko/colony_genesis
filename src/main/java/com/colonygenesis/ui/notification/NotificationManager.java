package com.colonygenesis.ui.notification;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages UI notifications shown to the user.
 */
public class NotificationManager {
    private static final int MAX_NOTIFICATIONS = 5;
    private static final int NOTIFICATION_DURATION_MS = 5000;

    private final VBox notificationArea;
    private final Map<Label, Timeline> activeNotifications;

    public NotificationManager() {
        notificationArea = new VBox(5);
        notificationArea.setAlignment(Pos.BOTTOM_RIGHT);
        notificationArea.setPrefWidth(300);
        notificationArea.setMaxHeight(200);
        activeNotifications = new HashMap<>();
    }

    /**
     * Shows a notification to the user.
     *
     * @param message The notification message
     * @param type The notification type
     */
    public void showNotification(String message, NotificationType type) {
        // Create notification label
        Label notification = createNotification(message, type);

        // Add to notification area (at the top)
        notificationArea.getChildren().add(0, notification);

        // Remove older notifications if we exceed the maximum
        if (notificationArea.getChildren().size() > MAX_NOTIFICATIONS) {
            Label oldestNotification = (Label) notificationArea.getChildren().get(MAX_NOTIFICATIONS);
            Timeline timeline = activeNotifications.remove(oldestNotification);
            if (timeline != null) {
                timeline.stop();
            }
            notificationArea.getChildren().remove(oldestNotification);
        }

        // Create fade-out animation
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(notification.opacityProperty(), 1.0)),
                new KeyFrame(Duration.millis(NOTIFICATION_DURATION_MS), event -> {
                    notificationArea.getChildren().remove(notification);
                    activeNotifications.remove(notification);
                }, new KeyValue(notification.opacityProperty(), 0.0))
        );

        // Store the timeline and play it
        activeNotifications.put(notification, timeline);
        timeline.play();
    }

    /**
     * Creates a styled notification label.
     */
    private Label createNotification(String message, NotificationType type) {
        Label notification = new Label(message);
        notification.setPrefWidth(280);
        notification.setWrapText(true);
        notification.setPadding(new javafx.geometry.Insets(10));
        notification.setAlignment(Pos.CENTER);

        // Apply styling based on type
        switch (type) {
            case INFO:
                notification.setStyle("-fx-background-color: #d9edf7; -fx-border-color: #bce8f1; -fx-text-fill: #31708f;");
                break;
            case SUCCESS:
                notification.setStyle("-fx-background-color: #dff0d8; -fx-border-color: #d6e9c6; -fx-text-fill: #3c763d;");
                break;
            case WARNING:
                notification.setStyle("-fx-background-color: #fcf8e3; -fx-border-color: #faebcc; -fx-text-fill: #8a6d3b;");
                break;
            case ERROR:
                notification.setStyle("-fx-background-color: #f2dede; -fx-border-color: #ebccd1; -fx-text-fill: #a94442;");
                break;
        }

        return notification;
    }

    /**
     * Gets the notification area container.
     */
    public VBox getNotificationArea() {
        return notificationArea;
    }

    /**
     * Clears all active notifications.
     */
    public void clearNotifications() {
        for (Timeline timeline : activeNotifications.values()) {
            timeline.stop();
        }

        activeNotifications.clear();
        notificationArea.getChildren().clear();
    }
}