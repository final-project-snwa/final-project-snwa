package com.team.snwa.snwabackend.domain.translation.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Getter
@Configuration
public class DeepLConfig {

    @Value("${translation.deepl.api.keys}")
    private String rawKeys;

    @Value("${translation.deepl.api.base-url}")
    private String baseUrl;

    public List<String> getApiKeys() {
        return Arrays.stream(rawKeys.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }
}