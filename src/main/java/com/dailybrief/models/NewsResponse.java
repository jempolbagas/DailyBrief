package com.dailybrief.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record NewsResponse(
        String status,
        List<Article> articles
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Article(String title, String description, Source source) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Source(String name) {}
}