package com.dailybrief.commands;

import com.dailybrief.services.DashboardService;
import com.dailybrief.models.WeatherResponse;
import com.dailybrief.models.NewsResponse;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import java.util.concurrent.Callable;

@Command(name = "dailybrief", mixinStandardHelpOptions = true, version = "1.0",
        description = "A lightweight terminal dashboard.")
public class RootCommand implements Callable<Integer> {

    @Option(names = {"-c", "--city"}, description = "City for weather (default: London)")
    private String city = "London";

    @Option(names = {"--no-news"}, description = "Disable news fetch")
    private boolean noNews = false;

    @Option(names = {"--no-weather"}, description = "Disable weather fetch")
    private boolean noWeather = false;

    private final DashboardService dashboardService = new DashboardService();

    @Override
    public Integer call() {
        long startTime = System.currentTimeMillis();

        System.out.println("Fetching Daily Brief...");
        var data = dashboardService.getDashboard(city, noNews, noWeather);

        System.out.println("\n=========================================");

        // Safe Weather Rendering
        if (data.weather() != null) {
            WeatherResponse w = data.weather();
            // Safe access to nested records using a helper or direct checks
            String country = (w.sys() != null) ? w.sys().country() : "N/A";
            String desc = (w.weather() != null && !w.weather().isEmpty())
                    ? w.weather().get(0).description()
                    : "Unknown";
            double temp = (w.main() != null) ? w.main().temp() : 0.0;

            System.out.printf("WEATHER: %s, %s%n", w.name(), country);
            System.out.printf("Temp: %.1f°C | Condition: %s%n", temp, desc);
        } else {
            if (!noWeather) System.out.println("WEATHER: Unavailable (See errors above)");
        }

        // Safe News Rendering
        if (data.news() != null && data.news().articles() != null) {
            System.out.println("\nTOP HEADLINES (US):");
            for (NewsResponse.Article article : data.news().articles()) {
                String sourceName = (article.source() != null) ? article.source().name() : "Unknown Source";
                System.out.printf("- %s [%s]%n", article.title(), sourceName);
            }
        } else {
            if (!noNews) System.out.println("\nNEWS: Unavailable (See errors above)");
        }

        System.out.println("=========================================");
        System.out.printf("Done in %dms%n", (System.currentTimeMillis() - startTime));

        return 0;
    }
}