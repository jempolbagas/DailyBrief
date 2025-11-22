package com.dailybrief.services;

import com.dailybrief.config.AppConfig;
import com.dailybrief.models.NewsResponse;
import com.dailybrief.models.WeatherResponse;
import java.util.concurrent.CompletableFuture;

public class DashboardService {
    private final WeatherService weatherService;
    private final NewsService newsService;
    private final CacheManager cacheManager;

    public DashboardService(WeatherService weatherService, NewsService newsService, CacheManager cacheManager) {
        this.weatherService = weatherService;
        this.newsService = newsService;
        this.cacheManager = cacheManager;
    }

    public DashboardService() {
        this(new WeatherService(AppConfig.getInstance()), new NewsService(), new CacheManager());
    }

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
            WeatherResponse cachedWeather = cacheManager.getWeather(city);
            if (cachedWeather != null) {
                weatherFuture = CompletableFuture.completedFuture(cachedWeather);
            } else {
                weatherFuture = weatherService.getWeatherAsync(city)
                        .thenApply(weather -> {
                            if (weather != null) {
                                cacheManager.saveWeather(city, weather);
                            }
                            return weather;
                        })
                        .exceptionally(this::handleException);
            }
        }

        // Handle News Request
        if (noNews) {
            newsFuture = CompletableFuture.completedFuture(null);
        } else {
            NewsResponse cachedNews = cacheManager.getNews();
            if (cachedNews != null) {
                newsFuture = CompletableFuture.completedFuture(cachedNews);
            } else {
                newsFuture = newsService.getNewsAsync()
                        .thenApply(news -> {
                            if (news != null) {
                                cacheManager.saveNews(news);
                            }
                            return news;
                        })
                        .exceptionally(this::handleException);
            }
        }

        // Wait for both (Parallel Execution)
        CompletableFuture.allOf(weatherFuture, newsFuture).join();

        return new DashboardData(weatherFuture.join(), newsFuture.join());
    }
}
