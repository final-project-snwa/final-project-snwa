package com.team.snwa.snwabackend.domain.translation.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class WebClientConfig {
    // 현재 DeepL은 WebClient 사용 X, 대신 DeepL에서 지원해주는 SDK 사용.
    // 추후 파파고, 구글 번역 api를 사용할 때 WebClient를 사용하게 되면 그때 작성
}
