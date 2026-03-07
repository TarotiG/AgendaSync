package calendar.google;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
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
import java.util.Collections;
import java.util.List;

@Service
public class GoogleCalendarService {

    private static final Logger logger = LoggerFactory.getLogger(GoogleCalendarService.class);

    private static final String APPLICATION_NAME = "AgendaSync";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    private static final List<String> SCOPES =
            Collections.singletonList(CalendarScopes.CALENDAR_EVENTS);

    /**
     * Maakt verbinding met Google Calendar via een Service Account.
     *
     * Geen interactieve OAuth flow — werkt headless in Docker/Render.
     * Vereiste: de kalender is gedeeld met het service account email adres.
     */
    public Calendar connectToPlatform() throws IOException, GeneralSecurityException {
        logger.debug("Connecting to Google Calendar via Service Account");

        final NetHttpTransport transport = GoogleNetHttpTransport.newTrustedTransport();

        GoogleCredentials credentials = loadServiceAccountCredentials();

        return new Calendar.Builder(transport, JSON_FACTORY, new HttpCredentialsAdapter(credentials))
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    /**
     * Laadt de Service Account credentials.
     *
     * Probeert eerst het bestand via een omgevingsvariabele (voor Render/Docker),
     * daarna als classpath resource (voor lokale ontwikkeling).
     */
    private GoogleCredentials loadServiceAccountCredentials() throws IOException {
        String credentialsPath = System.getenv("GOOGLE_SERVICE_ACCOUNT");

        if (credentialsPath != null && !credentialsPath.isEmpty()) {
            logger.debug("Loading service account credentials from path: {}", credentialsPath);
            
            try (InputStream in = new FileInputStream(credentialsPath)) {
                return ServiceAccountCredentials.fromStream(in)
                        .createScoped(SCOPES);
            }
        }
    }

    /**
     * Haalt alle aankomende events op uit de primaire Google Calendar.
     */
    public List<Event> retrieveAllCalendarItems(Calendar calendar) throws IOException {
        DateTime now = new DateTime(System.currentTimeMillis());

        Events events = calendar.events().list("primary")
                .setTimeMin(now)
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .execute();

        logger.debug("Retrieved {} events from Google Calendar", events.getItems().size());
        return events.getItems();
    }

    /**
     * Geeft het huidige access token terug voor gebruik in webhook herregistratie.
     * Het token wordt automatisch ververst door de GoogleCredentials implementatie.
     */
    public String getAccessToken() throws IOException {
        GoogleCredentials credentials = loadServiceAccountCredentials();
        credentials.refreshIfExpired();
        return credentials.getAccessToken().getTokenValue();
    }
}


// package calendar.google;

// import com.google.api.client.auth.oauth2.Credential;
// import com.google.auth.oauth2.GoogleCredentials;
// import com.google.auth.oauth2.ServiceAccountCredentials;

// import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
// import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
// import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
// import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
// import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
// import com.google.api.client.http.javanet.NetHttpTransport;
// import com.google.api.client.json.JsonFactory;
// import com.google.api.client.json.gson.GsonFactory;
// import com.google.api.client.util.DateTime;
// import com.google.api.client.util.store.FileDataStoreFactory;
// import com.google.api.services.calendar.Calendar;
// import com.google.api.services.calendar.CalendarScopes;
// import com.google.api.services.calendar.model.Event;
// import com.google.api.services.calendar.model.Events;
// //import jdk.jshell.spi.ExecutionControl;

// import java.io.FileInputStream;
// import java.io.FileNotFoundException;
// import java.io.IOException;
// import java.io.InputStream;
// import java.io.InputStreamReader;
// import java.security.GeneralSecurityException;
// import java.util.Collections;
// import java.util.List;

// import org.springframework.stereotype.Service;

// @Service
// public class GoogleCalendarService {
//     /**
//      * Application name.
//      */
//     private static final String APPLICATION_NAME = "AgendaSync";
//     /**
//      * Global instance of the JSON factory.
//      */
//     private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
//     /**
//      * Directory to store authorization tokens for this application.
//      */
//     private static final String TOKENS_DIRECTORY_PATH = "tokens";

//     /**
//      * Global instance of the scopes required by this quickstart.
//      * If modifying these scopes, delete your previously saved tokens/ folder.
//      */
//     private static final List<String> SCOPES =
//             Collections.singletonList(CalendarScopes.CALENDAR_EVENTS);
//     private static final String CREDENTIALS_FILE_PATH = System.getenv("GOOGLE_SERVICE_ACCOUNT");

//     /**
//      * Creates an authorized Credential object.
//      *
//      * @param HTTP_TRANSPORT The network HTTP Transport.
//      * @return An authorized Credential object.
//      * @throws IOException If the credentials.json file cannot be found.
//      */
//     // private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT)
//     //         throws IOException {
//     //     // Load client secrets.
//     //     InputStream in = GoogleCalendarService.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
//     //     if (in == null) {
//     //         throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
//     //     }
//     //     GoogleClientSecrets clientSecrets =
//     //             GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

//     //     // Build flow and trigger user authorization request.
//     //     GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
//     //             HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
//     //             .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
//     //             .setAccessType("offline")
//     //             .build();
//     //     LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();

//     //     //returns an authorized Credential object.
//     //     return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
//     // }

//     private GoogleCredentials loadServiceAccountCredentials() throws IOException {
//         String credentialsPath = System.getenv("GOOGLE_SERVICE_ACCOUNT_FILE");

//         try (InputStream in = new FileInputStream(credentialsPath)) {
//             return ServiceAccountCredentials.fromStream(in).createScoped(SCOPES);

//         } catch (FileNotFoundException e) {

//             throw new IOException("Google service account credentials file not found at: " + credentialsPath, e);
//         }
//     }


//     public Calendar connectToPlatform() throws IOException, GeneralSecurityException {

//         final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
//         return new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
//                         .setApplicationName(APPLICATION_NAME)
//                         .build();
//     }

//     // Webhook funcionaliteit toevoegen
//     public List<Event> retrieveAllCalendarItems(Calendar calendar) throws IOException  {
//         DateTime now = new DateTime(System.currentTimeMillis());

//         Events events = calendar.events().list("primary")
//                 .setMaxResults(10) // later verwijderen of aanpassen naar meer results
//                 .setTimeMin(now)
//                 .setOrderBy("startTime")
//                 .setSingleEvents(true)
//                 .execute();

//         return events.getItems();
//     }

// //    public void sortCalendarItemsToDate() throws ExecutionControl.NotImplementedException {
// //        throw new ExecutionControl.NotImplementedException("Not implemented yet");
// //    }
// //
// //    public void postCalendarItem() throws ExecutionControl.NotImplementedException {
// //        throw new ExecutionControl.NotImplementedException("Not implemented yet");
// //    }
// //
// //    public void syncCalendarWithEngine() throws ExecutionControl.NotImplementedException {
// //        throw new ExecutionControl.NotImplementedException("Not implemented yet");
// //    }
// //
// //    public void validateSyncToEngine() throws ExecutionControl.NotImplementedException {
// //        throw new ExecutionControl.NotImplementedException("Not implemented yet");
// //    }
// }
