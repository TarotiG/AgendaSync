package scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import syncengine.SyncEngine;
import sync.SyncStatus;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Scheduled Tasks for Calendar Synchronization
 * 
 * This component uses Spring's @Scheduled annotation to automatically invoke
 * synchronization methods on a fixed schedule. This is a lightweight alternative
 * to external scheduling frameworks (like Quartz) and is built into Spring.
 * 
 * How Spring Scheduling Works:
 * 1. @EnableScheduling activates the scheduling feature (in Main.java)
 * 2. @Scheduled methods run on a default thread pool
 * 3. Tasks execute at specified intervals automatically
 * 4. No database or external configuration needed
 * 
 * Polling Strategy:
 * - Apple Calendar: Polled every 15 minutes (configurable)
 * - Google Calendar: Not polled (receives webhooks instead)
 * - Sequential execution (Apple first, then compare)
 * 
 * Error Handling:
 * - Exceptions are caught and logged
 * - Sync failure doesn't prevent next execution
 * - Graceful degradation (one calendar failure doesn't stop the other)
 * 
 * Performance Optimization:
 * - Uses SyncStateTracker to fetch only new events (not all events)
 * - Tracks last sync timestamp to minimize API calls
 * - Reduces bandwidth and API quota consumption over time
 */
@Component
public class SyncScheduler {

    private static final Logger logger = LoggerFactory.getLogger(SyncScheduler.class);

    // Polling interval: 15 minutes = 900,000 milliseconds
    private static final long APPLE_POLL_INTERVAL_MS = 300000;

    @Autowired
    private SyncEngine syncEngine;

    @Autowired
    private SyncStatus syncStatus;

    /**
     * Scheduled task: Poll Apple Calendar every 15 minutes
     * 
     * This method is automatically invoked by Spring's scheduler at fixed intervals.
     * It fetches new/updated events from Apple Calendar and syncs them to Google Calendar.
     * 
     * Why 15 minutes?
     * - Balances between real-time updates and API call frequency
     * - Typical for polling-based integrations
     * - Allows reasonable responsiveness for calendar events
     * - Keeps API quota usage manageable
     * 
     * Execution:
     * - First run: 15 minutes after application startup
     * - Subsequent runs: Every 15 minutes thereafter
     * - Fixed delay: If sync takes 2 minutes, next starts in 15 minutes
     * 
     * fixedDelay Behavior:
     * - If SYNC_INTERVAL_MS = 900,000 and sync takes 2 minutes:
     *   - Sync starts at 0:00, ends at 0:02
     *   - Next sync starts at 0:17 (15 minutes from END of previous sync)
     * - This ensures consistent poll frequency regardless of sync duration
     */
    @Scheduled(fixedDelay = APPLE_POLL_INTERVAL_MS, initialDelay = APPLE_POLL_INTERVAL_MS)
    public void pollAppleCalendar() {
        LocalDateTime startTime = LocalDateTime.now();
        logger.info("========== Starting Apple Calendar Sync Cycle ==========");
        logger.info("Polling Apple Calendar for new/updated events");

        try {
            // Mark as syncing
            syncStatus.setSyncing(true);
            syncStatus.setSyncStartTime(startTime);

            // Fetch and sync Apple Calendar events
            logger.debug("Invoking syncEngineAppleCalendarToGoogle()");
            syncEngine.syncAppleCalendarToGoogle();

            // Calculate sync duration
            LocalDateTime endTime = LocalDateTime.now();
            long durationMs = ChronoUnit.MILLIS.between(startTime, endTime);
            syncStatus.setLastAppleSyncTime(endTime);
            syncStatus.setLastSyncDuration(durationMs);

            logger.info("Apple Calendar sync completed successfully ({}ms)",
                    durationMs);
            logger.info("========== Completed Apple Calendar Sync Cycle ==========");

        } catch (Exception e) {
            logger.error("Error during Apple Calendar sync: {}", e.getMessage(), e);
            logger.error("Apple Calendar sync failed - will retry at next scheduled time");

        } finally {
            // Mark as not syncing
            syncStatus.setSyncing(false);
            syncStatus.setNextAppleSyncTime(
                    LocalDateTime.now().plusSeconds(APPLE_POLL_INTERVAL_MS / 1000));
        }
    }

    /**
     * Future Enhancement: Sync from Google to Apple periodically
     * 
     * Currently, Google->Apple syncing is triggered by webhooks (real-time).
     * In the future, you may want to add a periodic sync to catch any updates
     * from Google that weren't captured by webhooks.
     * 
     * Example (currently disabled):
     * @Scheduled(fixedDelay = 1800000) // Every 30 minutes
     * public void pollGoogleCalendar() {
     *     logger.info("Polling Google Calendar via scheduled task");
     *     syncEngine.syncGoogleCalendarToApple();
     * }
     */
}
