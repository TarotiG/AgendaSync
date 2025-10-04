package Calendar.Mappers;

import Calendar.Sync.SyncCalendarDto;
import com.google.api.services.calendar.Calendar;

/*
Mapper class om Google Calendar object en Apple Calendar object te matchen
naar een CalendarDto-type object
 */
public class CalendarMapper {

    public static SyncCalendarDto mapGoogleCalendarToSyncDto(Calendar calendar) {
        return new SyncCalendarDto();
    }

     public static SyncCalendarDto mapAppleCalendarToSyncDto() {
        return new SyncCalendarDto();
    }
}
