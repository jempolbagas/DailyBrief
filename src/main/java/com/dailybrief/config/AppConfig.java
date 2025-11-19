package com.dailybrief.config;

import io.github.cdimascio.dotenv.Dotenv;
import java.net.http.HttpClient;
import java.time.Duration;

public class AppConfig {
    private static AppConfig instance;
    private final Dotenv dotenv;
    private final HttpClient httpClient;

    private AppConfig() {
        // Load .env file (ignore if missing to allow system env vars)
        this.dotenv = Dotenv.configure().ignoreIfMissing().load();

        // PRD 5.1: Timeout Policy - 5 seconds
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    public static synchronized AppConfig getInstance() {
        if (instance == null) {
            instance = new AppConfig();
        }
        return instance;
    }

    public String getApiKey(String keyName) {
        return dotenv.get(keyName);
    }

    public HttpClient getHttpClient() {
        return httpClient;
    }
}