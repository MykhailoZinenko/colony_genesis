package com.colonygenesis.building;

import com.colonygenesis.util.Result;

/**
 * Interface for building operations that affect game state.
 */
public interface BuildOperation {
    /**
     * Builds the structure on the specified tile.
     *
     * @return Result of the operation
     */
    Result<?> execute();

    /**
     * Gets the building associated with this operation.
     *
     * @return The building
     */
    Building getBuilding();
}