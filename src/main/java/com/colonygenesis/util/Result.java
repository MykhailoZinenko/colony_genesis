package com.colonygenesis.util;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A class that represents the result of an operation which can either succeed with a value
 * or fail with an error message.
 *
 * @param <T> The type of the value in case of success
 */
public class Result<T> {
    private final T value;
    private final String errorMessage;
    private final boolean success;

    private Result(T value, String errorMessage, boolean success) {
        this.value = value;
        this.errorMessage = errorMessage;
        this.success = success;
    }

    /**
     * Creates a successful result with the given value.
     */
    public static <T> Result<T> success(T value) {
        return new Result<>(value, null, true);
    }

    /**
     * Creates a successful result with no value (for void operations).
     */
    public static <T> Result<T> success() {
        return new Result<>(null, null, true);
    }

    /**
     * Creates a failed result with the given error message.
     */
    public static <T> Result<T> failure(String errorMessage) {
        return new Result<>(null, errorMessage, false);
    }

    /**
     * Whether this result represents a successful operation.
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Whether this result represents a failed operation.
     */
    public boolean isFailure() {
        return !success;
    }

    /**
     * Returns the value if this is a success, or null if it's a failure.
     * Consider using {@link #getValue()} instead for null-safety.
     */
    public T getValueOrNull() {
        return value;
    }

    /**
     * Returns the value wrapped in an Optional.
     */
    public Optional<T> getValue() {
        return Optional.ofNullable(value);
    }

    /**
     * Returns the error message if this is a failure, or null if it's a success.
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Executes the given action if this result is a success.
     */
    public Result<T> onSuccess(Consumer<T> action) {
        if (success && action != null) {
            action.accept(value);
        }
        return this;
    }

    /**
     * Executes the given action if this result is a failure.
     */
    public Result<T> onFailure(Consumer<String> action) {
        if (!success && action != null) {
            action.accept(errorMessage);
        }
        return this;
    }

    /**
     * Maps the value of this result if it's a success.
     */
    public <R> Result<R> map(Function<T, R> mapper) {
        if (success) {
            return Result.success(mapper.apply(value));
        } else {
            return Result.failure(errorMessage);
        }
    }

    /**
     * Returns the value if this is a success, or the given default value if it's a failure.
     */
    public T getOrElse(T defaultValue) {
        return success ? value : defaultValue;
    }
}