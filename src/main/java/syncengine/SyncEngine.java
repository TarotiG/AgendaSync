package syncengine;

import calendar.apple.AppleCalendarService;
import calendar.google.GoogleCalendarService;
import calendar.mappers.EventMapper;
import calendar.sync.SyncEventDto;

import net.fortuna.ical4j.model.component.VEvent;
import syncengine.services.EventService;

import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

public class SyncEngine {

    // SERVICES
    final GoogleCalendarService _googleCalendarService = new GoogleCalendarService();
    final AppleCalendarService _appleCalendarService = new AppleCalendarService();
    public final EventService eventService = new EventService();

    // METHODS
    /**
     * Ontvangt alle Google Events van een Google Calendar en mapt deze naar een SyncEngine Events
     * @return Lijst van SyncEngine Events
     */
    public List<SyncEventDto> receiveGoogleEvents() throws IOException, GeneralSecurityException {
        Calendar calendar = _googleCalendarService.connectToPlatform();
        List<Event> googleEvents = _googleCalendarService.retrieveAllCalendarItems(calendar);

        return EventMapper.mapGoogleEventsToSyncEventDto(googleEvents);
    }

    // AANPASSEN
    public List<SyncEventDto> receiveAppleEvents() throws Exception {
        List<VEvent> vEvents = _appleCalendarService.retrieveAllCalendarItems();

        return EventMapper.mapAppleVEventsToSyncDtos(vEvents);
    }

    public void sendGoogleAgendaNewEvent(SyncEventDto event) throws IOException, GeneralSecurityException {
        Calendar calendar = _googleCalendarService.connectToPlatform();

        Event googleEvent = EventMapper.mapSyncEventDtoBackToGoogleEvent(event);
        calendar.events().insert("primary", googleEvent).execute();
        System.out.printf("Event '%s' created and sent to Google Agenda!", googleEvent.getSummary());
    }

    // UITWERKEN
    public void sendGoogleAgendaNewUpdates(List<SyncEventDto> events) {

//        for(SyncEventDto event : events) {
//            System.out.printf("Event %s has been sent to the Google Agenda!%n", event.getSummary());
//        }
    }

    // UITWERKEN
    public void sendAppleAgendaNewUpdates(List<VEvent> events) {

        for(VEvent event : events) {
            System.out.printf("Event %s has been sent to the Google Agenda!%n", event.getSummary());
        }
    }

    /**
     * Event verzenden naar database
     */
    void sendEventToDb() {

    }

    /**
     * Status van een opgestuurde Event valideren of het al bestaat.
     * Indien het al bestaat voor 1 Agenda; teruggeven wat de volgende actie moet zijn
     * voor de achterlopende agenda
     */
    void validateStatusEventInDb() {

    }

    /**
     * Een Event aanmaken voor de agenda die achterloopt en terugsturen naar de achterlopende agenda
     */
    void sendNewEventForCalendar() {

    }

    /**
     * Een bestaande Event updaten bij de achterlopende agenda en vervolgens terugsturen naar deze agenda
     */
    void updateExistingEventForCalendar() {

    }

    /**
     * Als er twee vergelijkbare en/of dezelfde Events worden aangemaakt;
     * moet 1 van de 2 "prioriteit" krijgen. Vervolgens moet er bepaald worden welke actie moet volgen
     */
    void solveConflictingEvents() {

    }

    /**
     * Agenda's vergelijken en valideren dat ze gesynced zijn.
     * Indien niet gesynced; bepalen welke acties voldaan moeten worden om een "Synced" status te krijgen
     */
    void validateSyncedCalendars() {

    }
}
