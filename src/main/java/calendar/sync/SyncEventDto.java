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
 * Deze Dto wordt gebruikt om naar de database te verzenden ter vergelijking
 * Het is afgeleid van de VEvent model van ical4j, wat nodig is om te voldoen aan CALDAV.
 */
public class SyncEventDto extends VEvent {
    public String id;
    public String title;
    public String description;
    public String location;
    public String startDateTime; // conversie naar DateTime maken
    public String endDateTime; // conversie naar DateTime maken
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
}
