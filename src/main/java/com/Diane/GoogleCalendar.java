package com.Diane;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;

/**
 * Created by pedro on 07-10-2016.
 */
public class GoogleCalendar {

    /** Application name. */
    private static final String APPLICATION_NAME =
            "Google Calendar API Java Quickstart";

    /** Directory to store user credentials for this application. */
    private static final java.io.File DATA_STORE_DIR = new java.io.File(
            System.getProperty("user.home"), ".credentials/calendar-java-quickstart");

    /** Global instance of the {@link FileDataStoreFactory}. */
    private static FileDataStoreFactory DATA_STORE_FACTORY;

    /** Global instance of the JSON factory. */
    private static final JsonFactory JSON_FACTORY =
            JacksonFactory.getDefaultInstance();

    /** Global instance of the HTTP transport. */
    private static HttpTransport HTTP_TRANSPORT;

    /** Global instance of the scopes required by this quickstart.
     *
     * If modifying these scopes, delete your previously saved credentials
     * at ~/.credentials/calendar-java-quickstart
     */
    private static final List<String> SCOPES =
            Arrays.asList(CalendarScopes.CALENDAR_READONLY);

    static {
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }

    private Events events;
    public Events getEvents() { return events; }

    /**
     * Creates an authorized Credential object.
     * @return an authorized Credential object.
     * @throws IOException
     */
    public static Credential authorize() throws IOException {
        // Load client secrets.
        InputStream in =
                GoogleCalendar.class.getResourceAsStream("/client_secret.json");
        GoogleClientSecrets clientSecrets =
                GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow =
                new GoogleAuthorizationCodeFlow.Builder(
                        HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                        .setDataStoreFactory(DATA_STORE_FACTORY)
                        .setAccessType("offline")
                        .build();
        Credential credential = new AuthorizationCodeInstalledApp(
                flow, new LocalServerReceiver()).authorize("user");
        System.out.println(
                "Credentials saved to " + DATA_STORE_DIR.getAbsolutePath());
        return credential;
    }

    /**
     * Build and return an authorized Calendar client service.
     * @return an authorized Calendar client service
     * @throws IOException
     */
    public static com.google.api.services.calendar.Calendar
    getCalendarService() throws IOException {
        Credential credential = authorize();
        return new com.google.api.services.calendar.Calendar.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    public void loadEvents(String calendarName, int numMaxEvents) throws IOException {
        com.google.api.services.calendar.Calendar service =
                getCalendarService();

        DateTime now = new DateTime(System.currentTimeMillis());
        DateTime endOfNextDay = new DateTime(System.currentTimeMillis() + 86400000);
        String pageToken = "";
        String calendarID = "";
        do {
            CalendarList calendarList = service.calendarList().list().setPageToken(pageToken).execute();
            List<CalendarListEntry> items = calendarList.getItems();

            for (CalendarListEntry calendarListEntry : items) {
                if (calendarListEntry.getSummary().equalsIgnoreCase(calendarName)) {
                    calendarID = calendarListEntry.getId();
                    System.out.println(calendarListEntry.getId());
                }
            }
            pageToken = calendarList.getNextPageToken();
        } while (pageToken != null);

         events = service.events().list(calendarID)
                .setMaxResults(numMaxEvents)
                .setTimeMin(now)
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .execute();

        if (calendarName.equalsIgnoreCase("Classes"))
            events = service.events().list(calendarID)
                    .setMaxResults(numMaxEvents)
                    .setTimeMin(now)
                    .setTimeMax(endOfNextDay)
                    .setOrderBy("startTime")
                    .setSingleEvents(true)
                    .execute();

        System.out.println(events.getSummary());
        List<Event> items = events.getItems();
        if (items.size() == 0) {
            System.out.println("No upcoming events found.");
        } else {
            System.out.println("Upcoming events");
            for (Event event : items) {
                DateTime start = event.getStart().getDateTime();
                if (start == null) {
                    start = event.getStart().getDate();
                }
                if (calendarName.equalsIgnoreCase("Classes")) renameClasses(event);
                if (calendarName.equalsIgnoreCase("Tests and Exams")) renameTestsAndExams(event);
                System.out.printf("%s (%s)\n", event.getSummary(), start);
            }
        }
    }

    private void renameClasses(Event event) {
        String eventSummary = "";
        if (event.getSummary().contains("Sistemas"))
            eventSummary = "STVR";
        else if (event.getSummary().contains("Teoria"))
            eventSummary = "TCom";
        else if (event.getSummary().contains("Empreendedorismo"))
            eventSummary = "EITT";
        else if (event.getSummary().contains("Aprendizagem"))
            eventSummary = "AA";

        if (event.getSummary().contains("Teórica"))
            eventSummary = eventSummary.concat(" (T)");
        else if (event.getSummary().contains("Problemas"))
            eventSummary = eventSummary.concat(" (P)");
        else if (event.getSummary().contains("Lab"))
            eventSummary = eventSummary.concat(" (L)");

        event.setSummary(eventSummary);
    }

    private void renameTestsAndExams(Event event) {
        String eventSummary = "";
        if (event.getSummary().contains("STVR25179"))
            eventSummary = event.getSummary().replaceAll("STVR25179", "STVR");
        else if (event.getSummary().contains("TComu5179"))
            eventSummary = event.getSummary().replaceAll("TComu5179", "TCom");
        else if (event.getSummary().contains("AAut25179"))
            eventSummary = event.getSummary().replaceAll("AAut25179", "AA");

        eventSummary = eventSummary.replaceAll(" :", ":");
        eventSummary = eventSummary.replaceAll("1º", "1st");
        eventSummary = eventSummary.replaceAll("1ª", "1st");
        eventSummary = eventSummary.replaceAll("2º", "2nd");
        eventSummary = eventSummary.replaceAll("Teste", "Test");
        eventSummary = eventSummary.replaceAll("Exame", "Exam");
        eventSummary = eventSummary.replaceAll("Época", "Epoch");

        if (eventSummary.contains("Aula"))
            eventSummary = eventSummary.replaceAll(" - Aula", "");

        event.setSummary(eventSummary);
    }

    public String parseDateTime(DateTime dateTime) {
        char[] dateTimeArr = dateTime.toString().toCharArray();
        char[] year = new char[4];
        char[] month = new char[2];
        char[] day = new char[2];
        char[] time = new char[5];
        for (int i = 0; i < 10; i++) {
            if (i < 4)
                year[i] = dateTimeArr[i];
            else if (i > 4 && i < 7)
                month[i - 5] = dateTimeArr[i];
            else if (i > 7 && i < 10)
                day[i - 8] = dateTimeArr[i];
        }
        for (int i = 11; i < 16; i++)
            time[i-11] = dateTimeArr[i];

        String timeStr = new String(time);
        String yearStr = new String(year);
        String monthStr = new String(month);
        String dayStr = new String(day);
        String timeAndDate = dayStr + "/" + monthStr + "/" + yearStr + " at " + timeStr;

        return timeAndDate;
    }
}
