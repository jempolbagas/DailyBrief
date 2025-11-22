package com.dailybrief.services;

import com.dailybrief.models.NewsResponse;
import com.dailybrief.models.WeatherResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class CacheManager {
    private static final String CACHE_DIR = System.getProperty("user.home") + "/.dailybrief/";
    private static final String NEWS_CACHE_FILE = "news_cache.json";
    private static final String WEATHER_CACHE_FILE = "weather_cache.json";
    private static final long TTL_MS = 10 * 60 * 1000; // 10 minutes

    private final ObjectMapper mapper = new ObjectMapper();

    public record CacheEntry<T>(long timestamp, T data) {
        public boolean isExpired() {
            return System.currentTimeMillis() - timestamp > TTL_MS;
        }
    }

    public CacheManager() {
        ensureCacheDirectory();
    }

    private void ensureCacheDirectory() {
        try {
            Path path = Paths.get(CACHE_DIR);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }
        } catch (Exception e) {
            // Silent fail: caching is an enhancement, not a requirement for basic function
            System.err.println("Warning: Could not create cache directory: " + e.getMessage());
        }
    }

    // --- News Caching ---

    public NewsResponse getNews() {
        try {
            File file = new File(CACHE_DIR + NEWS_CACHE_FILE);
            if (!file.exists()) return null;

            CacheEntry<NewsResponse> entry = mapper.readValue(file, new TypeReference<CacheEntry<NewsResponse>>() {});
            if (entry.isExpired()) {
                return null;
            }
            return entry.data();
        } catch (Exception e) {
            // Corrupt or unreadable cache
            return null;
        }
    }

    public void saveNews(NewsResponse news) {
        try {
            File file = new File(CACHE_DIR + NEWS_CACHE_FILE);
            CacheEntry<NewsResponse> entry = new CacheEntry<>(System.currentTimeMillis(), news);
            mapper.writeValue(file, entry);
        } catch (Exception e) {
            // Ignore write failures
        }
    }

    // --- Weather Caching ---

    public WeatherResponse getWeather(String city) {
        try {
            File file = new File(CACHE_DIR + WEATHER_CACHE_FILE);
            if (!file.exists()) return null;

            // Load the entire map
            TypeReference<Map<String, CacheEntry<WeatherResponse>>> typeRef = new TypeReference<>() {};
            Map<String, CacheEntry<WeatherResponse>> cacheMap = mapper.readValue(file, typeRef);

            String normalizedCity = normalizeCity(city);
            CacheEntry<WeatherResponse> entry = cacheMap.get(normalizedCity);

            if (entry != null && !entry.isExpired()) {
                return entry.data();
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    public void saveWeather(String city, WeatherResponse weather) {
        try {
            File file = new File(CACHE_DIR + WEATHER_CACHE_FILE);
            Map<String, CacheEntry<WeatherResponse>> cacheMap = new HashMap<>();

            // Try to load existing cache first to preserve other cities
            if (file.exists()) {
                try {
                    TypeReference<Map<String, CacheEntry<WeatherResponse>>> typeRef = new TypeReference<>() {};
                    cacheMap = mapper.readValue(file, typeRef);
                } catch (Exception e) {
                    // If corrupt, we just start with a new map
                }
            }

            String normalizedCity = normalizeCity(city);
            cacheMap.put(normalizedCity, new CacheEntry<>(System.currentTimeMillis(), weather));

            mapper.writeValue(file, cacheMap);
        } catch (Exception e) {
            // Ignore write failures
        }
    }

    private String normalizeCity(String city) {
        return city == null ? "" : city.trim().toLowerCase();
    }
}
