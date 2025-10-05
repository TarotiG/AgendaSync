package syncengine.services;

import calendar.sync.CalendarType;
import calendar.sync.SyncEventDto;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.EventDateTime;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * De service die het handelen van de events regelt voor beide Google en Apple
 */
public class EventService {
    HashMap<String, SyncEventDto> updateEventMap = new HashMap<>();

    public SyncEventDto createNewEvent(String calendar) {
        SyncEventDto event = new SyncEventDto();

        switch (calendar.toLowerCase()) {
            case "google":
                event.eventOrigin = CalendarType.GOOGLE;
//                event.startDateTime = new EventDateTime().setDateTime("2025-10-06T10:00:00+02:00");
//                event.endDateTime = new EventDateTime().setDateTime("2025-10-07T10:00:00+02:00");
                break;
            case "apple":
                event.eventOrigin = CalendarType.APPLE;
                break;
        }
        return event;
    }

    /**
     * Vind een manier om de HashMap te gebruiken
     * Update een bestaande event van de achterlopende agenda obv de nieuwe status van een event
     * @param events Een lijst met 2 events; positie 1 is de event dat ge-updated moet worden en
     *               op positie 2 de event wat het moet matchen
     * @return Een event dat is ge-updated
     */
    public SyncEventDto updateExistingEvent(List<SyncEventDto> events) {
        return new SyncEventDto();
    }

    public void removeEventFromCalendar(String idEvent) {

    }
}
