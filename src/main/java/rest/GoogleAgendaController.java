package rest;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rest.dto.WebhookResponse;
import rest.dto.GoogleWebhookRequest;
import config.SecretsConfig;
import syncengine.SyncEngine;
import java.util.UUID;

/**
 * REST Controller for Google Calendar Webhook Integration
 * 
 * This controller exposes HTTP endpoints to receive real-time notifications from
 * Google Calendar when events are created, updated, or deleted.
 * 
 * How it works:
 * 1. Application subscribes to Google Calendar changes via push notification channel
 * 2. Google sends HTTP POST requests to this endpoint when changes occur
 * 3. Controller validates the webhook is legitimate (header validation)
 * 4. Triggers an immediate sync of Google Calendar events
 * 5. Returns 200 OK to acknowledge receipt (Google expects this)
 * 
 * Webhook Validation:
 * - X-Goog-Resource-ID: Must match expected calendar resource ID
 * - X-Goog-Channel-ID: Must match configured notification channel
 * - X-Goog-Channel-Token: Must match configured channel token
 * - X-Goog-Message-Number: Tracks webhook delivery count
 * - X-Goog-Resource-State: Indicates type of notification (sync, exists, not_exists)
 * 
 * Benefits of webhooks over polling:
 * - Real-time notification of changes (seconds vs minutes)
 * - Reduced API calls since we only sync when changes occur
 * - Faster end-user experience
 */
@RestController
@RequestMapping("/api/webhooks")
public class GoogleAgendaController {

    private static final Logger logger = LoggerFactory.getLogger(GoogleAgendaController.class);

    // Google Calendar webhook header constants
    private static final String HEADER_RESOURCE_ID = "X-Goog-Resource-ID";
    private static final String HEADER_MESSAGE_NUMBER = "X-Goog-Message-Number";
    private static final String HEADER_RESOURCE_STATE = "X-Goog-Resource-State";
    private static final String HEADER_CHANNEL_ID = "X-Goog-Channel-ID";
    private static final String HEADER_CHANNEL_TOKEN = "X-Goog-Channel-Token";

    @Autowired
    private SyncEngine syncEngine;

    @Autowired
    private SecretsConfig.GoogleWebhookSecrets googleWebhookSecrets;

    /**
     * POST endpoint: /api/webhooks/google-calendar
     * 
     * Receives webhook notifications from Google Calendar indicating that calendar
     * events have changed. The notification doesn't include the event data itself;
     * instead, it signals that a sync should occur.
     * 
     * Header Validation:
     * - X-Goog-Resource-ID: The calendar resource that changed
     * - X-Goog-Channel-ID: Must match our configured channel ID
     * - X-Goog-Channel-Token: Must match our configured channel token
     * - X-Goog-Message-Number: Sequential number for tracking
     * - X-Goog-Resource-State: Type of change (sync, exists, not_exists, sync)
     * 
     * Response codes:
     * - 200 OK: Webhook received and processed successfully (Google expects this)
     * - 400 Bad Request: Missing or invalid headers (webhook rejected)
     * - 500 Internal Server Error: Sync failed (still acknowledge to Google)
     * 
     * @param resourceId Header X-Goog-Resource-ID
     * @param messageNumber Header X-Goog-Message-Number
     * @param resourceState Header X-Goog-Resource-State
     * @param channelId Header X-Goog-Channel-ID
     * @param channelToken Header X-Goog-Channel-Token
     * @return ResponseEntity with status and message
     */
    @PostMapping("/google-calendar")
    public ResponseEntity<WebhookResponse> handleGoogleCalendarWebhook(
            @RequestHeader(value = HEADER_RESOURCE_ID, required = false) String resourceId,
            @RequestHeader(value = HEADER_MESSAGE_NUMBER, required = false) String messageNumber,
            @RequestHeader(value = HEADER_RESOURCE_STATE, required = false) String resourceState,
            @RequestHeader(value = HEADER_CHANNEL_ID, required = false) String channelId,
            @RequestHeader(value = HEADER_CHANNEL_TOKEN, required = false) String channelToken) {

        String requestId = UUID.randomUUID().toString();
        logger.info("Received Google Calendar webhook [RequestID: {}]", requestId);

        try {
            // Validate webhook headers
            WebhookValidationResult validationResult = validateWebhookHeaders(
                    resourceId, messageNumber, resourceState, channelId, channelToken, requestId);

            if (!validationResult.isValid) {
                logger.warn("Webhook validation failed: {} [RequestID: {}]", validationResult.reason, requestId);
                WebhookResponse response = WebhookResponse.error(validationResult.reason);
                response.setRequestId(requestId);
                return ResponseEntity.badRequest().body(response);
            }

            logger.debug("Webhook headers validated successfully [RequestID: {}]", requestId);

            // Log webhook details for monitoring
            logger.info("Google Calendar webhook details: ResourceID={}, State={}, MessageNumber={} [RequestID: {}]",
                    resourceId, resourceState, messageNumber, requestId);

            // Trigger synchronization from Google Calendar to Apple Calendar
            logger.debug("Triggering sync from Google Calendar to Apple Calendar [RequestID: {}]", requestId);
            syncEngine.syncGoogleCalendarToApple();

            WebhookResponse response = WebhookResponse.success(
                    "Google Calendar webhook processed successfully");
            response.setRequestId(requestId);
            response.setData("Sync triggered for Google Calendar changes");

            logger.info("Google Calendar webhook processed successfully [RequestID: {}]", requestId);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error processing Google Calendar webhook: {} [RequestID: {}]",
                    e.getMessage(), requestId, e);

            // Still return 200 OK to Google (so they don't keep retrying)
            // But log the error for debugging
            WebhookResponse response = WebhookResponse.error(
                    "Webhook received but sync encountered an error: " + e.getMessage());
            response.setRequestId(requestId);
            return ResponseEntity.ok(response); // 200 OK even on sync error
        }
    }

    /**
     * Health check endpoint for webhook receiver
     * 
     * Useful for monitoring systems to verify that the webhook endpoint
     * is reachable and the application is running.
     * 
     * @return ResponseEntity with status
     */
    @GetMapping("/google-calendar/status")
    public ResponseEntity<WebhookResponse> getWebhookStatus() {
        logger.debug("Webhook status check requested");
        WebhookResponse response = WebhookResponse.success("Google Calendar webhook endpoint is active");
        return ResponseEntity.ok(response);
    }

    /**
     * Validates incoming webhook headers against expected values
     * 
     * Webhook header validation ensures that:
     * 1. All required headers are present (fail-safe against misconfigurations)
     * 2. Channel ID matches our configured channel
     * 3. Channel Token matches our configured token
     * 
     * This prevents malicious or accidental requests from triggering syncs.
     * 
     * @param resourceId The resource ID from webhook
     * @param messageNumber The message number from webhook
     * @param resourceState The resource state from webhook
     * @param channelId The channel ID from webhook
     * @param channelToken The channel token from webhook
     * @param requestId Unique request identifier for logging
     * @return WebhookValidationResult containing validation status and reason
     */
    private WebhookValidationResult validateWebhookHeaders(
            String resourceId, String messageNumber, String resourceState,
            String channelId, String channelToken, String requestId) {

        logger.debug("Validating webhook headers [RequestID: {}]", requestId);

        // Check for required headers
        if (resourceId == null || resourceId.isEmpty()) {
            return new WebhookValidationResult(false, "Missing required header: X-Goog-Resource-ID");
        }
        if (messageNumber == null || messageNumber.isEmpty()) {
            return new WebhookValidationResult(false, "Missing required header: X-Goog-Message-Number");
        }
        if (resourceState == null || resourceState.isEmpty()) {
            return new WebhookValidationResult(false, "Missing required header: X-Goog-Resource-State");
        }
        if (channelId == null || channelId.isEmpty()) {
            return new WebhookValidationResult(false, "Missing required header: X-Goog-Channel-ID");
        }
        if (channelToken == null || channelToken.isEmpty()) {
            return new WebhookValidationResult(false, "Missing required header: X-Goog-Channel-Token");
        }

        // Validate channel ID matches configured channel
        if (googleWebhookSecrets.channelId != null && !googleWebhookSecrets.channelId.isEmpty()) {
            if (!channelId.equals(googleWebhookSecrets.channelId)) {
                logger.warn("Webhook Channel ID mismatch. Expected: {}, Got: {} [RequestID: {}]",
                        googleWebhookSecrets.channelId, channelId, requestId);
                return new WebhookValidationResult(false,
                        "Channel ID mismatch - unauthorized webhook");
            }
        }

        // Validate channel token matches configured token
        if (googleWebhookSecrets.channelToken != null && !googleWebhookSecrets.channelToken.isEmpty()) {
            if (!channelToken.equals(googleWebhookSecrets.channelToken)) {
                logger.warn("Webhook Channel Token mismatch [RequestID: {}]", requestId);
                return new WebhookValidationResult(false,
                        "Channel token invalid - unauthorized webhook");
            }
        }

        logger.debug("Webhook header validation successful [RequestID: {}]", requestId);
        return new WebhookValidationResult(true, "Validation successful");
    }

    /**
     * Inner class to hold webhook validation results
     */
    private static class WebhookValidationResult {
        boolean isValid;
        String reason;

        WebhookValidationResult(boolean isValid, String reason) {
            this.isValid = isValid;
            this.reason = reason;
        }
    }
}
