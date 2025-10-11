package calendar.mappers;

import calendar.sync.CalendarType;
import calendar.sync.SyncCalendarDto;
import calendar.sync.SyncEventDto;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.component.VEvent;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/*
Mapper class om Google Calendar Event objecten en Apple Calendar Event objecten te vertalen
naar een 'SyncEventDto' zodat het in dit format vergeleken kan worden.
 */
public class EventMapper {

    /**
    Mapt Google Calendar Events naar SyncEngine Events
    @param googleEvents Lijst van Google Calendar Events
     @return Lijst van SyncEngine events
    */
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
            syncEventDto.eventOrigin = CalendarType.GOOGLE;
//            syncEventDto.organizerEmail = event.getOrganizer();

            syncEventDtoList.add(syncEventDto);
        }

        return syncEventDtoList;
    }

    public static List<Event> mapSyncEventsDtoBackToGoogleEvents(List<SyncEventDto> syncEventDtos) {
        return new ArrayList<>();
    }

    public static Event mapSyncEventDtoBackToGoogleEvent(SyncEventDto event) {
        Event googleEvent = new Event();

        googleEvent.setSummary(event.title);
        googleEvent.setStart(event.startDateTime);
        googleEvent.setEnd(event.endDateTime);

        return googleEvent;
    }

    public static List<DateTime> createGoogleDateTimeForEvent(String startDate, String endDate) {
        return Arrays.asList(
                new DateTime(startDate),
                new DateTime(endDate)
        );
    }

    public static SyncCalendarDto mapAppleCalendarToSyncDto() {
        return new SyncCalendarDto();
    }

    // VERDER UITWERKEN NOG NIET HELEMAAL GOED
    public static List<SyncEventDto> mapAppleVEventsToSyncDtos(List<VEvent> events) {
        ArrayList<SyncEventDto> syncEvents = new ArrayList<SyncEventDto>();

        for(VEvent event : events) {
            SyncEventDto syncEventDto = new SyncEventDto();

//            syncEventDto.id = event.getId();
            syncEventDto.getVEventSummary(event);
            syncEventDto.getVEventDescription(event);
            syncEventDto.getVEventCreated(event);
            syncEventDto.getVEventStart(event);
            syncEventDto.getVEventEnd(event);
            syncEventDto.getVEventLocation(event);
            syncEventDto.getVEventICalUID(event);
            syncEventDto.eventOrigin = CalendarType.APPLE;
        }
        return new ArrayList<>();
    }
}
