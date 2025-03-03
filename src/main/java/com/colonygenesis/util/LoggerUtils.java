package com.colonygenesis.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.*;

public final class LoggerUtils {
    private static final String LOG_FOLDER = "logs";
    private static final String LOG_FILE = "colony-genesis.log";
    private static boolean initialized = false;

    private LoggerUtils() {
        // Private constructor to prevent instantiation
    }

    public static synchronized void initialize() {
        if (initialized) {
            return;
        }

        try {
            // Create logs directory if it doesn't exist
            Path logDir = Paths.get(LOG_FOLDER);
            if (!Files.exists(logDir)) {
                Files.createDirectories(logDir);
            }

            // Create and configure the file handler
            FileHandler fileHandler = new FileHandler(LOG_FOLDER + "/" + LOG_FILE, true);
            fileHandler.setFormatter(new SimpleFormatter());

            // Configure the console handler
            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setFormatter(new SimpleFormatter());

            // Configure the root logger
            Logger rootLogger = Logger.getLogger("");
            rootLogger.setLevel(Level.INFO);

            // Remove existing handlers
            Handler[] handlers = rootLogger.getHandlers();
            for (Handler handler : handlers) {
                rootLogger.removeHandler(handler);
            }

            // Add our handlers
            rootLogger.addHandler(fileHandler);
            rootLogger.addHandler(consoleHandler);

            initialized = true;

            Logger.getLogger(LoggerUtils.class.getName()).info("Logging system initialized");
        } catch (IOException e) {
            System.err.println("Failed to initialize logging: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static Logger getLogger(Class<?> clazz) {
        if (!initialized) {
            initialize();
        }
        return Logger.getLogger(clazz.getName());
    }

    // Convenience methods
    public static void logDebug(Class<?> clazz, String message) {
        getLogger(clazz).fine(message);
    }

    public static void logInfo(Class<?> clazz, String message) {
        getLogger(clazz).info(message);
    }

    public static void logWarning(Class<?> clazz, String message) {
        getLogger(clazz).warning(message);
    }

    public static void logError(Class<?> clazz, String message, Throwable throwable) {
        getLogger(clazz).log(Level.SEVERE, message, throwable);
    }

    public static void logError(Class<?> clazz, String message) {
        getLogger(clazz).severe(message);
    }
}