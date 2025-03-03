package com.colonygenesis.core;

import com.colonygenesis.controller.GameController;
import com.colonygenesis.ui.UserInterface;
import com.colonygenesis.util.LoggerUtils;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.kordamp.bootstrapfx.BootstrapFX;

import java.util.logging.Logger;

public class ExoplanetColonyApp extends Application {
    private static final Logger LOGGER = LoggerUtils.getLogger(ExoplanetColonyApp.class);

    private Game game;
    private GameController gameController;

    @Override
    public void start(Stage primaryStage) {
        LOGGER.info("Starting Exoplanet: Colony Genesis");

        // Initialize logging system
        LoggerUtils.initialize();

        // Initialize game
        game = new Game();
        game.initializeGame();

        // Initialize the game controller
        gameController = new GameController(game);

        // Create the UI with the game and controller
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

        LOGGER.info("Application started successfully");
    }

    public static void main(String[] args) {
        launch(args);
    }
}