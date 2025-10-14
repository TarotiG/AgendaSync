package calendar.apple.services;

import calendar.apple.modules.ICal4jConfig;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.component.CalendarComponent;
import net.fortuna.ical4j.model.component.VEvent;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.w3c.dom.NodeList;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

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
    public static List<VEvent> mapResponseToAppleVEvent(HttpResponse<String> response) throws IOException,
            ParserException,
            ParserConfigurationException,
            SAXException,
            XPathExpressionException {

        ICal4jConfig.setICal4jParseConfig();

        ArrayList<VEvent> vEvents = new ArrayList<>();

        String xml = response.body();
        Document doc = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder()
                .parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));

        XPath xpath = XPathFactory.newInstance().newXPath();
//        String ics = xpath.evaluate("//*[local-name()='calendar-data']/text()", doc).trim();
        NodeList nodes = (NodeList) xpath.evaluate(
                "//*[local-name()='calendar-data']/text()",
                doc,
                XPathConstants.NODESET
        );

        List<String> icsBlocks = new ArrayList<>();
        for (int i = 0; i < nodes.getLength(); i++) {
            icsBlocks.add(nodes.item(i).getNodeValue().trim());
        }

        CalendarBuilder builder = new CalendarBuilder();

        for(String ics : icsBlocks) {
            Calendar calendar = builder.build(new StringReader(ics));
            CalendarComponent event = calendar.getComponent(VEvent.VEVENT);
            vEvents.add((VEvent) event);
        }

        return vEvents;
    }
}
