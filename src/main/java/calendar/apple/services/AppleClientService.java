package calendar.apple.services;

import calendar.apple.modules.ICal4jConfig;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.component.CalendarComponent;
import net.fortuna.ical4j.model.component.VEvent;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.w3c.dom.NodeList;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;



public class AppleClientService {

    public static class HttpPropFind extends HttpEntityEnclosingRequestBase {
        public static final String METHOD_NAME = "PROPFIND";

        public HttpPropFind(final URI uri) {
            setURI(uri);
        }

        @Override
        public String getMethod() {
            return METHOD_NAME;
        }
    }

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

    public static HttpUriRequest createPropFindUriRequest(URI uri, String body, String auth) {
        HttpPropFind request = new HttpPropFind(uri);

        request.setEntity(new StringEntity(body, StandardCharsets.UTF_8));
        request.addHeader("Authorization", "Basic " + auth);
        request.addHeader("Content-Type", "application/xml; charset=UTF-8");
        request.addHeader("Depth", "1");

        return request;
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

    public static void createAndSendPutRequest(Calendar calendar, String uuid, String url, String username, String password) throws IOException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        new CalendarOutputter().output(calendar, bout);
        String icsBody = bout.toString(StandardCharsets.UTF_8);

        String eventUrl = url + "/" + uuid + ".ics";

        BasicCredentialsProvider provider = new BasicCredentialsProvider();
        provider.setCredentials(
                new AuthScope(null, -1),
                new UsernamePasswordCredentials(username, password)
        );

        try (CloseableHttpClient client = HttpClients.custom()
                .setDefaultCredentialsProvider(provider)
                .build()) {

            HttpPut put = new HttpPut(eventUrl);
            put.setHeader("Content-Type", "text/calendar; charset=utf-8");
            put.setHeader("If-None-Match", "*");  // ensures creation, not overwrite
            put.setHeader("Depth", "0");
            put.setHeader("User-Agent", "iCal/9.0");
            put.setEntity(new StringEntity(icsBody, "UTF-8"));

            var response = client.execute(put);
            int status = response.getStatusLine().getStatusCode();

            if (status >= 200 && status < 300) {
                System.out.println("CalDAV event created: " + eventUrl);
            } else {
                throw new RuntimeException("CalDAV PUT failed: " + status);
            }
        }
    }

    public static HttpResponse<String> sendRequest(HttpRequest request) throws IOException, InterruptedException {
        return client.send(request, HttpResponse.BodyHandlers.ofString());

    }

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
