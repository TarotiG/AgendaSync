public interface CalendarDataService {

    void connectToPlatform();

    void retrieveCalendarItems();

    void sortCalendarItemsToDate();

    void postCalendarItem();

    void syncCalendar();

    void validateSyncToEngine();
}