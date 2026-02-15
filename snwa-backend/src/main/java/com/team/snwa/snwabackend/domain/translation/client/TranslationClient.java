package com.team.snwa.snwabackend.domain.translation.client;

import com.team.snwa.snwabackend.domain.translation.dto.request.CrawledArticleRequestDto;
import com.team.snwa.snwabackend.domain.translation.dto.response.TranslatedArticleResponseDto;

public interface TranslationClient {
    TranslatedArticleResponseDto translate(CrawledArticleRequestDto request, String targetLang);
}
