package syncengine.utilities;

import java.time.*;
import java.time.format.*;
import com.google.api.client.util.DateTime;

public class DateTimeMapper {

    /**
     * Converteert een iCal datum/tijd string naar een Google Calendar DateTime.
     *
     * Drie gevallen:
     * 1. "20260424T120000Z"   → UTC tijd met Z suffix
     * 2. "20260424T120000"    → lokale tijd, tijdzone wordt meegegeven via timeZoneId
     * 3. "20260424"           → all-day event, geeft null terug (gebruik isAllDay() om te detecteren)
     */
    public static DateTime convertICalDateTimeToGoogleDateTime(String iCalDateTime, String timeZoneId) {
        ZonedDateTime zdt;

        if (iCalDateTime.contains("T")) {
            DateTimeFormatter fmt;

            if (iCalDateTime.endsWith("Z")) {
                // UTC tijd
                fmt = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmssX");
                zdt = ZonedDateTime.parse(iCalDateTime, fmt);
            } else {
                // Lokale tijd zonder tijdzone — gebruik de meegegeven tijdzone
                fmt = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");
                LocalDateTime ldt = LocalDateTime.parse(iCalDateTime, fmt);
                zdt = ldt.atZone(ZoneId.of(timeZoneId));
            }

            return new DateTime(zdt.toInstant().toEpochMilli());

        } else {
            // All-day event — geef null terug zodat de caller een date-only EventDateTime kan maken
            return null;
        }
    }

    /**
     * Geeft true als de iCal string een all-day event is (geen T component).
     */
    public static boolean isAllDay(String iCalDateTime) {
        return !iCalDateTime.contains("T");
    }

    /**
     * Converteert een iCal all-day datum string naar "yyyy-MM-dd" formaat voor Google Calendar.
     * Google verwacht all-day events als "2026-04-24", niet als een timestamp.
     */
    public static String convertICalDateToGoogleDate(String iCalDateTime) {
        // Input: "20260424" → Output: "2026-04-24"
        LocalDate ld = LocalDate.parse(iCalDateTime, DateTimeFormatter.ofPattern("yyyyMMdd"));
        return ld.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }
}