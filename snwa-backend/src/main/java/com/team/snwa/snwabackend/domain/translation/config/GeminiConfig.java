package com.team.snwa.snwabackend.domain.translation.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Getter
@Configuration
public class GeminiConfig {

    @Value("${translation.gemini.api.keys}")
    private String rawKeys;

    @Value("${translation.gemini.model:gemini-2.5-flash}")
    private String model;

    @Value("${translation.gemini.temperature:0.2}")
    private float temperature;

    public List<String> getApiKeys() {
        return Arrays.stream(rawKeys.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }
}
