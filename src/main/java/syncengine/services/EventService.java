package syncengine.services;

import calendar.sync.CalendarType;
import calendar.sync.SyncEventDto;

import java.util.HashMap;
import java.util.List;

/**
 * De service die het handelen van de events regelt voor beide Google en Apple
 */
public class EventService {
    HashMap<String, SyncEventDto> updateEventMap = new HashMap<>();

    public SyncEventDto createNewEvent(String calendar) {
        SyncEventDto event = new SyncEventDto();

        switch (calendar.toLowerCase()) {
            case "google" -> event.eventOrigin = CalendarType.GOOGLE;
            case "apple" -> event.eventOrigin = CalendarType.APPLE;
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
