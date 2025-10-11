package calendar.apple.services;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class AppleClientService {
    public static HttpClient client = HttpClient.newHttpClient();

    public static HttpRequest createPropFindRequest(URI uri, String body, String auth) {

        return HttpRequest.newBuilder()
                .uri(uri)
                .method("PROPFIND", HttpRequest.BodyPublishers.ofString(body))
                .header("Authorization", "Basic " + auth)
                .header("Content-Type", "application/xml; charset=UTF-8")
                .header("Depth", "1")
                .build();
    }

    public static HttpRequest createReportRequest(URI uri, String body, String auth) {

        return HttpRequest.newBuilder()
                .uri(uri)
                .method("REPORT", HttpRequest.BodyPublishers.ofString(body))
                .header("Authorization", "Basic " + auth)
                .header("Content-Type", "application/xml; charset=UTF-8")
                .header("Depth", "1")
                .build();
    }

    public static HttpResponse<String> sendRequest(HttpRequest request) throws IOException, InterruptedException {
        return client.send(request, HttpResponse.BodyHandlers.ofString());

    }

    // VERDER UITWERKEN
    public void mapResponseToAppleVEvent(HttpResponse<String> response) throws IOException, ParserException {
        String ics = "..."; // haal dit uit <c:calendar-data>
        CalendarBuilder builder = new CalendarBuilder();
        Calendar calendar = builder.build(new StringReader(ics));

        calendar.getComponents("VEVENT").forEach(event -> {
            System.out.println("Event: " + event.getProperty("SUMMARY"));
            System.out.println("Start: " + event.getProperty("DTSTART"));
        });
    }
}
