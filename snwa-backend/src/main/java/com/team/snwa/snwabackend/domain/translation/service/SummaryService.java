package com.team.snwa.snwabackend.domain.translation.service;

import com.team.snwa.snwabackend.domain.article.entity.Article;
import com.team.snwa.snwabackend.domain.article.repository.ArticleRepository;
import com.team.snwa.snwabackend.domain.translation.dto.response.SummaryResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StreamUtils;

import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SummaryService {

    private final ChatClient.Builder chatClientBuilder;
    private final ArticleRepository articleRepository;
    private final ResourceLoader resourceLoader;    // 파일, url등 추상적으로 불러올 수 있도록 해줌(Spring Framework 기능)

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public SummaryResponseDto summarizeArticle(Long articleId) {
        log.info("기사 요약 시작: articleId={}", articleId);

        // DB에서 Article 조회
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new RuntimeException("기사를 찾을 수 없습니다: " + articleId));

        // 번역된 내용이 없으면 예외 처리
        if (article.getTranslatedContent() == null || article.getTranslatedContent().trim().isEmpty()) {
            throw new RuntimeException("번역된 내용이 없습니다. 먼저 번역을 진행해주세요.");
        }

        // 62번째 줄 메소드 호출
        String promptTemplate = loadPromptTemplate();
        String prompt = promptTemplate.replace("{translatedContent}", article.getTranslatedContent());

        // Gemini로 요약 요청
        ChatClient chatClient = chatClientBuilder.build();
        String summary = chatClient.prompt(prompt)
                .call()
                .content();

        // DB에 요약 저장
        article.setSummary(summary);
        articleRepository.save(article);

        log.info("기사 요약 완료 및 저장: articleId={}", articleId);

        return SummaryResponseDto.builder()
                .summary(summary)
                .translatedContent(article.getTranslatedContent())
                .build();
    }

    private String loadPromptTemplate() {
        try {
            Resource resource = resourceLoader.getResource("classpath:prompts/summary-prompt.txt");
            return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("프롬프트 템플릿 로드 실패: {}", e.getMessage(), e);
            // 기본 프롬프트 반환
            return "다음 기사 내용을 3줄로 요약해주세요.\n\n기사 내용:\n{translatedContent}";
        }
    }
}