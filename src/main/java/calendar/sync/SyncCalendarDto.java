package calendar.sync;

import calendar.interfaces.CalendarDto;

public class SyncCalendarDto implements CalendarDto {
    private String id;
    private String name;
    private String description;
    private String location;
    private String timeZone;
    private String hostName;         // Voor Apple CalDAV
    private Integer port;            // Voor Apple CalDAV
    private Boolean useSSL;          // Voor Apple CalDAV
    private String principalUrl;     // Voor Apple CalDAV
    private String colorId;          // Voor Google
    private String accessRole;       // Voor Google

}
