package com.dailybrief.services;

import com.dailybrief.models.NewsResponse;
import com.dailybrief.models.WeatherResponse;
import java.util.concurrent.CompletableFuture;

public class DashboardService {
    private final WeatherService weatherService = new WeatherService();
    private final NewsService newsService = new NewsService();

    public record DashboardData(WeatherResponse weather, NewsResponse news) {}

    private <T> T handleException(Throwable e) {
        Throwable cause = e.getCause() != null ? e.getCause() : e;
        if (cause instanceof IllegalStateException) {
            throw new RuntimeException(cause);
        }
        System.err.println("Service Error: " + e.getMessage());
        return null; // Partial failure handling
    }

    // TDD 4.2: Orchestrator Logic
    public DashboardData getDashboard(String city, boolean noNews, boolean noWeather) {
        CompletableFuture<WeatherResponse> weatherFuture;
        CompletableFuture<NewsResponse> newsFuture;

        // Handle Weather Request
        if (noWeather) {
            weatherFuture = CompletableFuture.completedFuture(null);
        } else {
            weatherFuture = weatherService.getWeatherAsync(city)
                    .exceptionally(this::handleException);
        }

        // Handle News Request
        if (noNews) {
            newsFuture = CompletableFuture.completedFuture(null);
        } else {
            newsFuture = newsService.getNewsAsync()
                    .exceptionally(this::handleException);
        }

        // Wait for both (Parallel Execution)
        CompletableFuture.allOf(weatherFuture, newsFuture).join();

        return new DashboardData(weatherFuture.join(), newsFuture.join());
    }
}