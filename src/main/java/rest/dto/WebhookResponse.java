package rest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;

/**
 * Standard Response DTO for Webhook Endpoints
 * 
 * This DTO provides a consistent response format for all webhook-related endpoints:
 * - Status: success or failure
 * - Message: Human-readable message about the operation
 * - Timestamp: When the response was generated
 * - RequestId: Unique identifier for tracking/debugging
 * - Data: Optional payload with additional information
 * 
 * This standardization helps with:
 * - API consistency
 * - Error tracking and debugging
 * - Client-side response handling
 * - Monitoring and alerting systems
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WebhookResponse {
    private String status;
    private String message;
    private LocalDateTime timestamp;
    private String requestId;
    private Object data;

    // Constructors
    public WebhookResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public WebhookResponse(String status, String message) {
        this();
        this.status = status;
        this.message = message;
    }

    public WebhookResponse(String status, String message, Object data) {
        this();
        this.status = status;
        this.message = message;
        this.data = data;
    }

    // Static factory methods for common responses
    /**
     * Creates a success response
     */
    public static WebhookResponse success(String message) {
        return new WebhookResponse("success", message);
    }

    /**
     * Creates a success response with data payload
     */
    public static WebhookResponse success(String message, Object data) {
        return new WebhookResponse("success", message, data);
    }

    /**
     * Creates an error response
     */
    public static WebhookResponse error(String message) {
        return new WebhookResponse("error", message);
    }

    /**
     * Creates an error response with error details
     */
    public static WebhookResponse error(String message, Object error) {
        return new WebhookResponse("error", message, error);
    }

    // Getters and Setters
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
