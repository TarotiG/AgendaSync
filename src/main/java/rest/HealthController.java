package rest;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rest.dto.WebhookResponse;
import sync.SyncStatus;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Health Check Endpoint Controller
 * 
 * Provides endpoints for monitoring the application's health and sync status.
 * These endpoints are publicly accessible (not authenticated) to allow:
 * - External monitoring systems to check application status
 * - Load balancers to verify instance health
 * - DevOps dashboards to display real-time status
 * 
 * Endpoints exposed:
 * - GET /api/health/status: Detailed sync information
 * - GET /api/health/ping: Simple liveness probe
 */
@RestController
@RequestMapping("/api/health")
public class HealthController {

    private static final Logger logger = LoggerFactory.getLogger(HealthController.class);

    @Autowired
    private SyncStatus syncStatus;

    /**
     * GET endpoint: /api/health/status
     * 
     * Returns comprehensive sync and application status information.
     * Useful for monitoring dashboards and debugging.
     * 
     * Response includes:
     * - applicationStatus: Overall application status (running, error, etc.)
     * - lastAppleSyncTime: When Apple Calendar was last synced
     * - lastGoogleSyncTime: When Google Calendar was last synced
     * - isSyncing: Whether a sync is currently in progress
     * - nextAppleSyncTime: When the next Apple Calendar poll will occur
     * - syncDuration: How long the last sync took
     * 
     * @return ResponseEntity with status information
     */
    @GetMapping("/status")
    public ResponseEntity<WebhookResponse> getStatus() {
        logger.debug("Health status check requested");

        try {
            Map<String, Object> statusData = new LinkedHashMap<>();
            statusData.put("applicationStatus", "running");
            statusData.put("lastAppleSyncTime", syncStatus.getLastAppleSyncTime());
            statusData.put("lastGoogleSyncTime", syncStatus.getLastGoogleSyncTime());
            statusData.put("isSyncing", syncStatus.isSyncing());
            statusData.put("nextAppleSyncTime", syncStatus.getNextAppleSyncTime());
            statusData.put("lastSyncDuration", syncStatus.getLastSyncDuration());

            WebhookResponse response = WebhookResponse.success(
                    "AgendaSync application is healthy");
            response.setData(statusData);

            logger.debug("Health status check completed successfully");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error retrieving health status", e);
            WebhookResponse response = WebhookResponse.error(
                    "Error retrieving health status: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    /**
     * GET endpoint: /api/health/ping
     * 
     * Simple liveness probe endpoint.
     * Returns 200 OK if the application is running.
     * 
     * Useful for:
     * - Load balancer health checks
     * - Kubernetes readiness probes
     * - Quick application responsiveness verification
     * 
     * @return ResponseEntity with simple pong message
     */
    @GetMapping("/ping")
    public ResponseEntity<WebhookResponse> ping() {
        logger.debug("Ping requested");
        WebhookResponse response = WebhookResponse.success("pong");
        return ResponseEntity.ok(response);
    }
}
