package com.dailybrief.services;

import com.dailybrief.models.NewsResponse;
import com.dailybrief.models.WeatherResponse;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AsyncOrchestratorTest {

    @Test
    void testParallelExecution() {
        // Mock Services
        WeatherService mockWeather = mock(WeatherService.class);
        NewsService mockNews = mock(NewsService.class);

        // Force 1 second delay
        when(mockWeather.getWeatherAsync(anyString())).thenAnswer(invocation ->
            CompletableFuture.supplyAsync(() -> {
                try { Thread.sleep(1000); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
                return null; // We don't need a real object for timing tests
            })
        );

        when(mockNews.getNewsAsync()).thenAnswer(invocation ->
            CompletableFuture.supplyAsync(() -> {
                try { Thread.sleep(1000); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
                return null; // We don't need a real object for timing tests
            })
        );

        CacheManager mockCache = mock(CacheManager.class);
        DashboardService dashboardService = new DashboardService(mockWeather, mockNews, mockCache);

        long startTime = System.currentTimeMillis();
        dashboardService.getDashboard("TestCity", false, false);
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // Assert Parallelism: If sequential, it would take ~2000ms. Parallel should be ~1000ms.
        // Allowing a larger buffer for overhead (1500ms) to reduce flakiness in CI/CD.
        assertTrue(duration < 1500, "Execution took too long: " + duration + "ms. Expected parallel execution.");
        assertTrue(duration >= 1000, "Execution was suspiciously fast: " + duration + "ms. Expected at least 1000ms delay.");
    }

    @Test
    void testPartialFailure() {
        // Mock Services
        WeatherService mockWeather = mock(WeatherService.class);
        NewsService mockNews = mock(NewsService.class);

        // Weather succeeds
        // Create a dummy weather response using nulls/defaults since we just check for non-null
        WeatherResponse successfulWeather = new WeatherResponse("London", null, null, null);
        when(mockWeather.getWeatherAsync(anyString())).thenReturn(CompletableFuture.completedFuture(successfulWeather));

        // News fails
        when(mockNews.getNewsAsync()).thenReturn(CompletableFuture.failedFuture(new RuntimeException("News API Down")));

        CacheManager mockCache = mock(CacheManager.class);
        DashboardService dashboardService = new DashboardService(mockWeather, mockNews, mockCache);

        // Execute
        DashboardService.DashboardData result = dashboardService.getDashboard("TestCity", false, false);

        // Assertions
        assertNotNull(result.weather(), "Weather should be populated");
        assertNull(result.news(), "News should be null due to failure");
    }
}
