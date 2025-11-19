package com.dailybrief.services;

import com.dailybrief.config.AppConfig;
import com.dailybrief.models.NewsResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class NewsService {
    private static final String API_URL = "https://newsapi.org/v2/top-headlines";
    private final AppConfig config = AppConfig.getInstance();
    private final ObjectMapper mapper = new ObjectMapper();

    public CompletableFuture<NewsResponse> getNewsAsync() {
        String apiKey = config.getApiKey("NEWS_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            return CompletableFuture.failedFuture(new IllegalStateException("NEWS_API_KEY is missing in .env"));
        }

        String uri = String.format("%s?country=us&pageSize=5&apiKey=%s", API_URL, apiKey);

        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(uri)).GET().build();

        return config.getHttpClient().sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    // CRITICAL FIX: Check for HTTP errors
                    if (response.statusCode() != 200) {
                        throw new RuntimeException("News API Error (Status " + response.statusCode() + "): " + response.body());
                    }
                    return response.body();
                })
                .thenApply(json -> {
                    try {
                        return mapper.readValue(json, NewsResponse.class);
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to parse News JSON", e);
                    }
                });
    }
}