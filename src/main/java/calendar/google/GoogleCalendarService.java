package calendar.google;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
//import jdk.jshell.spi.ExecutionControl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class GoogleCalendarService {
    /**
     * Application name.
     */
    private static final String APPLICATION_NAME = "AgendaSync";
    /**
     * Global instance of the JSON factory.
     */
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    /**
     * Directory to store authorization tokens for this application.
     */
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES =
            Collections.singletonList(CalendarScopes.CALENDAR_EVENTS);
    private static final String CREDENTIALS_FILE_PATH = "agendasync-474013-82a844cdf0f6.json";

    /**
     * Creates an authorized Credential object.
     *
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT)
            throws IOException {
        // Load client secrets.
        InputStream in = GoogleCalendarService.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets =
                GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();

        //returns an authorized Credential object.
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    public Calendar connectToPlatform() throws IOException, GeneralSecurityException {

        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        return new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                        .setApplicationName(APPLICATION_NAME)
                        .build();
    }

    /**
     * Haalt alle toekomstige events op (initiële sync of Apple->Google richting).
     * Geen syncToken — haalt alles op vanaf nu tot +1 jaar.
     */
    public List<Event> retrieveAllCalendarItems(Calendar calendar) throws IOException {
        DateTime now = new DateTime(System.currentTimeMillis());
        DateTime oneYearFromNow = new DateTime(System.currentTimeMillis() + 365L * 24 * 60 * 60 * 1000);

        List<Event> allEvents = new ArrayList<>();
        String pageToken = null;

        do {
            Events events = calendar.events().list("primary")
                    .setTimeMin(now)
                    .setTimeMax(oneYearFromNow)
                    .setOrderBy("startTime")
                    .setSingleEvents(true)
                    .setPageToken(pageToken)
                    .execute();

            if (events.getItems() != null) {
                allEvents.addAll(events.getItems());
            }
            pageToken = events.getNextPageToken();
        } while (pageToken != null);

        return allEvents;
    }

    /**
     * Haalt alleen gewijzigde events op sinds de laatste sync via Google's syncToken mechanisme.
     * Geeft null terug als het syncToken verlopen is (dan moet een volledige sync gedaan worden).
     *
     * @param calendar  Google Calendar client
     * @param syncToken het syncToken van de vorige sync (opgeslagen door SyncStateTracker)
     * @return lijst van gewijzigde/nieuwe events, of null als syncToken verlopen is
     */
    public List<Event> retrieveChangedCalendarItems(Calendar calendar, String syncToken) throws IOException {
        try {
            List<Event> changedEvents = new ArrayList<>();
            String pageToken = null;

            do {
                Events events = calendar.events().list("primary")
                        .setSyncToken(syncToken)
                        .setSingleEvents(true)
                        .setPageToken(pageToken)
                        .execute();

                if (events.getItems() != null) {
                    changedEvents.addAll(events.getItems());
                }
                pageToken = events.getNextPageToken();

                // Sla het nieuwe syncToken op als we op de laatste pagina zijn
                if (pageToken == null && events.getNextSyncToken() != null) {
                    this.latestSyncToken = events.getNextSyncToken();
                }
            } while (pageToken != null);

            return changedEvents;

        } catch (com.google.api.client.googleapis.json.GoogleJsonResponseException e) {
            if (e.getStatusCode() == 410) {
                // 410 Gone: syncToken is verlopen — volledige sync nodig
                return null;
            }
            throw e;
        }
    }

    /**
     * Voert een initiële volledige sync uit en slaat het syncToken op voor toekomstig gebruik.
     * Moet aangeroepen worden als er nog geen syncToken is of als het verlopen is.
     */
    public List<Event> retrieveAllItemsAndStoreSyncToken(Calendar calendar) throws IOException {
        DateTime now = new DateTime(System.currentTimeMillis());
        DateTime oneYearFromNow = new DateTime(System.currentTimeMillis() + 365L * 24 * 60 * 60 * 1000);

        List<Event> allEvents = new ArrayList<>();
        String pageToken = null;

        do {
            Events events = calendar.events().list("primary")
                    .setTimeMin(now)
                    .setTimeMax(oneYearFromNow)
                    .setOrderBy("startTime")
                    .setSingleEvents(true)
                    .setPageToken(pageToken)
                    .execute();

            if (events.getItems() != null) {
                allEvents.addAll(events.getItems());
            }
            pageToken = events.getNextPageToken();

            if (pageToken == null && events.getNextSyncToken() != null) {
                this.latestSyncToken = events.getNextSyncToken();
            }
        } while (pageToken != null);

        return allEvents;
    }

    /** Geeft het meest recente syncToken terug na een sync aanroep. */
    public String getLatestSyncToken() {
        return latestSyncToken;
    }

    private String latestSyncToken = null;

//    public void sortCalendarItemsToDate() throws ExecutionControl.NotImplementedException {
//        throw new ExecutionControl.NotImplementedException("Not implemented yet");
//    }
//
//    public void postCalendarItem() throws ExecutionControl.NotImplementedException {
//        throw new ExecutionControl.NotImplementedException("Not implemented yet");
//    }
//
//    public void syncCalendarWithEngine() throws ExecutionControl.NotImplementedException {
//        throw new ExecutionControl.NotImplementedException("Not implemented yet");
//    }
//
//    public void validateSyncToEngine() throws ExecutionControl.NotImplementedException {
//        throw new ExecutionControl.NotImplementedException("Not implemented yet");
//    }
}