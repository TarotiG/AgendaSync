import calendar.apple.AppleCalendarService;
import syncengine.sync.SyncEventDto;
import syncengine.SyncEngine;
import syncengine.utilities.TestDataGenerator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

// Omzetten naar een Spring Boot Application Main class
public class Main {

    public static void main(String... args) throws Exception, IOException, GeneralSecurityException {
        SyncEngine syncEngine = new SyncEngine();
        List<SyncEventDto> syncEngineGoogleEvents = syncEngine.receiveGoogleEvents();
        List<SyncEventDto> syncEngineAppleEvents = syncEngine.receiveAppleEvents();

        // Vergelijk nieuwe events met bestaande events in db
        // 1. Ophalen syncEvents van db
        // 2. Google events vergelijken
        // 3. Apple events vergelijken



        // IF New, Updated of Removed => sturen naar db en agenda's syncen
        // ELSE niks

        syncEngine.sendGoogleAgendaNewEvent(syncEngine.eventService.createNewEvent("google"));
        syncEngine.sendGoogleAgendaNewUpdates(syncEngineAppleEvents);

        ArrayList<SyncEventDto> events = new ArrayList<>();
        events.add(TestDataGenerator.generateSyncEventDto());
        syncEngine.sendAppleAgendaNewUpdates(events);
    }
}