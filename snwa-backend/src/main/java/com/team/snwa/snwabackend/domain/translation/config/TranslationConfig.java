package com.team.snwa.snwabackend.domain.translation.config;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
public class TranslationConfig {

    private final DeepLConfig deepLConfig;
    private final WebClientConfig webClientConfig;

    // 확장성을 위해서 만듦
    // 추후 DeepL 말고도 Google Translate / Papago를 추가하게 되면 작성
    /*
    public WebClient getWebClient() {
        return webClientConfig.webClient();
    }
    */

}
