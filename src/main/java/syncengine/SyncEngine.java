package syncengine;

import calendar.apple.AppleCalendarService;
import calendar.google.GoogleCalendarService;
// import net.fortuna.ical4j.model.Date;
// import net.fortuna.ical4j.model.Property;
// import net.fortuna.ical4j.model.parameter.Value;
import syncengine.mappers.EventMapper;
import syncengine.sync.SyncEventDto;

import net.fortuna.ical4j.model.component.VEvent;
import sync.SyncStateTracker;
import syncengine.services.EventService;

import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.ParseException;
import java.time.LocalDateTime;
// import java.util.ArrayList;
import java.util.List;

public class SyncEngine {

    private static final Logger logger = LoggerFactory.getLogger(SyncEngine.class);

    // // SERVICES
    final GoogleCalendarService _googleCalendarService;
    final AppleCalendarService _appleCalendarService;
    public final EventService _eventService;
    final SyncStateTracker _syncStateTracker;

    public SyncEngine(
        GoogleCalendarService googleCalendarService,
        AppleCalendarService appleCalendarService,
        EventService eventService,
        SyncStateTracker syncStateTracker
    ) {
        this._googleCalendarService = googleCalendarService;
        this._appleCalendarService = appleCalendarService;
        this._eventService = eventService;
        this._syncStateTracker = syncStateTracker;
    }

    // METHODS
    /**
     * Ontvangt alle Google Events van een Google Calendar en mapt deze naar een SyncEngine Events
     * @return Lijst van SyncEngine Events
     */
    public List<SyncEventDto> receiveGoogleEvents() throws IOException, GeneralSecurityException {
        logger.debug("Fetching events from Google Calendar");
        Calendar calendar = _googleCalendarService.connectToPlatform();
        List<Event> googleEvents = _googleCalendarService.retrieveAllCalendarItems(calendar);
        logger.info("Retrieved {} events from Google Calendar", googleEvents.size());

        return EventMapper.mapGoogleEventsToSyncEventDto(googleEvents);
    }

    // AANPASSEN
    public List<SyncEventDto> receiveAppleEvents() throws Exception {
        LocalDateTime syncTime = _syncStateTracker.getLastAppleSyncTime();

        if (syncTime != null) {
            logger.debug("Incremental Apple fetch: retrieving events since {}", syncTime);
        } else {
            logger.info("No previous Apple sync found — performing initial full sync (last 30 days + future)");
        }

        List<VEvent> vEvents = _appleCalendarService.retrieveAllCalendarItems(syncTime);
        logger.info("Retrieved {} events from Apple Calendar", vEvents.size());

        return EventMapper.mapAppleVEventsToSyncDtos(vEvents);
    }

    public void sendGoogleAgendaNewEvent(SyncEventDto event) throws IOException, GeneralSecurityException {
        logger.debug("Sending event to Google Calendar: {}", event.title);
        Calendar calendar = _googleCalendarService.connectToPlatform();

        Event googleEvent = EventMapper.mapSyncEventDtoBackToGoogleEvent(event);
        calendar.events().insert("primary", googleEvent).execute();
        logger.info("Event '{}' created and sent to Google Calendar", googleEvent.getSummary());
    }

    public void sendGoogleAgendaNewUpdates(List<SyncEventDto> syncEventDtoList) throws IOException, GeneralSecurityException {
        logger.debug("Sending {} events to Google Calendar", syncEventDtoList.size());
        Calendar calendar = _googleCalendarService.connectToPlatform();

        List<Event> events = EventMapper.mapSyncEventsDtoBackToGoogleEvents(syncEventDtoList);

        events.forEach( (event) -> {
            try {
                calendar.events().insert("primary", event).execute();
                logger.debug("Event '{}' created on date {} and sent to Google Calendar", event.getSummary(), event.getStart());
            } catch (IOException e) {
                logger.error("Error sending event to Google Calendar: {}", e.getMessage(), e);
            }
        });
        logger.info("Completed sending {} events to Google Calendar", events.size());
    }

    public void sendAppleAgendaNewEvent(SyncEventDto event) {

    }

    /**
     * Synchronize Apple Calendar events to Google Calendar
     * 
     * This is the main method called by the scheduler every 15 minutes.
     * It:
     * 1. Fetches all events from Apple Calendar
     * 2. Maps them to internal SyncEventDto format
     * 3. Sends them to Google Calendar
     * 4. Logs the operation
     * 
     * This method is called by:
     * - SyncScheduler (every 15 minutes)
     * - Health checks for manual trigger
     * 
     * Error handling: Exceptions are logged but not thrown (graceful degradation)
     */
    public void syncAppleCalendarToGoogle() {
        logger.info("========== Starting Apple->Google Calendar Sync ==========");
        try {
            logger.debug("Step 1: Fetching events from Apple Calendar");
            List<SyncEventDto> appleEvents = receiveAppleEvents();
            logger.info("Fetched {} events from Apple Calendar", appleEvents.size());

            if (appleEvents.isEmpty()) {
                logger.info("No events to sync from Apple Calendar");
                logger.info("========== Completed Apple->Google Sync (no changes) ==========");
                return;
            }

            logger.debug("Step 2: Sending {} events to Google Calendar", appleEvents.size());
            sendGoogleAgendaNewUpdates(appleEvents);
            logger.info("Successfully synced {} events from Apple to Google", appleEvents.size());

            logger.info("========== Completed Apple->Google Calendar Sync ==========");

        } catch (Exception e) {
            logger.error("Error during Apple->Google sync: {}", e.getMessage(), e);
            logger.error("Apple->Google sync failed - will retry at next scheduled interval");
        }
    }

    /**
     * Synchronize Google Calendar events to Apple Calendar
     * 
     * This method is called when Google Calendar webhooks are received (real-time).
     * It:
     * 1. Fetches all events from Google Calendar
     * 2. Maps them to internal SyncEventDto format
     * 3. Sends them to Apple Calendar
     * 4. Logs the operation
     * 
     * This method is called by:
     * - GoogleAgendaController (webhook endpoint)
     * - Manual triggers via REST API
     * 
     * Error handling: Exceptions are logged but not thrown (graceful degradation)
     */
    public void syncGoogleCalendarToApple() {
        logger.info("========== Starting Google->Apple Calendar Sync ==========");
        try {
            logger.debug("Step 1: Fetching events from Google Calendar");
            List<SyncEventDto> googleEvents = receiveGoogleEvents();
            logger.info("Fetched {} events from Google Calendar", googleEvents.size());

            if (googleEvents.isEmpty()) {
                logger.info("No events to sync from Google Calendar");
                logger.info("========== Completed Google->Apple Sync (no changes) ==========");
                return;
            }

            logger.debug("Step 2: Sending {} events to Apple Calendar", googleEvents.size());
            sendAppleAgendaNewUpdates(googleEvents);
            logger.info("Successfully synced {} events from Google to Apple", googleEvents.size());

            logger.info("========== Completed Google->Apple Calendar Sync ==========");

        } catch (Exception e) {
            logger.error("Error during Google->Apple sync: {}", e.getMessage(), e);
            logger.error("Google->Apple sync failed - will retry on next webhook");
        }
    }

    public void sendAppleAgendaNewUpdates(List<SyncEventDto> events) throws ParseException {
        logger.debug("Sending {} events to Apple Calendar", events.size());

        List<VEvent> appleEvents = EventMapper.mapSyncEventDtoBackToAppleVEvents(events);

        appleEvents.forEach( (event) -> {
            try {
                _appleCalendarService.sendIcsToAppleCalendar(event);
                logger.debug("Event '{}' created and sent to Apple Calendar", event.getSummary().getValue());
            } catch (IOException e) {
                logger.error("Error sending event to Apple Calendar: {}", e.getMessage(), e);
            }
        });
        logger.info("Completed sending {} events to Apple Calendar", appleEvents.size());
    }
}
