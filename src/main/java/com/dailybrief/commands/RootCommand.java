package com.dailybrief.commands;

import com.dailybrief.services.DashboardService;
import com.dailybrief.services.DashboardService.DashboardData;
import com.dailybrief.ui.AnsiColors;
import com.dailybrief.ui.ConsoleRenderer;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionException;

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
        DashboardData data;
        try {
            data = dashboardService.getDashboard(city, noNews, noWeather);
        } catch (CompletionException e) {
            Throwable cause = e.getCause();
            // Unwrap RuntimeException if present (added in DashboardService)
            if (cause instanceof RuntimeException && cause.getCause() != null) {
                cause = cause.getCause();
            }

            if (cause instanceof IllegalStateException) {
                System.out.println(AnsiColors.RED + "Setup Required: Please create a .env file with your API keys." + AnsiColors.RESET);
                System.exit(1);
            }
            throw e; // Rethrow if it's not the expected config error
        } catch (Exception e) {
            // Should not happen based on current logic, but good safety
            throw new RuntimeException(e);
        }

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
