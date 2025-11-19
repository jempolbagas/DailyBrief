package com.dailybrief.ui;

import com.dailybrief.models.NewsResponse;
import com.dailybrief.models.WeatherResponse;

public class ConsoleRenderer {

    public void renderWeather(WeatherResponse w) {
        if (w != null) {
            String country = (w.sys() != null) ? w.sys().country() : "N/A";
            String desc = (w.weather() != null && !w.weather().isEmpty())
                    ? w.weather().get(0).description()
                    : "Unknown";
            double temp = (w.main() != null) ? w.main().temp() : 0.0;

            System.out.printf("WEATHER: %s, %s%n", w.name(), country);
            System.out.printf("Temp: %.1f°C | Condition: %s%n", temp, desc);
        } else {
            System.out.println("WEATHER: Unavailable (See errors above)");
        }
    }

    public void renderNews(NewsResponse data) {
        if (data != null && data.articles() != null) {
            System.out.println("\nTOP HEADLINES (US):");
            for (NewsResponse.Article article : data.articles()) {
                String sourceName = (article.source() != null) ? article.source().name() : "Unknown Source";
                System.out.printf("- %s [%s]%n", article.title(), sourceName);
            }
        } else {
            System.out.println("\nNEWS: Unavailable (See errors above)");
        }
    }
}
