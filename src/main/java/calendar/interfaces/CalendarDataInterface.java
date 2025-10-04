package calendar.interfaces;

import jdk.jshell.spi.ExecutionControl;

import java.io.IOException;
import java.security.GeneralSecurityException;

public interface CalendarDataInterface {

    CalendarDto connectToPlatform() throws IOException, GeneralSecurityException;

    void retrieveCalendarItems(CalendarDto calendarDto);

    void sortCalendarItemsToDate() throws ExecutionControl.NotImplementedException;

    void postCalendarItem() throws ExecutionControl.NotImplementedException;

    void syncCalendarWithEngine() throws ExecutionControl.NotImplementedException;

    void validateSyncToEngine() throws ExecutionControl.NotImplementedException;
}