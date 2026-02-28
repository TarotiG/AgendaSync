package rest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * DTO for Google Calendar Webhook Request
 * 
 * When Google Calendar detects changes to a calendar you're subscribed to,
 * it sends a webhook notification with this payload structure.
 * 
 * Note: The actual event data is typically NOT included in the webhook payload.
 * Instead, the webhook signals that changes occurred, and the application
 * must fetch the updates via Google Calendar API.
 * 
 * Required headers (validated separately in controller):
 * - X-Goog-Resource-ID: Identifies the calendar resource
 * - X-Goog-Message-Number: Sequential notification number
 * - X-Goog-Resource-State: sync, exists, or not_exists
 * - X-Goog-Channel-ID: Identifies the notification channel
 * - X-Goog-Channel-Token: Token for channel verification
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GoogleWebhookRequest {
    private String resourceId;
    private String resourceState;
    private String messageNumber;
    private String channelId;
    private String channelToken;

    // Constructors
    public GoogleWebhookRequest() {
    }

    public GoogleWebhookRequest(String resourceId, String resourceState, String messageNumber) {
        this.resourceId = resourceId;
        this.resourceState = resourceState;
        this.messageNumber = messageNumber;
    }

    // Getters and Setters
    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getResourceState() {
        return resourceState;
    }

    public void setResourceState(String resourceState) {
        this.resourceState = resourceState;
    }

    public String getMessageNumber() {
        return messageNumber;
    }

    public void setMessageNumber(String messageNumber) {
        this.messageNumber = messageNumber;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getChannelToken() {
        return channelToken;
    }

    public void setChannelToken(String channelToken) {
        this.channelToken = channelToken;
    }

    @Override
    public String toString() {
        return "GoogleWebhookRequest{" +
                "resourceId='" + resourceId + '\'' +
                ", resourceState='" + resourceState + '\'' +
                ", messageNumber='" + messageNumber + '\'' +
                ", channelId='" + channelId + '\'' +
                '}';
    }
}
