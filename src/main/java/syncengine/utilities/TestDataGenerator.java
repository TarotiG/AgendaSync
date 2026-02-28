package syncengine.utilities;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;

import com.google.api.services.calendar.model.EventDateTime;
import net.fortuna.ical4j.model.component.VEvent;

import syncengine.sync.SyncEventDto;

// import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
// import java.util.Date;
import java.util.List;

public class TestDataGenerator {

    public static DateTime vandaagUtc() {
        return new DateTime(
                ZonedDateTime.now(ZoneOffset.UTC).toInstant().toEpochMilli()
        );
    }

    public static SyncEventDto generateSyncEventDto() {
        SyncEventDto event = new SyncEventDto();

        event.title = "Test Event";
        event.created = event.setCreatedToNow();
        event.startDateTime = new EventDateTime().setDateTime(vandaagUtc());
        event.endDateTime = new EventDateTime().setDateTime(vandaagUtc());
        event.setICalUID();

        return event;
    }

    public static List<SyncEventDto> generateSyncEventDtoList() {
        return new ArrayList<>();
    }

    public static Event generateGoogleEvent() {
        return new Event();
    }

    public static VEvent generateAppleEvent() {
        return new VEvent();
    }
}
