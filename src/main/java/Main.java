import syncengine.sync.SyncEventDto;
import syncengine.SyncEngine;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

// Omzetten naar een Spring Boot Application Main class
public class Main {

    public static void main(String... args) throws Exception, IOException, GeneralSecurityException {
        SyncEngine syncEngine = new SyncEngine();
        List<SyncEventDto> syncEngineGoogleEvents = syncEngine.receiveGoogleEvents();
        List<SyncEventDto> syncEngineAppleEvents = syncEngine.receiveAppleEvents();

        // Vergelijk nieuwe events met bestaande events in db

        // IF New, Updated of Removed => sturen naar db en agenda's syncen
        // ELSE niks

        syncEngine.sendGoogleAgendaNewEvent(syncEngine.eventService.createNewEvent("google"));
    }
}