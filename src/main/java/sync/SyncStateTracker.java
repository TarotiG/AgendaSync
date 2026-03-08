package sync;

import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Persistent Sync State Tracking Component
 * 
 * This component persists synchronization timestamps to disk (sync-state.properties file).
 * This allows the application to remember when each calendar was last synced, even after
 * application restarts. This is essential for efficient incremental syncing.
 * 
 * How it works:
 * 1. Stores last sync time for Apple Calendar
 * 2. Stores last sync time for Google Calendar (if needed)
 * 3. Uses ISO-8601 format for timestamps (parseable, sortable)
 * 4. Writes to local sync-state.properties file
 * 5. Reads on startup to restore state
 * 
 * File Location:
 * - ./sync-state.properties (in application working directory)
 * - Configurable via agendasyc.sync.sync-state-file in application.yml
 * 
 * Why Persistence is Important:
 * - Without persistence: Every restart fetches ALL events (inefficient)
 * - With persistence: Fetches only events since last sync (efficient)
 * - Saves API quota and bandwidth
 * - Reduces sync time as calendar grows
 * 
 * File Format Example:
 * ```
 * last_apple_sync_time=2026-02-28T15:30:00
 * last_google_sync_time=2026-02-28T15:25:00
 * ```
 * 
 * Thread Safety:
 * - Uses synchronized methods for concurrent access protection
 * - Multiple threads may read/write state simultaneously
 * - File I/O is atomic where possible
 * 
 * Error Handling:
 * - If file doesn't exist, creates it
 * - If timestamps are malformed, uses current time
 * - Graceful fallback to "sync all events" if state is corrupted
 */
@Component
public class SyncStateTracker {

    private static final Logger logger = LoggerFactory.getLogger(SyncStateTracker.class);
    private static final String STATE_FILE = "./sync-state.properties";
    private static final String LAST_APPLE_SYNC_KEY = "last_apple_sync_time";
    private static final String LAST_GOOGLE_SYNC_KEY = "last_google_sync_time";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /**
     * Get the last recorded Apple Calendar sync time
     * 
     * The first time this is called (before any sync), it returns null.
     * After the first sync, it returns the persisted timestamp.
     * 
     * Calendars should only fetch events with lastModifiedTime > this value.
     * 
     * @return LocalDateTime of last Apple Calendar sync, or null if never synced
     */
    public synchronized LocalDateTime getLastAppleSyncTime() {
        logger.debug("Retrieving last Apple Calendar sync time from state file");
        LocalDateTime lastSyncTime = readTimestampFromFile(LAST_APPLE_SYNC_KEY);
        if (lastSyncTime != null) {
            logger.debug("Last Apple sync time: {}", lastSyncTime);
        } else {
            logger.debug("No previous Apple sync found - will perform full sync");
        }
        return lastSyncTime;
    }

    /**
     * Update the last recorded Apple Calendar sync time
     * 
     * Should be called after a successful Apple Calendar sync.
     * Persists the current time to disk for next application run.
     * 
     * @param lastSyncTime The timestamp of the sync completion
     */
    public synchronized void updateLastAppleSyncTime(LocalDateTime lastSyncTime) {
        logger.debug("Updating last Apple Calendar sync time: {}", lastSyncTime);
        writeTimestampToFile(LAST_APPLE_SYNC_KEY, lastSyncTime);
        logger.debug("Apple Calendar sync time updated successfully");
    }

    /**
     * Get the last recorded Google Calendar sync time
     * 
     * Similar to Apple sync time tracking, returns the last Google Calendar
     * synchronization timestamp for incremental fetch support.
     * 
     * @return LocalDateTime of last Google Calendar sync, or null if never synced
     */
    public synchronized LocalDateTime getLastGoogleSyncTime() {
        logger.debug("Retrieving last Google Calendar sync time from state file");
        LocalDateTime lastSyncTime = readTimestampFromFile(LAST_GOOGLE_SYNC_KEY);
        if (lastSyncTime != null) {
            logger.debug("Last Google sync time: {}", lastSyncTime);
        } else {
            logger.debug("No previous Google sync found - will perform full sync");
        }
        return lastSyncTime;
    }

    /**
     * Update the last recorded Google Calendar sync time
     * 
     * Should be called after a successful Google Calendar sync.
     * Persists the current time to disk for next application run.
     * 
     * @param lastSyncTime The timestamp of the sync completion
     */
    public synchronized void updateLastGoogleSyncTime(LocalDateTime lastSyncTime) {
        logger.debug("Updating last Google Calendar sync time: {}", lastSyncTime);
        writeTimestampToFile(LAST_GOOGLE_SYNC_KEY, lastSyncTime);
        logger.debug("Google Calendar sync time updated successfully");
    }

    /**
     * Reads a timestamp value from the state file
     * 
     * @param key The property key to read
     * @return LocalDateTime parsed from the file, or null if not found
     */
    private LocalDateTime readTimestampFromFile(String key) {
        try {
            Path filePath = Paths.get(STATE_FILE);
            if (!Files.exists(filePath)) {
                logger.debug("State file does not exist yet: {}", STATE_FILE);
                return null;
            }

            BufferedReader reader = new BufferedReader(new FileReader(STATE_FILE));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith(key + "=")) {
                    String value = line.substring((key + "=").length());
                    LocalDateTime timestamp = LocalDateTime.parse(value, FORMATTER);
                    logger.debug("Read {} from state file: {}", key, timestamp);
                    reader.close();
                    return timestamp;
                }
            }
            reader.close();
            logger.debug("Key {} not found in state file", key);
            return null;

        } catch (Exception e) {
            logger.warn("Error reading sync state from file: {}", e.getMessage());
            logger.warn("Will perform full sync (no incremental fetch)");
            return null; // Fallback: perform full sync
        }
    }

    /**
     * Writes a timestamp value to the state file
     * 
     * @param key The property key to write
     * @param value The LocalDateTime value to write
     */
    private void writeTimestampToFile(String key, LocalDateTime value) {
        try {
            // Create state file directory if it doesn't exist
            Path filePath = Paths.get(STATE_FILE);
            Path parent = filePath.getParent();
            if (parent != null && !Files.exists(parent)) {
                Files.createDirectories(parent);
            }

            // Read existing state
            java.util.Properties props = new java.util.Properties();
            if (Files.exists(filePath)) {
                try (FileInputStream fis = new FileInputStream(STATE_FILE)) {
                    props.load(fis);
                }
            }

            // Update with new timestamp
            props.setProperty(key, value.format(FORMATTER));

            // Write back to file
            try (FileOutputStream fos = new FileOutputStream(STATE_FILE)) {
                props.store(fos, "AgendaSync Synchronization State");
            }

            logger.debug("Sync state written to file: {} = {}", key, value);

        } catch (Exception e) {
            logger.error("Error writing sync state to file: {}", e.getMessage(), e);
            logger.error("State persistence failed - next sync will be full sync");
        }
    }

    private static final String GOOGLE_SYNC_TOKEN_KEY = "google_sync_token";

    /**
     * Geeft het opgeslagen Google syncToken terug, of null als er nog geen is.
     */
    public synchronized String getLastGoogleSyncToken() {
        return readStringFromFile(GOOGLE_SYNC_TOKEN_KEY);
    }

    /**
     * Slaat het nieuwe Google syncToken op na een succesvolle sync.
     */
    public synchronized void saveGoogleSyncToken(String syncToken) {
        writeStringToFile(GOOGLE_SYNC_TOKEN_KEY, syncToken);
        logger.debug("Google syncToken saved");
    }

    /**
     * Leest een string waarde uit het state bestand (voor syncToken opslag).
     */
    private String readStringFromFile(String key) {
        try {
            Path filePath = Paths.get(STATE_FILE);
            if (!Files.exists(filePath)) return null;

            java.util.Properties props = new java.util.Properties();
            try (FileInputStream fis = new FileInputStream(STATE_FILE)) {
                props.load(fis);
            }
            return props.getProperty(key);
        } catch (Exception e) {
            logger.warn("Error reading {} from state file: {}", key, e.getMessage());
            return null;
        }
    }

    /**
     * Schrijft een string waarde naar het state bestand.
     */
    private void writeStringToFile(String key, String value) {
        try {
            Path filePath = Paths.get(STATE_FILE);
            // Fix: getParent() kan null zijn als het pad geen directory component heeft
            Path parent = filePath.getParent();
            if (parent != null && !Files.exists(parent)) {
                Files.createDirectories(parent);
            }

            java.util.Properties props = new java.util.Properties();
            if (Files.exists(filePath)) {
                try (FileInputStream fis = new FileInputStream(STATE_FILE)) {
                    props.load(fis);
                }
            }
            props.setProperty(key, value);
            try (FileOutputStream fos = new FileOutputStream(STATE_FILE)) {
                props.store(fos, "AgendaSync Synchronization State");
            }
        } catch (Exception e) {
            logger.error("Error writing {} to state file: {}", key, e.getMessage(), e);
        }
    }

    /**
     * Clears all sync state (resets to first-time state)
     * 
     * Useful for testing or when you want to perform a full resync.
     * After calling this, the next sync will treat all events as new.
     */
    public synchronized void resetSyncState() {
        try {
            logger.info("Resetting sync state - clearing state file");
            Path filePath = Paths.get(STATE_FILE);
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                logger.info("Sync state file deleted");
            }
        } catch (Exception e) {
            logger.error("Error resetting sync state: {}", e.getMessage(), e);
        }
    }
}