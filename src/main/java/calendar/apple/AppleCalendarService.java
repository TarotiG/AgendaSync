package calendar.apple;

import calendar.apple.services.AppleClientService;

import java.io.File;
import java.io.FileWriter;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

import io.github.cdimascio.dotenv.Dotenv;
import net.fortuna.ical4j.model.component.VEvent;

public class AppleCalendarService {

    public void connectToPlatform() {

    }

    public List<VEvent> retrieveAllCalendarItems() throws Exception {
        Dotenv dotenv = Dotenv.load();

        String appleId = dotenv.get("APPLE_USR");
        String appSpecificPassword = dotenv.get("APPLE_SPEC_PW");
        String caldavUrl = dotenv.get("URL");

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

        HttpRequest reportRequest = AppleClientService.createReportRequest(new URI(caldavUrl), body, auth);
        HttpResponse<String> response = AppleClientService.sendRequest(reportRequest);

        return AppleClientService.mapResponseToAppleVEvent(response);
    }

    public static void execute() throws Exception {



//        File file = new File("response.txt");
//        FileWriter fw = new FileWriter(file);
//
//        for(VEvent event : events) {
//            System.out.println(event.getUid());
//            System.out.println(event.getSummary());
//            System.out.println(event.getStartDate());
//            System.out.println(event.getEndDate());
//        }
//
//        fw.write(response.body());
//        fw.close();

//        System.out.println("Response code: " + response.statusCode());
//        System.out.println("Body: " + response.body());
    }
}
