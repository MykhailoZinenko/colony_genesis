package com.colonygenesis.command;

import com.colonygenesis.core.Game;
import com.colonygenesis.util.LoggerUtils;
import com.colonygenesis.util.Result;

import java.io.*;
import java.util.logging.Logger;

/**
 * Command for saving the game state.
 */
public class SaveGameCommand implements Command {
    private static final Logger LOGGER = LoggerUtils.getLogger(SaveGameCommand.class);

    private final Game game;
    private final File saveFile;

    /**
     * Creates a command to save the game.
     *
     * @param game The game to save
     * @param saveFile The file to save to
     */
    public SaveGameCommand(Game game, File saveFile) {
        this.game = game;
        this.saveFile = saveFile;
    }

    @Override
    public Result<File> execute() {
        if (game == null) {
            return Result.failure("Game cannot be null");
        }

        if (saveFile == null) {
            return Result.failure("Save file cannot be null");
        }

        LOGGER.info("Saving game to " + saveFile.getAbsolutePath());

        try (ObjectOutputStream oos = new ObjectOutputStream(
                new BufferedOutputStream(new FileOutputStream(saveFile)))) {

            // Create a wrapper to hold all game state
            GameState gameState = new GameState(game);

            // Write the game state to file
            oos.writeObject(gameState);

            LOGGER.info("Game saved successfully");
            return Result.success(saveFile);

        } catch (IOException e) {
            LOGGER.severe("Failed to save game: " + e.getMessage());
            return Result.failure("Failed to save game: " + e.getMessage());
        }
    }

    @Override
    public boolean isUndoable() {
        return false; // Saving can't be undone
    }

    @Override
    public Result<?> undo() {
        return Result.failure("Cannot undo save operation");
    }

    /**
     * Class to hold all game state for serialization.
     */
    private static class GameState implements Serializable {
        private static final long serialVersionUID = 1L;

        // Add all game state fields here
        // This will need to be expanded as the game grows

        private final int currentTurn;
        private final String currentPhase;

        public GameState(Game game) {
            this.currentTurn = game.getCurrentTurn();
            this.currentPhase = game.getTurnManager().getCurrentPhase().name();

            // Add more state extraction here
        }
    }
}