package sync;

import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
// import java.time.temporal.ChronoUnit;

/**
 * Sync Status Tracking Component
 * 
 * Tracks the current state of the synchronization process in memory.
 * Provides information about:
 * - Whether a sync is currently in progress
 * - When the last sync completed
 * - How long the last sync took
 * - When the next sync is scheduled
 * 
 * This information is used by:
 * - Health check endpoints (/api/health/status)
 * - UI dashboards for monitoring
 * - Alerting systems for sync failures
 * 
 * Thread Safety:
 * - Uses synchronized methods for thread-safe updates
 * - Multiple threads may access this concurrently (scheduler, webhooks, REST calls)
 * 
 * In-Memory Only:
 * - Status is stored in memory only (lost on application restart)
 * - For persistent tracking across restarts, see SyncStateTracker
 * 
 * Persistence:
 * - Last sync timestamps are also persisted to sync-state.properties
 * - This component complements SyncStateTracker for real-time monitoring
 */
@Component
public class SyncStatus {
    private boolean isSyncing = false;
    private LocalDateTime lastAppleSyncTime;
    private LocalDateTime lastGoogleSyncTime;
    private LocalDateTime nextAppleSyncTime;
    private LocalDateTime syncStartTime;
    private long lastSyncDuration = 0;

    /**
     * Check if a sync is currently in progress
     * @return true if syncing, false otherwise
     */
    public synchronized boolean isSyncing() {
        return isSyncing;
    }

    /**
     * Set the sync status
     * @param syncing true if sync is in progress, false otherwise
     */
    public synchronized void setSyncing(boolean syncing) {
        this.isSyncing = syncing;
    }

    /**
     * Get the timestamp of the last Apple Calendar sync
     * @return LocalDateTime of last sync, or null if never synced
     */
    public synchronized LocalDateTime getLastAppleSyncTime() {
        return lastAppleSyncTime;
    }

    /**
     * Set the timestamp of the last Apple Calendar sync
     * @param lastAppleSyncTime Timestamp of sync completion
     */
    public synchronized void setLastAppleSyncTime(LocalDateTime lastAppleSyncTime) {
        this.lastAppleSyncTime = lastAppleSyncTime;
    }

    /**
     * Get the timestamp of the last Google Calendar sync
     * @return LocalDateTime of last sync, or null if never synced
     */
    public synchronized LocalDateTime getLastGoogleSyncTime() {
        return lastGoogleSyncTime;
    }

    /**
     * Set the timestamp of the last Google Calendar sync
     * @param lastGoogleSyncTime Timestamp of sync completion
     */
    public synchronized void setLastGoogleSyncTime(LocalDateTime lastGoogleSyncTime) {
        this.lastGoogleSyncTime = lastGoogleSyncTime;
    }

    /**
     * Get the scheduled time of the next Apple Calendar sync
     * @return LocalDateTime of next scheduled sync
     */
    public synchronized LocalDateTime getNextAppleSyncTime() {
        return nextAppleSyncTime;
    }

    /**
     * Set the scheduled time of the next Apple Calendar sync
     * @param nextAppleSyncTime Timestamp of next scheduled sync
     */
    public synchronized void setNextAppleSyncTime(LocalDateTime nextAppleSyncTime) {
        this.nextAppleSyncTime = nextAppleSyncTime;
    }

    /**
     * Get the duration (in milliseconds) of the last sync
     * @return Duration in milliseconds
     */
    public synchronized long getLastSyncDuration() {
        return lastSyncDuration;
    }

    /**
     * Set the duration (in milliseconds) of the last sync
     * @param lastSyncDuration Duration in milliseconds
     */
    public synchronized void setLastSyncDuration(long lastSyncDuration) {
        this.lastSyncDuration = lastSyncDuration;
    }

    /**
     * Set the start time of the current/last sync
     * @param syncStartTime Timestamp when sync started
     */
    public synchronized void setSyncStartTime(LocalDateTime syncStartTime) {
        this.syncStartTime = syncStartTime;
    }

    /**
     * Get the start time of the current/last sync
     * @return Timestamp when sync started
     */
    public synchronized LocalDateTime getSyncStartTime() {
        return syncStartTime;
    }
}
