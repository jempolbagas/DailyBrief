package com.dailybrief.services;

import com.dailybrief.config.AppConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UrlBuilderTest {

    @Mock
    private AppConfig appConfig;
    @Mock
    private HttpClient httpClient;
    @Mock
    private HttpResponse<String> httpResponse;

    @Captor
    private ArgumentCaptor<HttpRequest> requestCaptor;

    private WeatherService weatherService;

    @BeforeEach
    void setUp() {
        weatherService = new WeatherService(appConfig);
    }

    @Test
    void testCityNameWithSpacesIsEncoded() {
        // Arrange
        String city = "New York";
        String apiKey = "dummy_api_key";
        String expectedEncodedCity = "New%20York";

        when(appConfig.getApiKey("OPENWEATHER_API_KEY")).thenReturn(apiKey);
        when(appConfig.getHttpClient()).thenReturn(httpClient);
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn("{}"); // Empty JSON is enough as we fail on 404/etc but catch parse errors later,
                                                   // actually the service throws if parsing fails.
                                                   // Ideally we provide minimal valid JSON or just don't care about result join exception for this test
                                                   // Let's provide minimal valid JSON to avoid unnecessary exceptions in logs or flow.
        String minimalJson = "{}";

        CompletableFuture<HttpResponse<String>> futureResponse = CompletableFuture.completedFuture(httpResponse);
        when(httpClient.sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(futureResponse);

        // Act
        try {
            weatherService.getWeatherAsync(city).join();
        } catch (Exception e) {
            // We expect parsing failure for "{}" or we can just ignore the result.
            // The goal is to verify the request was sent with correct URL.
        }

        // Assert
        verify(httpClient).sendAsync(requestCaptor.capture(), any());
        HttpRequest capturedRequest = requestCaptor.getValue();
        URI uri = capturedRequest.uri();

        // Check that the URI contains the encoded city name
        assertTrue(uri.toString().contains("q=" + expectedEncodedCity),
                   "URI should contain encoded city name: " + expectedEncodedCity + ", but was: " + uri);
    }
}
