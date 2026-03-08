package syncengine.mappers;

import com.google.api.services.calendar.model.EventDateTime;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.property.*;
import syncengine.sync.SyncEventDto;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;

import net.fortuna.ical4j.model.component.VEvent;

import java.text.ParseException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
// import java.util.UUID;

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
//            syncEventDto.description = event.getDescription();
            syncEventDto.created = event.getCreated();
            syncEventDto.updated = event.getUpdated();
            syncEventDto.startDateTime = event.getStart();
            syncEventDto.endDateTime = event.getEnd();
            syncEventDto.location = event.getLocation();
            syncEventDto.iCalUID = event.getICalUID();
            
            // Extract or generate syncId for duplicate prevention
            if (event.getExtendedProperties() != null && 
                event.getExtendedProperties().getPrivate() != null &&
                event.getExtendedProperties().getPrivate().containsKey("syncId")) {
                syncEventDto.syncId = event.getExtendedProperties().getPrivate().get("syncId");
            } else {
                syncEventDto.syncId = "google_" + event.getId();
            }
            
            syncEventDto.setEventOrigin("google");
//            syncEventDto.organizerEmail = event.getOrganizer();

            syncEventDtoList.add(syncEventDto);
        }

        return syncEventDtoList;
    }

    public static List<Event> mapSyncEventsDtoBackToGoogleEvents(List<SyncEventDto> syncEventDtoList) {
        ArrayList<Event> googleEvents = new ArrayList<>();

        for (SyncEventDto event : syncEventDtoList) {
            Event googleEvent = EventMapper.mapSyncEventDtoBackToGoogleEvent(event);
            googleEvents.add(googleEvent);
        }

        return googleEvents;
    }

    public static Event mapSyncEventDtoBackToGoogleEvent(SyncEventDto event) {
        Event googleEvent = new Event();

        googleEvent.setSummary(event.title);
        googleEvent.setStart(event.startDateTime);
        googleEvent.setEnd(event.endDateTime);
        
        // Set syncId in extended properties for duplicate prevention
        if (event.syncId != null) {
            Map<String, String> privateProperties = new HashMap<>();
            privateProperties.put("syncId", event.syncId);
            com.google.api.services.calendar.model.Event.ExtendedProperties extendedProps = 
                new com.google.api.services.calendar.model.Event.ExtendedProperties();
            extendedProps.setPrivate(privateProperties);
            googleEvent.setExtendedProperties(extendedProps);
        }

        return googleEvent;
    }

    public static List<DateTime> createGoogleDateTimeForEvent(String startDate, String endDate) {
        return Arrays.asList(
                new DateTime(startDate),
                new DateTime(endDate)
        );
    }

    public static List<SyncEventDto> mapAppleVEventsToSyncDtos(List<VEvent> events) {
        ArrayList<SyncEventDto> syncEvents = new ArrayList<>();

        for(VEvent event : events) {
            SyncEventDto syncEventDto = new SyncEventDto();

            syncEventDto.getVEventSummary(event);
            syncEventDto.getVEventDescription(event);
            syncEventDto.getVEventCreated(event);
            syncEventDto.getVEventStart(event);
            syncEventDto.getVEventEnd(event);
            syncEventDto.getVEventLocation(event);
            syncEventDto.getVEventICalUID(event);
            
            // Extract or generate syncId for duplicate prevention
            net.fortuna.ical4j.model.property.XProperty syncIdProperty = 
                (net.fortuna.ical4j.model.property.XProperty) event.getProperties()
                    .getProperty("X-AGENDASYC-SYNCID");
            if (syncIdProperty != null && syncIdProperty.getValue() != null) {
                syncEventDto.syncId = syncIdProperty.getValue();
            } else {
                syncEventDto.syncId = "apple_" + event.getUid().getValue();
            }
            
            syncEventDto.setEventOrigin("apple");

            syncEvents.add(syncEventDto);
        }

        return syncEvents;
    }

    public static List<VEvent> mapSyncEventDtoBackToAppleVEvents(List<SyncEventDto> events) throws ParseException {
        ArrayList<VEvent> appleEvents = new ArrayList<>();

        for (SyncEventDto event : events) {
            VEvent appleEvent = new VEvent();

            appleEvent.getProperties().add(new Uid(event.iCalUID));
//            appleEvent.getProperties().add(new Created(event.created));
            appleEvent.getProperties().add(new DtStart(convertDateToCalDavDate(event.startDateTime)));
            appleEvent.getProperties().add(new DtEnd(convertDateToCalDavDate(event.endDateTime)));
            appleEvent.getProperties().add(new Summary(event.title));
            
            // Add syncId as X-property for duplicate prevention
            if (event.syncId != null) {
                appleEvent.getProperties().add(new XProperty("X-AGENDASYC-SYNCID", event.syncId));
            }

            appleEvents.add(appleEvent);
        }

        return appleEvents;
    }

    /**
     * Converteert een Google EventDateTime naar een iCal4j Date.
     *
     * Houdt rekening met twee gevallen:
     * 1. Tijdgebonden event: EventDateTime.getDateTime() is gevuld
     * 2. All-day event: EventDateTime.getDateTime() is null, getDate() is gevuld
     */
    public static Date convertDateToCalDavDate(EventDateTime syncEventDate) throws ParseException {
        DateTime googleDateTime = syncEventDate.getDateTime();

        if (googleDateTime != null) {
            // Tijdgebonden event — gebruik net.fortuna.ical4j.model.DateTime (met tijd)
            net.fortuna.ical4j.model.DateTime icalDateTime = new net.fortuna.ical4j.model.DateTime();
            icalDateTime.setTime(googleDateTime.getValue());
            return icalDateTime;
        } else {
            // All-day event — Google geeft "yyyy-MM-dd" via getDate()
            // iCal4j verwacht "yyyyMMdd" formaat voor een date-only Date object
            DateTime googleDate = syncEventDate.getDate();
            if (googleDate == null) {
                throw new ParseException("EventDateTime heeft geen dateTime of date waarde", 0);
            }
            // Converteer "2026-04-24" naar "20260424" voor iCal4j
            String rawDate = googleDate.toStringRfc3339().replace("-", "").substring(0, 8);
            return new Date(rawDate);
        }
    }

    /**
     * Extracts the UUID portion from a source-based syncId for verification purposes.
     * Supports formats: "google_<eventId>" and "apple_<uid>"
     * @param syncId The source-based syncId
     * @return The UUID portion after the source prefix, or the original syncId if no prefix found
     */
    public static String extractUuidForVerification(String syncId) {
        if (syncId == null || syncId.isEmpty()) {
            return null;
        }
        
        if (syncId.startsWith("google_")) {
            return syncId.substring(7); // Remove "google_" prefix
        } else if (syncId.startsWith("apple_")) {
            return syncId.substring(6); // Remove "apple_" prefix
        }
        
        return syncId;
    }
}