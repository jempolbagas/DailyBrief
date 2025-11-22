package com.dailybrief.services;

import com.dailybrief.models.NewsResponse;
import com.dailybrief.models.WeatherResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CacheIntegrationTest {

    @Mock
    private WeatherService weatherService;
    @Mock
    private NewsService newsService;
    @Mock
    private CacheManager cacheManager;

    private DashboardService dashboardService;

    @BeforeEach
    void setUp() {
        dashboardService = new DashboardService(weatherService, newsService, cacheManager);
    }

    @Test
    void testGetDashboard_UsesCache_WhenCacheIsValid() {
        // Arrange
        String city = "London";
        WeatherResponse cachedWeather = new WeatherResponse("London", null, null, null);
        NewsResponse cachedNews = new NewsResponse("ok", Collections.emptyList());

        when(cacheManager.getWeather(city)).thenReturn(cachedWeather);
        when(cacheManager.getNews()).thenReturn(cachedNews);

        // Act
        DashboardService.DashboardData data = dashboardService.getDashboard(city, false, false);

        // Assert
        assertNotNull(data.weather());
        assertNotNull(data.news());
        assertEquals("London", data.weather().name());
        assertEquals("ok", data.news().status());

        // Verify services were NOT called
        verify(weatherService, never()).getWeatherAsync(anyString());
        verify(newsService, never()).getNewsAsync();
    }

    @Test
    void testGetDashboard_CallsService_WhenCacheIsMissing() {
        // Arrange
        String city = "Paris";
        WeatherResponse apiWeather = new WeatherResponse("Paris", null, null, null);
        NewsResponse apiNews = new NewsResponse("ok", Collections.emptyList());

        when(cacheManager.getWeather(city)).thenReturn(null);
        when(cacheManager.getNews()).thenReturn(null);

        when(weatherService.getWeatherAsync(city)).thenReturn(CompletableFuture.completedFuture(apiWeather));
        when(newsService.getNewsAsync()).thenReturn(CompletableFuture.completedFuture(apiNews));

        // Act
        DashboardService.DashboardData data = dashboardService.getDashboard(city, false, false);

        // Assert
        assertEquals("Paris", data.weather().name());
        assertEquals("ok", data.news().status());

        // Verify services WERE called
        verify(weatherService, times(1)).getWeatherAsync(city);
        verify(newsService, times(1)).getNewsAsync();

        // Verify cache was updated
        verify(cacheManager, times(1)).saveWeather(city, apiWeather);
        verify(cacheManager, times(1)).saveNews(apiNews);
    }
}
