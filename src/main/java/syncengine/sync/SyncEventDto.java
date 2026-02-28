package syncengine.sync;

import calendar.google.enums.Status;
import calendar.google.enums.Visibility;
import calendar.google.models.Attachment;
import calendar.google.models.Attendee;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.EventDateTime;

import net.fortuna.ical4j.model.component.VEvent;
import syncengine.utilities.DateTimeMapper;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

/**
 * Deze Dto wordt gebruikt om naar de database te verzenden ter vergelijking.
 *
 * Nog toe te voegen functionaliteit:
 * - Genereren van een iCalUID --> een uuid aanmaken is voldoende
 * - iCalUID matchen met apple --> format is uuid@domain.com
 */
public class SyncEventDto {
    public String id;
    public String title;
    public String description;
    public String location;
    public EventDateTime startDateTime;
    public EventDateTime endDateTime;
    public DateTime timeZone;
    public String recurrence;
    public String organizerEmail;
    public List<Attendee> attendees;
    public Visibility visibility;
    public String sequence;
    public DateTime created;
    public DateTime updated;
    public Status status;
    public String iCalUID;
    public List<Attachment> attachments;
    public String syncId; // Unique identifier to track events across calendar syncs and prevent duplicates
    private CalendarType eventOrigin;
    public boolean existsInForeignCalendar; // mogelijk hernoemen


    public void getVEventSummary(VEvent event) {
        this.title = event.getSummary().getValue();
    }

    public void getVEventDescription(VEvent event) {
        this.description = event.getDescription() != null
            ? event.getDescription().getValue()
            : null;
    }

    public void getVEventICalUID(VEvent event) {
        this.iCalUID = event.getUid().getValue();
    }

    public void getVEventCreated(VEvent event) {
        this.created = new DateTime(event.getCreated().getDate());
    }

    public void getVEventStart(VEvent event) {
        String isoStartDate = event.getStartDate().getValue();
        DateTime startDate = DateTimeMapper.convertICalDateTimeToGoogleDateTime(isoStartDate, "Europe/Amsterdam");

        this.startDateTime = new EventDateTime().setDateTime(startDate);
    }

    public void getVEventEnd(VEvent event) {
        String isoEndDate = event.getEndDate().getValue();
        DateTime endDate = DateTimeMapper.convertICalDateTimeToGoogleDateTime(isoEndDate, "Europe/Amsterdam");

        this.endDateTime = new EventDateTime().setDateTime(endDate);
    }

    public void getVEventLocation(VEvent event) {
        this.location = event.getLocation() != null
                ? event.getLocation().getValue()
                : null;
    }

    public void setEventOrigin(String calendar) {
        this.eventOrigin = calendar.equals("google")
                ? CalendarType.GOOGLE
                : CalendarType.APPLE;
    }

    public CalendarType getEventOrigin() {
        return this.eventOrigin;
    }

    public DateTime setCreatedToNow() {
        LocalDate date = LocalDate.now();

        long milliSeconds = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        return new DateTime(milliSeconds);
    }

    public void setICalUID() {
        if(this.iCalUID == null || this.iCalUID.equals("")) {
            this.iCalUID = UUID.randomUUID().toString();
        }
    }
}
