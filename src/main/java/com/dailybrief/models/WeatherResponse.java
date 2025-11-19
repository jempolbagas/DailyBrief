package com.dailybrief.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record WeatherResponse(
        String name,
        Main main,
        List<Weather> weather,
        Sys sys
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Main(double temp, int humidity) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Weather(String main, String description, String icon) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Sys(String country, long sunrise, long sunset) {}
}