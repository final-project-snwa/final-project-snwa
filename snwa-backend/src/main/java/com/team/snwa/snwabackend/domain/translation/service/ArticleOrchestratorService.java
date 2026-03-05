package com.team.snwa.snwabackend.domain.translation.service;

import com.team.snwa.snwabackend.domain.article.entity.Article;
import com.team.snwa.snwabackend.domain.article.repository.ArticleRepository;
import com.team.snwa.snwabackend.domain.translation.dto.response.TranslatedArticleResponseDto;
import com.team.snwa.snwabackend.domain.translation.entity.ArticleTranslation;
import com.team.snwa.snwabackend.domain.translation.entity.TranslationAccessLog;
import com.team.snwa.snwabackend.domain.translation.repository.TranslationAccessLogRepository;
import com.team.snwa.snwabackend.domain.user.entity.User;
import com.team.snwa.snwabackend.domain.user.entity.enums.UserRole;
import com.team.snwa.snwabackend.domain.user.repository.UserRepository;
import com.team.snwa.snwabackend.domain.wallet.service.WalletTransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ArticleOrchestratorService {

    private final TranslationService translationService;
    private final SummaryService summaryService;
    private final KeywordExtractionService keywordExtractionService;

    // 권한 체크 및 접근 로그, 결제를 위한 의존성
    private final TranslationAccessLogRepository translationAccessLogRepository;
    private final WalletTransactionService walletTransactionService;
    private final UserRepository userRepository;
    private final ArticleRepository articleRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public TranslatedArticleResponseDto getTranslation(Long userId, Long articleId, String language) {
        String targetLang = (language == null || language.isBlank()) ? "KO" : language.toUpperCase();

        // 1. 코인 결제 및 권한 체크
        checkAccessAndSpendCoin(userId, articleId, targetLang);

        return processPipeline(articleId, targetLang);
    }

    @Deprecated
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public TranslatedArticleResponseDto translateArticle(Long articleId) {
        // 스케줄러 등에서 userId 없이 호출용 (기본 KO 번역)
        return processPipeline(articleId, "KO");
    }

    private TranslatedArticleResponseDto processPipeline(Long articleId, String targetLang) {
        // 2. 번역 (필요시 내부에서 DB 확인 후 API 번역 및 저장)
        ArticleTranslation translation = translationService.getOrTranslate(articleId, targetLang);

        // 3. 요약 (필요시 내부에서 DB 확인 후 API 요약 및 저장)
        String summary = summaryService.summarizeIfNeeded(translation, targetLang);

        // 4. 키워드 추출 (필요시 내부에서 DB 확인 후 API 추출 및 저장)
        List<String> tags = keywordExtractionService.extractKeywordsIfNeeded(translation, targetLang);

        // 5. 조립 후 반환
        return TranslatedArticleResponseDto.builder()
                .title(translation.getArticle().getTitle())
                .content(translation.getArticle().getContent())
                .translatedTitle(translation.getTranslatedTitle())
                .translatedContent(translation.getTranslatedContent())
                .summary(summary)
                .tags(tags)
                .authorName(translation.getArticle().getAuthorName())
                .publisherName(translation.getArticle().getPublisherName())
                .originalUrl(translation.getArticle().getOriginalUrl())
                .build();
    }

    private void checkAccessAndSpendCoin(Long userId, Long articleId, String targetLang) {
        if (userId == null)
            return;

        boolean hasAccess = translationAccessLogRepository.existsByUserIdAndArticleIdAndLanguage(userId, articleId,
                targetLang);
        if (hasAccess)
            return;

        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        if (user.getRole() != UserRole.ADMIN) {
            Article article = articleRepository.findById(articleId)
                    .orElseThrow(() -> new RuntimeException("기사를 찾을 수 없습니다."));

            // 코인 차감 (1코인)
            String externalRef = "TRANS_" + articleId + "_" + targetLang + "_" + System.currentTimeMillis();
            walletTransactionService.spend(user, 1L, externalRef);

            // 접근 로그 저장
            TranslationAccessLog logInfo = TranslationAccessLog.builder()
                    .user(user)
                    .article(article)
                    .language(targetLang)
                    .build();
            translationAccessLogRepository.save(logInfo);
        }
    }
}
