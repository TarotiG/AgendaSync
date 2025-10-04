package calendar.sync;

import calendar.google.enums.Status;
import calendar.google.enums.Visibility;
import calendar.google.models.Attachment;
import calendar.google.models.Attendee;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.EventDateTime;

import java.util.List;

public class SyncEventDto {
    public String id;
    public String title;
    public String description;
    public String location;
    public EventDateTime startDateTime; // conversie naar DateTime maken
    public EventDateTime endDateTime; // conversie naar DateTime maken
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
