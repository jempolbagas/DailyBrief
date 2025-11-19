package com.dailybrief.services;

import com.dailybrief.config.AppConfig;
import com.dailybrief.models.WeatherResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

public class WeatherService {
    private static final String API_URL = "https://api.openweathermap.org/data/2.5/weather";
    private final AppConfig config = AppConfig.getInstance();
    private final ObjectMapper mapper = new ObjectMapper();

    public CompletableFuture<WeatherResponse> getWeatherAsync(String city) {
        String apiKey = config.getApiKey("OPENWEATHER_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            // Fail fast if key is missing
            return CompletableFuture.failedFuture(new IllegalStateException("OPENWEATHER_API_KEY is missing in .env"));
        }

        String encodedCity = URLEncoder.encode(city, StandardCharsets.UTF_8);
        String uri = String.format("%s?q=%s&units=metric&appid=%s", API_URL, encodedCity, apiKey);

        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(uri)).GET().build();

        return config.getHttpClient().sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    // CRITICAL FIX: Check for HTTP errors (401 Unauthorized, 404 Not Found, etc.)
                    if (response.statusCode() != 200) {
                        throw new RuntimeException("Weather API Error (Status " + response.statusCode() + "): " + response.body());
                    }
                    return response.body();
                })
                .thenApply(json -> {
                    try {
                        return mapper.readValue(json, WeatherResponse.class);
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to parse Weather JSON", e);
                    }
                });
    }
}