package com.team.snwa.snwabackend.domain.translation.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class DeepLConfig {
    @Value("${translation.deepl.api.key}")
    private String apiKey;

    @Value("${translation.deepl.api.base-url}")
    private String baseUrl;
}
