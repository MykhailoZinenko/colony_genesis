package com.colonygenesis.command;

import com.colonygenesis.util.Result;

/**
 * Interface for the Command pattern.
 * Represents an action that can be executed and potentially undone.
 */
public interface Command {
    /**
     * Executes the command.
     * @return Result of the execution
     */
    Result<?> execute();

    /**
     * Checks if the command can be undone.
     * @return true if the command can be undone, false otherwise
     */
    default boolean isUndoable() {
        return false;
    }

    /**
     * Undoes the command.
     * @return Result of the undo operation
     */
    default Result<?> undo() {
        return Result.failure("Command does not support undo");
    }
}