package com.dailybrief.ui;

import com.dailybrief.models.NewsResponse;
import com.dailybrief.models.WeatherResponse;

import java.util.ArrayList;
import java.util.List;

public class ConsoleRenderer {

    private static final int WEATHER_ART_WIDTH = 15;

    public void renderWeather(WeatherResponse w) {
        String country = (w.sys() != null) ? w.sys().country() : "N/A";
        String desc = (w.weather() != null && !w.weather().isEmpty())
                ? w.weather().get(0).description()
                : "Unknown";
        double temp = (w.main() != null) ? w.main().temp() : 0.0;
        String iconCode = (w.weather() != null && !w.weather().isEmpty())
                ? w.weather().get(0).icon()
                : null;

        String artBlock = AsciiArt.getWeatherArt(iconCode);
        String[] artLines = artBlock.split("\n");

        List<String> textLines = new ArrayList<>();
        textLines.add(String.format("WEATHER: %s, %s", w.name(), country));
        textLines.add(String.format("Temp: %.1f°C | Condition: %s", temp, desc));

        int maxLines = Math.max(artLines.length, textLines.size());

        for (int i = 0; i < maxLines; i++) {
            String art = (i < artLines.length) ? artLines[i] : "";
            String text = (i < textLines.size()) ? textLines.get(i) : "";
            // Pad art to WEATHER_ART_WIDTH chars for alignment
            System.out.printf("%-" + WEATHER_ART_WIDTH + "s %s%n", art, text);
        }
    }

    public void renderNews(NewsResponse data) {
        System.out.println("\nTOP HEADLINES (US):");
        for (NewsResponse.Article article : data.articles()) {
            String sourceName = (article.source() != null) ? article.source().name() : "Unknown Source";
            System.out.printf("- %s%s%s [%s%s%s]%n",
                    AnsiColors.BOLD, article.title(), AnsiColors.RESET,
                    AnsiColors.DIM, sourceName, AnsiColors.RESET);
        }
    }
}
