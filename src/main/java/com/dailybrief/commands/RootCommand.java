package com.dailybrief.commands;

import com.dailybrief.services.DashboardService;
import com.dailybrief.ui.ConsoleRenderer;
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
    private final ConsoleRenderer consoleRenderer = new ConsoleRenderer();

    @Override
    public Integer call() {
        long startTime = System.currentTimeMillis();

        System.out.println("Fetching Daily Brief...");
        var data = dashboardService.getDashboard(city, noNews, noWeather);

        System.out.println("\n=========================================");

        if (data.weather() != null) {
            consoleRenderer.renderWeather(data.weather());
        } else {
            if (!noWeather) System.out.println("WEATHER: Unavailable (See errors above)");
        }

        if (data.news() != null) {
            consoleRenderer.renderNews(data.news());
        } else {
            if (!noNews) System.out.println("\nNEWS: Unavailable (See errors above)");
        }

        System.out.println("=========================================");
        System.out.printf("Done in %dms%n", (System.currentTimeMillis() - startTime));

        return 0;
    }
}
