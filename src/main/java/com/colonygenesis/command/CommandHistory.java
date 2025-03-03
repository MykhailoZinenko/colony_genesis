package com.colonygenesis.command;

import com.colonygenesis.util.LoggerUtils;
import com.colonygenesis.util.Result;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.logging.Logger;

/**
 * Tracks command execution and supports undo/redo operations.
 */
public class CommandHistory {
    private static final Logger LOGGER = LoggerUtils.getLogger(CommandHistory.class);
    private static final int MAX_HISTORY_SIZE = 20;

    private final Deque<Command> undoStack = new ArrayDeque<>();
    private final Deque<Command> redoStack = new ArrayDeque<>();

    /**
     * Executes a command and adds it to the history if successful.
     *
     * @param command The command to execute
     * @return The result of the command execution
     */
    public Result<?> executeCommand(Command command) {
        Result<?> result = command.execute();

        if (result.isSuccess() && command.isUndoable()) {
            undoStack.push(command);
            redoStack.clear(); // Clear redo stack when a new command is executed

            // Limit history size
            if (undoStack.size() > MAX_HISTORY_SIZE) {
                undoStack.removeLast();
            }
        }

        return result;
    }

    /**
     * Undoes the most recent command.
     *
     * @return The result of the undo operation
     */
    public Result<?> undo() {
        if (undoStack.isEmpty()) {
            return Result.failure("Nothing to undo");
        }

        Command command = undoStack.pop();
        Result<?> result = command.undo();

        if (result.isSuccess()) {
            redoStack.push(command);
        } else {
            // If undo failed, put the command back
            undoStack.push(command);
        }

        return result;
    }

    /**
     * Redoes the most recently undone command.
     *
     * @return The result of the redo operation
     */
    public Result<?> redo() {
        if (redoStack.isEmpty()) {
            return Result.failure("Nothing to redo");
        }

        Command command = redoStack.pop();
        Result<?> result = command.execute();

        if (result.isSuccess()) {
            undoStack.push(command);
        } else {
            // If redo failed, put the command back
            redoStack.push(command);
        }

        return result;
    }

    /**
     * Checks if there are commands that can be undone.
     */
    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    /**
     * Checks if there are commands that can be redone.
     */
    public boolean canRedo() {
        return !redoStack.isEmpty();
    }

    /**
     * Clears the command history.
     */
    public void clear() {
        undoStack.clear();
        redoStack.clear();
    }
}