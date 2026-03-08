package calendar.google;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class GoogleCalendarService {

    private static final Logger logger = LoggerFactory.getLogger(GoogleCalendarService.class);
    private static final String APPLICATION_NAME = "AgendaSync";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final List<String> SCOPES =
            Collections.singletonList(CalendarScopes.CALENDAR_EVENTS);

    private String latestSyncToken = null;

    // ─── Authenticatie ────────────────────────────────────────────────────────

    /**
     * Laadt service account credentials via:
     * 1. GOOGLE_SERVICE_ACCOUNT_FILE omgevingsvariabele (Render Secret File pad)
     * 2. Classpath (voor lokaal testen — zet het bestand in src/main/resources)
     */
    private GoogleCredentials loadServiceAccountCredentials() throws IOException {
        String credentialsPath = System.getenv("GOOGLE_SERVICE_ACCOUNT_FILE");

        if (credentialsPath != null && !credentialsPath.isEmpty()) {
            logger.debug("Loading service account credentials from path: {}", credentialsPath);
            try (InputStream in = new FileInputStream(credentialsPath)) {
                return ServiceAccountCredentials.fromStream(in).createScoped(SCOPES);
            }
        }

        // Fallback: classpath (lokale ontwikkeling)
        logger.debug("GOOGLE_SERVICE_ACCOUNT_FILE not set — trying classpath");
        InputStream in = getClass().getClassLoader().getResourceAsStream("service-account.json");
        if (in == null) {
            throw new IOException(
                "Geen service account credentials gevonden. " +
                "Stel GOOGLE_SERVICE_ACCOUNT_FILE in als omgevingsvariabele, " +
                "of zet service-account.json in src/main/resources."
            );
        }
        return ServiceAccountCredentials.fromStream(in).createScoped(SCOPES);
    }

    /**
     * Maakt verbinding met Google Calendar via service account.
     * Werkt headless — geen browser of OAuth flow nodig.
     */
    public Calendar connectToPlatform() throws IOException, GeneralSecurityException {
        logger.debug("Connecting to Google Calendar via Service Account");
        GoogleCredentials credentials = loadServiceAccountCredentials();
        HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(credentials);

        return new Calendar.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JSON_FACTORY,
                requestInitializer)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    // ─── Event ophalen ────────────────────────────────────────────────────────

    /**
     * Haalt alle toekomstige events op (voor Apple->Google richting of initiële sync).
     * Gebruikt paginering — geen limiet op aantal events.
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
     * Haalt alleen gewijzigde events op via Google's syncToken (incrementeel).
     * Geeft null terug bij een verlopen token (410) — dan volledige sync uitvoeren.
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

                if (pageToken == null && events.getNextSyncToken() != null) {
                    this.latestSyncToken = events.getNextSyncToken();
                }
            } while (pageToken != null);

            return changedEvents;

        } catch (com.google.api.client.googleapis.json.GoogleJsonResponseException e) {
            if (e.getStatusCode() == 410) {
                // syncToken verlopen — volledige sync nodig
                logger.warn("Google syncToken expired (410) — full sync required");
                return null;
            }
            throw e;
        }
    }

    /**
     * Volledige sync met syncToken opslag — gebruik bij eerste sync of na verlopen token.
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

    public String getLatestSyncToken() {
        return latestSyncToken;
    }
}