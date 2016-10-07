package com.Diane;

import processing.core.PApplet;
import processing.core.PFont;

import java.util.Calendar;

import static processing.core.PApplet.*;

/**
 * Created by pedro on 05-10-2016.
 */
public class TimeAndDate extends PApplet {
    private Calendar c;

    // Date variables
    private int m_day = 0;
    public int getDay() { return m_day; }
    public void setDay(int day) { m_day = day; }

    private int m_month = 0;
    public int getMonth() { return m_month; }
    public void setMonth(int month) { m_month = month; }

    private int m_year = 0;
    public int getYear() { return m_year; }
    public void setYear(int year) { m_year = year;}

    private String daysOfWeek[] = {"Sunday", "Monday", "Tuesday", "Wednesday",
            "Thursday", "Friday", "Saturday"};
    private String m_dayOfWeek;
    public String getDayOfWeek() { return m_dayOfWeek; }
    public void setDayOfWeek(String dayOfWeek) { m_dayOfWeek = new String(dayOfWeek); }

    // Time variables
    private int m_second = 0;
    public int getSecond() { return m_second; }
    public void setSecond(int second) { m_second = second;}

    private int m_minute = 0;
    public int getMinute() { return m_minute; }
    public void setMinute(int minute) { m_minute = minute;}

    private int m_hour = 0;
    public int getHour() { return m_hour; }
    public void setHour(int hour) { m_hour = hour;}

    public void setTimeAndDate() {
        setSecond(second());
        setMinute(minute());
        setHour(hour());
        setDay(day());
        setMonth(month());
        setYear(year());
        c = Calendar.getInstance();
        setDayOfWeek(daysOfWeek[c.get(Calendar.DAY_OF_WEEK) - 1]);
    }
}
