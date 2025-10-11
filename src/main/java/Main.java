import calendar.apple.AppleCalendarService;
import calendar.google.GoogleCalendarService;
import calendar.mappers.EventMapper;

import calendar.sync.SyncEventDto;
import com.google.api.services.calendar.model.Event;
import syncengine.SyncEngine;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

// Omzetten naar een Spring Boot Application Main class
public class Main {

    public static void main(String... args) throws Exception, IOException, GeneralSecurityException {
        SyncEngine syncEngine = new SyncEngine();
//        List<SyncEventDto> syncEngineGoogleEvents = syncEngine.receiveGoogleEvents();
        List<SyncEventDto> syncEngineAppleEvents = syncEngine.receiveAppleEvents();

        for(SyncEventDto event : syncEngineAppleEvents) {
            System.out.println(event.title);
        }
//
//        syncEngine.sendGoogleAgendaNewEvent(syncEngine.eventService.createNewEvent("google"));

//        AppleCalendarService.execute();
        // Versturen en vergelijken met DB State

        // IF New, Updated of Removed => Agenda's syncen
        // ELSE niks
    }
}