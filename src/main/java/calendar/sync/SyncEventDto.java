package calendar.sync;

import calendar.google.enums.Status;
import calendar.google.enums.Visibility;
import calendar.google.models.Attachment;
import calendar.google.models.Attendee;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.EventDateTime;

import net.fortuna.ical4j.model.component.VEvent;

import java.util.List;

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
    public CalendarType eventOrigin;
    public boolean existsInForeignCalendar; // mogelijk hernoemen


    public void getVEventId(VEvent event) {

    }

    public void getVEventSummary(VEvent event) {
        this.title = event.getSummary().toString();
    }

    public void getVEventDescription(VEvent event) {
        this.description = event.getDescription().toString();
    }

    public void getVEventICalUID(VEvent event) {
        this.iCalUID = event.getUid().toString();
    }

    public void getVEventCreated(VEvent event) {
        this.created = new DateTime(event.getCreated().getDate());
    }

    public void getVEventStart(VEvent event) {
        String isoStartDate = event.getStartDate().toString();
        DateTime startDate = new DateTime(isoStartDate);

        this.startDateTime = new EventDateTime().setDateTime(startDate).setTimeZone("Europe/Amsterdam");
    }

    public void getVEventEnd(VEvent event) {
        String isoEndDate = event.getEndDate().toString();
        DateTime endDate = new DateTime(isoEndDate);

        this.startDateTime = new EventDateTime().setDateTime(endDate).setTimeZone("Europe/Amsterdam");
    }

    public void getVEventLocation(VEvent event) {
        this.location = event.getLocation().toString();
    }

}
