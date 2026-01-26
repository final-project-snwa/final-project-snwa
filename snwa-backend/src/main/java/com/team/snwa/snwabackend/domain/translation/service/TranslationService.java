package com.team.snwa.snwabackend.domain.translation.service;

import com.team.snwa.snwabackend.domain.article.entity.Article;
import com.team.snwa.snwabackend.domain.article.repository.ArticleRepository;
import com.team.snwa.snwabackend.domain.translation.client.TranslationClient;
import com.team.snwa.snwabackend.domain.translation.dto.request.CrawledArticleRequestDto;
import com.team.snwa.snwabackend.domain.translation.dto.response.TranslatedArticleResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TranslationService {

    private final TranslationClient translationClient;
    private final ArticleRepository articleRepository;

    @Transactional
    public TranslatedArticleResponseDto translateArticle(Long articleId) {
        log.info("기사 번역 시작: articleId={}", articleId);

        // DB에서 Article 조회
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new RuntimeException("기사를 찾을 수 없습니다: " + articleId));

        CrawledArticleRequestDto request = CrawledArticleRequestDto.builder()
                .title(article.getTitle())
                .content(article.getContent())
                .authorName(article.getAuthorName())
                .publisherName(article.getPublisherName())
                .originalUrl(article.getOriginalUrl())
                .build();

        TranslatedArticleResponseDto response = translationClient.translate(request);

        // DB저장
        article.setTranslatedTitle(response.getTranslatedTitle());
        article.setTranslatedContent(response.getTranslatedContent());
        articleRepository.save(article);

        log.info("기사 번역 완료 및 저장: articleId={}", articleId);
        return response;
    }
}
