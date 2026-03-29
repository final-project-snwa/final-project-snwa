package com.team.snwa.snwabackend.domain.translation.service;

import com.team.snwa.snwabackend.domain.article.entity.Article;
import com.team.snwa.snwabackend.domain.article.repository.ArticleRepository;
import com.team.snwa.snwabackend.domain.translation.client.TranslationClient;
import com.team.snwa.snwabackend.domain.translation.dto.request.CrawledArticleRequestDto;
import com.team.snwa.snwabackend.domain.translation.dto.response.TranslatedArticleResponseDto;
import com.team.snwa.snwabackend.domain.translation.entity.ArticleTranslation;
import com.team.snwa.snwabackend.domain.translation.repository.ArticleTranslationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TranslationService {

    private final TranslationClient translationClient;
    private final ArticleRepository articleRepository;
    private final ArticleTranslationRepository articleTranslationRepository;

    /**
     * DB에서 번역본을 조회하고, 없으면 외부 AI API를 호출하여 번역 후 저장합니다.
     */
    @Transactional
    public ArticleTranslation getOrTranslate(Long articleId, String targetLang) {
        return articleTranslationRepository.findByArticleIdAndLanguage(articleId, targetLang)
                .orElseGet(() -> {
                    log.info("기사 번역 요청 시작: articleId={}, lang={}", articleId, targetLang);
                    Article article = articleRepository.findById(articleId)
                            .orElseThrow(() -> new RuntimeException("기사를 찾을 수 없습니다: " + articleId));

                    CrawledArticleRequestDto request = CrawledArticleRequestDto.builder()
                            .title(article.getTitle())
                            .content(article.getContent())
                            .authorName(article.getAuthorName())
                            .publisherName(article.getPublisherName())
                            .originalUrl(article.getOriginalUrl())
                            .build();

                    // 외부 API 호출
                    TranslatedArticleResponseDto response = translationClient.translate(request, targetLang);

                    ArticleTranslation translation = ArticleTranslation.builder()
                            .article(article)
                            .language(targetLang)
                            .translatedTitle(response.getTranslatedTitle())
                            .translatedContent(response.getTranslatedContent())
                            .build();

                    return articleTranslationRepository.save(translation);
                });
    }
}