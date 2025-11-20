package com.dailybrief.services;

import com.dailybrief.models.NewsResponse;
import com.dailybrief.models.WeatherResponse;
import java.util.concurrent.CompletableFuture;

public class DashboardService {
    private final WeatherService weatherService;
    private final NewsService newsService;

    public DashboardService(WeatherService weatherService, NewsService newsService) {
        this.weatherService = weatherService;
        this.newsService = newsService;
    }

    public DashboardService() {
        this(new WeatherService(), new NewsService());
    }

    public record DashboardData(WeatherResponse weather, NewsResponse news) {}

    // TDD 4.2: Orchestrator Logic
    public DashboardData getDashboard(String city, boolean noNews, boolean noWeather) {
        CompletableFuture<WeatherResponse> weatherFuture;
        CompletableFuture<NewsResponse> newsFuture;

        // Handle Weather Request
        if (noWeather) {
            weatherFuture = CompletableFuture.completedFuture(null);
        } else {
            weatherFuture = weatherService.getWeatherAsync(city)
                    .exceptionally(e -> {
                        System.err.println("Weather Service Error: " + e.getMessage());
                        return null; // Partial failure handling
                    });
        }

        // Handle News Request
        if (noNews) {
            newsFuture = CompletableFuture.completedFuture(null);
        } else {
            newsFuture = newsService.getNewsAsync()
                    .exceptionally(e -> {
                        System.err.println("News Service Error: " + e.getMessage());
                        return null; // Partial failure handling
                    });
        }

        // Wait for both (Parallel Execution)
        CompletableFuture.allOf(weatherFuture, newsFuture).join();

        return new DashboardData(weatherFuture.join(), newsFuture.join());
    }
}