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

    // List<SyncEventDto> returnen
    public static List<SyncEventDto> mapGoogleEventsToSyncEventDto(List<Event> googleEvents) {
        ArrayList<SyncEventDto> syncEventDtoList = new ArrayList<>();

        for(Event event : googleEvents) {
            SyncEventDto syncEventDto = new SyncEventDto();
            syncEventDto.id = event.getId();
            syncEventDto.title = event.getSummary();
            syncEventDto.description = event.getDescription();
            syncEventDto.created = event.getCreated();
            syncEventDto.updated = event.getUpdated();
            syncEventDto.startDateTime = event.getStart();
            syncEventDto.endDateTime = event.getEnd();
            syncEventDto.location = event.getLocation();
            syncEventDto.iCalUID = event.getICalUID();
//            syncEventDto.organizerEmail = event.getOrganizer();

            syncEventDtoList.add(syncEventDto);
        }

        return syncEventDtoList;
    }

    public static List<Event> mapSyncEventDtoBackToGoogleEvent(List<SyncEventDto> syncEventDtos) {
        return new ArrayList<>();
    }

     public static SyncCalendarDto mapAppleCalendarToSyncDto() {
        return new SyncCalendarDto();
    }
}
