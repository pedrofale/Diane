package com.Diane;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import processing.core.PImage;
import processing.*;

import java.io.IOException;

/**
 * Class to fetch weather information from the IPMA website for Lisbon.
 * Created by pedro on 06-10-2016.
 */
public class Weather {

    private final String currentWeatherURL = "https://www.ipma.pt/pt/html.jsp";
    private final String weatherURL = "https://www.ipma.pt/pt/html.prev.jsp";

    private String currentTemp = "";
    public String getCurrentTemp() { return currentTemp; }

    private String maxTemp = "";
    public String getMaxTemp() { return maxTemp; }

    private String minTemp = "";
    public String getMinTemp() { return minTemp; }

    private String state = "";
    public String getState() { return state; }

    public void getWeather() throws IOException {
        Document doc = Jsoup.connect(weatherURL).get();

        Elements table = doc.select("table.tablelist.block600w");
        for (Element row : table.select("tr")) {
            Elements tds = row.select("td");
            if (tds.size() > 4)
                if (tds.get(0).text().contains("Lisboa")) {
                    maxTemp = tds.get(4).text();
                    minTemp = tds.get(3).text();
                    state = tds.get(2).text();
                    break;
                }
        }

        getCurrentWeather();
    }

    private void getCurrentWeather() throws IOException {
        Document doc = Jsoup.connect(currentWeatherURL).get();

        Elements table = doc.select("table.tablelist.block600w");
        for (Element row : table.select("tr")) {
            Elements tds = row.select("td");
            if (tds.size() > 4)
                if (tds.get(0).text().contains("Lisboa")) {
                    currentTemp = tds.get(2).text();
                    break;
                }
        }
    }
}
