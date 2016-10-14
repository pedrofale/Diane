package com.Diane;

import com.google.api.client.googleapis.apache.GoogleApacheHttpTransport;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PImage;

import java.io.IOException;
import java.util.List;

/**
 * Created by pedro on 05-10-2016.
 */
public class BedroomAssistant extends PApplet {

    private TimeAndDate timeAndDate = new TimeAndDate();
    private Bus bus = new Bus();
    private Weather weather = new Weather();
    private GoogleCalendar classes = new GoogleCalendar();
    private GoogleCalendar tests = new GoogleCalendar();

    private boolean endOfDay = false;

    // Text font
    private PFont font;
    private int textColor = 255;

    // Time and date font sizes
    private int dayFontSize = 32;
    private int timeFontSize = 52;
    private int dateFontSize = 52;


    // Weather icons
    private PImage Sunny;
    private PImage Partlysunny;
    private PImage Mostlycloudy;
    private PImage Partlycloudy;
    private PImage Cloudy;
    private PImage Clear;
    private PImage Lightrain;
    private PImage Rain;
    private PImage Mist;


    public static void main(String[] args) {
        PApplet.main("com.Diane.BedroomAssistant");
    }

    // Call size() inside settings(). Other than that, use setup() and draw() as usual
    public void settings() {
        size(1024,768);
    }

    public void setup() {
        fill(120, 50, 240);
        background(0);
        font = createFont("Helvetica", 72);
        try {
            String mailAuth = "/home/pedro/pthebot/auth.txt";
            bus.sendMail(mailAuth, "sms@carris.pt", "C 07413", "Shut up Cesar!");
            bus.readEmail(mailAuth);
            weather.getWeather();
            classes.loadEvents("Classes", 5);
            tests.loadEvents("Tests and Exams", 5);
        } catch (IOException ex) {
            System.err.print(ex);
        }
        loadWeatherIcons();
    }

    public void draw() {
        background(0);
        fill(0);
        stroke(255);
        timeAndDate.setTimeAndDate();
        drawTimeAndDate(timeAndDate);
        drawBusTimetable(bus, 5);
        drawWeather(weather);
        if (hour() == 18) {
            endOfDay = true;
            try {
                classes.loadEvents("Classes", 5);
            } catch (IOException ex) {
                System.err.print(ex);
            }
        }
        if (hour() > 24 && hour() < 18)
            endOfDay = false;
        drawUpcomingEvents(classes, 20, 300, 130);
        drawUpcomingEvents(tests, 500, 300, 290);
    }

    public void drawTimeAndDate(TimeAndDate timeAndDate) {
        textFont(font, dayFontSize);
        fill(textColor);
        text(timeAndDate.getDayOfWeek(), 20, 70);

        textFont(font, timeFontSize);
        fill(textColor);

        int hour = timeAndDate.getHour();
        int minute = timeAndDate.getMinute();
        int second = timeAndDate.getSecond();
        String hourStr = "" + hour;
        String minuteStr = "" + minute;
        String secondStr = "" + second;
        if (hour < 10) hourStr = "0" + hour;
        if (minute < 10) minuteStr = "0" + minute;
        if (second < 10) secondStr = "0" + second;

        String timeString = hourStr + ":" + minuteStr + ":" + secondStr;
        text(timeString, 20, 120);

        textFont(font, dateFontSize);
        fill(textColor);

        int day = timeAndDate.getDay();
        int month = timeAndDate.getMonth();
        int year = timeAndDate.getYear();
        String dateString = day + "/" + month + "/" + year;
        text(dateString, 20, 170);
    }

    public void drawBusTimetable(Bus bus, int numBuses) {
        textFont(font, 22);
        fill(textColor);
        text("Last request: " + bus.getlastRequestTime(), 150, height - 20);
        if (bus.getNoBuses()) {
            text("No buses available.", 150, 370);
        } else {
            if (numBuses == 0)
                numBuses = bus.getNumLines();
            for (int i = 0; i < numBuses; i++) {
                for (int j = 0; j < 4; j++) {
                    text(bus.getBusInfo(i, j) + " ", 150 + 200*j, height - 100 + 22*i);
                }
            }
        }
    }

    public void drawWeather(Weather weather) {
        // Current
        textFont(font, 32);
        fill(textColor);
        text(weather.getCurrentTemp() + "º", width - 290, 170);

        // Maximum
        textFont(font, 22);
        fill(textColor);
        text("Max " + weather.getMaxTemp() + "º", width - 270, 70);

        // Minimum
        textFont(font, 22);
        fill(textColor);
        text("Min " + weather.getMinTemp() + "º", width - 170, 70);

        // Sky state
        textFont(font, 18);
        fill(textColor);
        putWeatherIcon(weather);
    }

    public void loadWeatherIcons() {
        Sunny = loadImage("Sunnyicon.jpg");
        Partlysunny= loadImage("Partlysunnyicon.jpg");
        Mostlycloudy = loadImage("Mostlycloudyicon.jpg");
        Partlycloudy= loadImage("Partlycloudyicon.jpg");
        Cloudy = loadImage("Cloudyicon.jpg");
        Clear = loadImage("Clearicon.jpg");
        Lightrain = loadImage("Lightrainicon.jpg");
        Rain = loadImage("Rainicon.jpg");
        Mist = loadImage("Misticon.jpg");
    }

    public void putWeatherIcon(Weather weather) {
        if (weather.getState().contains("limpo")) {
            image(Sunny, width-200, 100);
            text("Sunny",width-170,180);
        } else if (weather.getState().contains("pouco nublado")) {
            image(Partlycloudy, width-200, 100);
            text("Partly Cloudy",width-170,180);
        } else if (weather.getState().contains("muito nublado")) {
            image(Mostlycloudy, width-200, 100);
            text("Mostly Cloudy",width-170,180);
        } else if (weather.getState().contains("chuva") || weather.getState().contains("Aguaceiros")) {
            image(Rain, width-200, 100);
            text("Rain",width-170,180);
        } else if (weather.getState().contains("Aguaceiros fracos")) {
            image(Lightrain, width-200, 100);
            text("Light Rain",width-170,180);
        }
    }

    public void drawUpcomingEvents(GoogleCalendar calendar, int width, int height, int dateSpace) {
        Events events = calendar.getEvents();

        String title = "";
        if (events.getSummary().contains("Classes"))
            if (endOfDay && hour() < 24)
                title = "Tomorrow's classes";
            else
                title = "Today's classes";
        else
            title = "Important dates";

        textFont(font, 22);
        fill(textColor);
        List<Event> items = events.getItems();
        int i = 0;
        if (items.size() == 0) {
            String noEvents = "";
            if (title.contains("Tomorrow"))
                noEvents = "No classes tomorrow!";
            else if (title.contains("Today"))
                noEvents = "No classes today!";
            textFont(font, 26);
            text(noEvents, width, height - 50);
        } else {
            text(title, width, height - 50);
            for (Event event : items) {
                DateTime start = event.getStart().getDateTime();
                if (start == null) {
                    start = event.getStart().getDate();
                }
                textFont(font, 22);
                fill(textColor);
                text(event.getSummary(), width, height + 28*i);
                textFont(font, 18);
                fill(textColor);
                text(calendar.parseDateTime(start), width + dateSpace, height + 28*i);
                i++;
            }
        }
    }
}
