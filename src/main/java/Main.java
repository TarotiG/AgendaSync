import calendar.google.GoogleCalendarService;
import calendar.mappers.EventMapper;

import calendar.sync.SyncEventDto;
import com.google.api.services.calendar.model.Event;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

// Omzetten naar een Spring Boot Application Main class
public class Main {

    public static void main(String... args) throws IOException, GeneralSecurityException {
        GoogleCalendarService _googleService = new GoogleCalendarService();

        var calendar = _googleService.connectToPlatform();
        List<Event> events = _googleService.retrieveAllCalendarItems(calendar);

        // Map voor SyncEngine
        List<SyncEventDto> syncedGoogleEvents = EventMapper.mapGoogleEventsToSyncEventDto(events);

        for(SyncEventDto eventDto : syncedGoogleEvents) {
            System.out.println(eventDto.id);
            System.out.println(eventDto.title);
            System.out.println(eventDto.description);
            System.out.println(eventDto.created);
            System.out.println(eventDto.startDateTime);
            System.out.println(eventDto.endDateTime);
        }
//        EventMapper.mapGoogleEventsToSyncEventDto(events);

        // Versturen en vergelijken met DB State

        // IF New, Updated of Removed => Agenda's syncen
        // ELSE niks

//        if (events.isEmpty()) {
//            System.out.println("No upcoming events found.");
//        } else {
//            System.out.println("Upcoming events");
//            for (Event event : events) {
//                DateTime start = event.getStart().getDateTime();
//                if (start == null) {
//                    start = event.getStart().getDate();
//                }
//                System.out.printf("%s (%s)\n", event.getSummary(), start);
//            }
//        }
    }
}