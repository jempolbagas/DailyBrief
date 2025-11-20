package com.dailybrief.services;

import com.dailybrief.config.AppConfig;
import com.dailybrief.models.WeatherResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WeatherServiceTest {

    @Mock
    private AppConfig appConfig;
    @Mock
    private HttpClient httpClient;
    @Mock
    private HttpResponse<String> httpResponse;

    private WeatherService weatherService;

    @BeforeEach
    void setUp() {
        weatherService = new WeatherService(appConfig);
    }

    @Test
    void testGetWeatherAsync_Success() throws Exception {
        // Arrange
        String jsonResponse = """
            {
              "coord": { "lon": -0.1257, "lat": 51.5085 },
              "weather": [
                {
                  "id": 804,
                  "main": "Clouds",
                  "description": "overcast clouds",
                  "icon": "04n"
                }
              ],
              "base": "stations",
              "main": {
                "temp": 280.32,
                "feels_like": 278.86,
                "temp_min": 279.26,
                "temp_max": 281.48,
                "pressure": 1022,
                "humidity": 81
              },
              "visibility": 10000,
              "wind": { "speed": 2.06, "deg": 210 },
              "clouds": { "all": 100 },
              "dt": 1699996422,
              "sys": {
                "type": 2,
                "id": 2075535,
                "country": "GB",
                "sunrise": 1699946477,
                "sunset": 1699978892
              },
              "timezone": 0,
              "id": 2643743,
              "name": "London",
              "cod": 200
            }
            """;

        when(appConfig.getApiKey("OPENWEATHER_API_KEY")).thenReturn("dummy_api_key");
        when(appConfig.getHttpClient()).thenReturn(httpClient);
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn(jsonResponse);

        CompletableFuture<HttpResponse<String>> futureResponse = CompletableFuture.completedFuture(httpResponse);
        when(httpClient.sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(futureResponse);

        // Act
        CompletableFuture<WeatherResponse> resultFuture = weatherService.getWeatherAsync("London");
        WeatherResponse response = resultFuture.join();

        // Assert
        assertNotNull(response);
        assertEquals("London", response.name());
        assertEquals(280.32, response.main().temp());
        assertFalse(response.weather().isEmpty());
        assertEquals("overcast clouds", response.weather().get(0).description());
        assertEquals("GB", response.sys().country());
    }
}
