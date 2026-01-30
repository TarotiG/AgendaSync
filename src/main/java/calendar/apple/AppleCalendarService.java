package calendar.apple;

import calendar.apple.services.AppleClientService;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

import io.github.cdimascio.dotenv.Dotenv;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Version;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

//import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilderFactory;
import java.util.ArrayList;
import java.util.List;

public class AppleCalendarService {

    public void connectToPlatform() {

    }

    public List<VEvent> retrieveAllCalendarItems() throws Exception {
        Dotenv dotenv = Dotenv.load();

        String appleId = dotenv.get("APPLE_USR");
        String appSpecificPassword = dotenv.get("APPLE_SPEC_PW");
        String caldavUrl = dotenv.get("URL");

        String auth = Base64.getEncoder().encodeToString((appleId + ":" + appSpecificPassword).getBytes(StandardCharsets.UTF_8));

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

    private Calendar createCalendarForSync(VEvent event) {
        Calendar calendar = new Calendar();

        calendar.getProperties().add(new ProdId(""));
        calendar.getProperties().add(Version.VERSION_2_0);
        calendar.getProperties().add(CalScale.GREGORIAN);
        calendar.getComponents().add(event);

        return calendar;
    }

    public void sendIcsToAppleCalendar(VEvent event) throws IOException {
        Dotenv dotenv = Dotenv.load();

        String appleId = dotenv.get("APPLE_USR");
        String appSpecificPassword = dotenv.get("APPLE_SPEC_PW");
        String caldavUrl = dotenv.get("URL");

        Calendar calendar = createCalendarForSync(event);

        AppleClientService.createAndSendPutRequest(calendar, event.getUid().getValue(), caldavUrl, appleId, appSpecificPassword);
    }

//    public List<CalendarInfo> fetchWritableCalendars() throws Exception {
//        Dotenv dotenv = Dotenv.load();
//
//        String appleId = dotenv.get("APPLE_USR");
//        String appSpecificPassword = dotenv.get("APPLE_SPEC_PW");
//        String caldavUrl = dotenv.get("URL");
//        String auth = Base64.getEncoder().encodeToString((appleId + ":" + appSpecificPassword).getBytes(StandardCharsets.UTF_8));
//
//        // XML PROPFIND body
//        String requestBody =
//                """
//                        <?xml version="1.0" encoding="UTF-8"?>
//                        <d:propfind xmlns:d="DAV:" xmlns:cs="http://calendarserver.org/ns/">
//                          <d:prop>
//                            <d:displayname/>
//                            <d:resourcetype/>
//                            <d:current-user-privilege-set/>
//                          </d:prop>
//                        </d:propfind>""";
//
//        // Setup basic auth
//        CredentialsProvider provider = new BasicCredentialsProvider();
//        provider.setCredentials(AuthScope.ANY,
//                new UsernamePasswordCredentials(appleId, appSpecificPassword));
//
//        try (CloseableHttpClient client = HttpClients.custom()
//                .setDefaultCredentialsProvider(provider)
//                .build()) {
//
//            HttpUriRequest request = AppleClientService.createPropFindUriRequest(new URI(caldavUrl), requestBody, auth);
//            HttpResponse response = client.execute(request);
//            String xml = EntityUtils.toString(response.getEntity());
//            System.out.println(xml);
//
//            return parseCalendarResponse(xml);
//        }
//    }
//
//    private static List<CalendarInfo> parseCalendarResponse(String xml) throws Exception {
//        List<CalendarInfo> calendars = new ArrayList<>();
//
//        Document doc = DocumentBuilderFactory.newInstance()
//                .newDocumentBuilder()
//                .parse(new java.io.ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
//
//        NodeList responses = doc.getElementsByTagName("d:response");
//
//        for (int i = 0; i < responses.getLength(); i++) {
//            Element response = (Element) responses.item(i);
//
//            String href = getText(response, "d:href");
//            String displayName = getText(response, "d:displayname");
//
//            boolean writeable = xmlContainsWritePrivilege(response);
//
//            if (writeable) {
//                calendars.add(new CalendarInfo(displayName, href));
//            }
//        }
//
//        return calendars;
//    }
//
//    private static boolean xmlContainsWritePrivilege(Element response) {
//        NodeList privilegeNodes = response.getElementsByTagName("d:privilege");
//        for (int i = 0; i < privilegeNodes.getLength(); i++) {
//            if (privilegeNodes.item(i).getTextContent().contains("write")) {
//                return true;
//            }
//        }
//        return false;
//    }
//
//    private static String getText(Element parent, String tag) {
//        NodeList nl = parent.getElementsByTagName(tag);
//        if (nl.getLength() == 0) return "";
//        return nl.item(0).getTextContent();
//    }
//
//    // Simple calendar info object
//    public static class CalendarInfo {
//        public String name;
//        public String href;
//
//        public CalendarInfo(String name, String href) {
//            this.name = name;
//            this.href = href;
//        }
//
//        public String toString() {
//            return name + " -> " + href;
//        }
//    }
}
