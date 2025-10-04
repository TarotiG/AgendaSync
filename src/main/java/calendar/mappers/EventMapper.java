package calendar.mappers;

import calendar.sync.SyncCalendarDto;
import calendar.sync.SyncEventDto;
import com.google.api.services.calendar.model.Event;

import java.util.ArrayList;
import java.util.List;

/*
Mapper class om Google Calendar Event objecten en Apple Calendar Event objecten te vertalen
naar een 'SyncEventDto' zodat het in dit format vergeleken kan worden.
 */
public class EventMapper {

    public static List<SyncEventDto> mapGoogleEventsToSyncEventDto(List<Event> googleEvents) {
        return new ArrayList<>();
    }

    public static List<Event> mapSyncEventDtoBackToGoogleEvent(List<SyncEventDto> syncEventDtos) {
        return new ArrayList<>();
    }

     public static SyncCalendarDto mapAppleCalendarToSyncDto() {
        return new SyncCalendarDto();
    }
}
