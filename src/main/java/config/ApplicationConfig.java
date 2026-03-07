package config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import calendar.google.GoogleCalendarService;
import calendar.apple.AppleCalendarService;
import syncengine.SyncEngine;
import syncengine.services.EventService;
import sync.SyncStateTracker;

/**
 * Spring Configuration for Bean Management
 * 
 * This configuration class defines Spring-managed beans for the application's core services.
 * By using @Configuration and @Bean, we leverage Spring's dependency injection container
 * instead of manually creating instances in Main.java.
 * 
 * Benefits:
 * - Centralized bean management
 * - Easier testing with mock beans
 * - Lifecycle management (initialization/destruction)
 * - Lazy instantiation where applicable
 */
@Configuration
public class ApplicationConfig {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationConfig.class);

    /**
     * Creates and configures GoogleCalendarService bean
     * 
     * The GoogleCalendarService manages all interactions with Google Calendar API:
     * - OAuth2 authentication
     * - Event retrieval from Google Calendar
     * - Event creation/update in Google Calendar
     * 
     * @return GoogleCalendarService singleton instance
     */
    public GoogleCalendarService googleCalendarService() {
        logger.debug("Initializing GoogleCalendarService bean");
        return new GoogleCalendarService();
    }

    /**
     * Creates and configures AppleCalendarService bean
     * 
     * The AppleCalendarService manages all interactions with Apple Calendar via CalDAV:
     * - CalDAV protocol handling
     * - Event retrieval from Apple Calendar
     * - Event creation/update in Apple Calendar
     * 
     * @return AppleCalendarService singleton instance
     */
    public AppleCalendarService appleCalendarService() {
        logger.debug("Initializing AppleCalendarService bean");
        return new AppleCalendarService();
    }

    /**
     * Creates and configures EventService bean
     * 
     * The EventService contains business logic for event operations:
     * - Event creation
     * - Event comparison
     * - Event transformation
     * 
     * @return EventService singleton instance
     */
    public EventService eventService() {
        logger.debug("Initializing EventService bean");
        return new EventService();
    }

    /**
     * Creates and configures SyncEngine bean
     * 
     * The SyncEngine is the core orchestrator that:
     * - Coordinates synchronization between calendars
     * - Invokes GoogleCalendarService and AppleCalendarService
     * - Manages the sync workflow
     * 
     * @return SyncEngine singleton instance
     */
    @Bean
    public SyncEngine syncEngine(GoogleCalendarService googleCalendarService,
                                  AppleCalendarService appleCalendarService,
                                  EventService eventService,
                                  SyncStateTracker syncStateTracker
                                ) {
        logger.debug("Initializing SyncEngine bean with dependencies");
        SyncEngine syncEngine = new SyncEngine(googleCalendarService, appleCalendarService, eventService, syncStateTracker);
        // SyncEngine will auto-wire these services
        return syncEngine;
    }
}
