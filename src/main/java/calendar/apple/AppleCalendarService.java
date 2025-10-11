package calendar.apple;

import calendar.apple.services.AppleClientService;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import io.github.cdimascio.dotenv.Dotenv;


/**
 * Agenda's
 * /10464129180/calendars/429F7B63-AAA0-4E90-A40C-11D9B4189A70/ LEEG
 * /10464129180/calendars/9F0BD4B2-E3FF-470B-9842-2B201F4A6518/ LEEG
 * /10464129180/calendars/A1ED255E-740B-4C1B-85AE-6AFDE731931E/ LEEG
 * /10464129180/calendars/C00890B2-AA08-41CD-A4F6-6499E88CF45A/ <===========
 * /10464129180/calendars/F1787D76-B902-4368-9A70-3BDC8AE7D49B/ LEEG
 * /10464129180/calendars/inbox/
 * /10464129180/calendars/notification/
 * /10464129180/calendars/outbox/
 */
public class AppleCalendarService {

    public void connectToPlatform() {

    }

    public void retrieveAllCalendarItems() {

    }

    public static void execute() throws Exception {
        Dotenv dotenv = Dotenv.load();

        String appleId = dotenv.get("APPLE_USR");
        String appSpecificPassword = dotenv.get("APPLE_SPEC_PW");
        String caldavUrl = dotenv.get("URL");

        System.out.println(appleId);
        System.out.println(appSpecificPassword);
        System.out.println(caldavUrl);

        String auth = Base64.getEncoder().encodeToString((appleId + ":" + appSpecificPassword).getBytes(StandardCharsets.UTF_8));

//        String body = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
//                + "<A:propfind xmlns:A=\"DAV:\" xmlns:C=\"urn:ietf:params:xml:ns:caldav\">\n"
//                + "  <A:prop>\n"
//                + "    <C:calendar-home-set/>\n"
//                + "  </A:prop>\n"
//                + "</A:propfind>";

        String body = """
                <?xml version="1.0" encoding="UTF-8"?>
                <c:calendar-query xmlns:c="urn:ietf:params:xml:ns:caldav"
                                  xmlns:d="DAV:">
                  <d:prop>
                    <d:getetag/>
                    <c:calendar-data/>
                  </d:prop>
                  <c:filter>
                    <c:comp-filter name="VCALENDAR">
                      <c:comp-filter name="VEVENT">
                        <c:time-range start="20250401T000000Z" end="20250430T000000Z"/>
                      </c:comp-filter>
                    </c:comp-filter>
                  </c:filter>
                </c:calendar-query>
                """;


//        HttpRequest propFindRequest = HttpClientService.createPropFindRequest(new URI(caldavUrl), body, auth)
//        HttpClientService.sendRequest(propFindRequest);

        HttpRequest reportRequest = AppleClientService.createReportRequest(new URI(caldavUrl), body, auth);
        HttpResponse<String> response = AppleClientService.sendRequest(reportRequest);

        System.out.println("Response code: " + response.statusCode());
        System.out.println("Body: " + response.body());
    }
}
