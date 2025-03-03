package com.colonygenesis.core;

import com.colonygenesis.ui.UserInterface;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.kordamp.bootstrapfx.BootstrapFX;

import static javafx.application.Application.launch;

public class ExoplanetColonyApp extends Application {

    private Game game;

    @Override
    public void start(Stage primaryStage) {
        // Initialize game
        game = new Game();
        game.initializeGame();

        // Create the UI with the game
        UserInterface ui = new UserInterface(game);

        // Create scene with our UI as the root
        Scene scene = new Scene(ui, 1280, 800);

        // Apply Bootstrap CSS
        scene.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());

        // Configure stage
        primaryStage.setTitle("Exoplanet: Colony Genesis");
        primaryStage.setScene(scene);
        primaryStage.show();

        // After UI is shown, store reference in Game
        game.setUserInterface(ui);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
