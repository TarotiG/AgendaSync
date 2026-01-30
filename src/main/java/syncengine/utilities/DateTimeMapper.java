package syncengine.utilities;

import java.time.*;
import java.time.format.*;
import com.google.api.client.util.DateTime;

public class DateTimeMapper {

    public static DateTime convertICalDateTimeToGoogleDateTime(String iCalDateTime, String timeZoneId) {
        ZonedDateTime zdt;

        if (iCalDateTime.contains("T")) {
            DateTimeFormatter fmt;

            if (iCalDateTime.endsWith("Z")) {
                // UTC tijd
                fmt = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmssX");
                zdt = ZonedDateTime.parse(iCalDateTime, fmt);

            } else {
                // waarschijnlijk all-day
                fmt = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");
                LocalDateTime ldt = LocalDateTime.parse(iCalDateTime, fmt);
                zdt = ldt.atZone(ZoneId.of(timeZoneId));
            }

        } else {
            LocalDate ld = LocalDate.parse(iCalDateTime, DateTimeFormatter.ofPattern("yyyyMMdd"));
            zdt = ld.atStartOfDay(ZoneId.of("Europe/Amsterdam"));

        }

        return new DateTime(zdt.toInstant().toEpochMilli());
    }
}
