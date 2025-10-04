package calendar.google;

import calendar.google.enums.Status;
import calendar.google.enums.Visibility;
import calendar.google.models.Attachment;
import calendar.google.models.Attendee;
import com.google.api.client.util.DateTime;

import java.util.List;

/*
Map ontvangen data vanuit google's calendar webhook naar dit DTO voor verdere verwerking in db en SE

OMBOUWEN NAAR GOOGLE SPECIFIEK
HET IS NU HETZELFDE ALS HET SYNCDTO
 */
public class GoogleEventDto {
    public String id;
    public String title;
    public String description;
    public String location;
    public DateTime startDateTime;
    public DateTime endDateTime;
    public DateTime timeZone;
    public String recurrence;
    public String organizerEmail;
    public List<Attendee> attendees;
    public Visibility visibility;
    public String sequence;
    public String created; // waarschijnlijk DateTime type
    public String updated; // waarschijnlijk DateTime type
    public Status status;
    public String iCalUID;
    public List<Attachment> attachments;
}
