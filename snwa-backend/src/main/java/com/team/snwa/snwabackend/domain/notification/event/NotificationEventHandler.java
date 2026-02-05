package com.team.snwa.snwabackend.domain.notification.event;

import com.team.snwa.snwabackend.domain.article.entity.Article;
import com.team.snwa.snwabackend.domain.article.repository.ArticleRepository;
import com.team.snwa.snwabackend.domain.interest.entity.InterestType;
import com.team.snwa.snwabackend.domain.interest.service.InterestService;
import com.team.snwa.snwabackend.domain.notification.service.NotificationService;
import com.team.snwa.snwabackend.domain.user.entity.User;
import com.team.snwa.snwabackend.global.exception.CustomException;
import com.team.snwa.snwabackend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventHandler {

    private final ArticleRepository articleRepository;
    private final InterestService interestService;
    private final NotificationService notificationService;

    /**
     * 기사 번역 + 요약 + 키워드 추출 완료 후 호출됨
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT) //이벤트 처리가 필요할 때 사용하는 트랜잭(커밋 완료 시 이벤트 실행)
    public void handleArticleReadyForNotification(
            ArticleReadyForNotificationEvent event
    ) {
        log.info("알림 이벤트 수신: articleId={}", event.articleId());

        // 1. Article 조회
        Article article = articleRepository.findById(event.articleId())
                .orElseThrow(() -> {
                    log.error("알림 생성 실패 - 기사 없음: articleId={}", event.articleId());
                    return new CustomException(ErrorCode.ARTICLE_NOT_FOUND);
                });

        // 2. 관심사 기반 구독 유저 조회
        Map<String, InterestType> typedKeywords = event.typedKeywords();
        List<User> interestedUsers =
                interestService.findSubscribersForTypedTags(typedKeywords);

        if (interestedUsers.isEmpty()) {
            log.info("알림 대상 유저 없음: articleId={}", event.articleId());
            return;
        }

        // 3. 알림 메시지 생성 (번역된 제목이 있으면 사용, 없으면 원문 제목 사용)
        String articleTitle =
                (article.getTranslatedTitle() != null && !article.getTranslatedTitle().isBlank())
                        ? article.getTranslatedTitle()
                        : article.getTitle();

        String message = "새로운 관심 기사가 등록되었습니다: " + articleTitle;

        // 4. 유저별 알림 생성
        for (User user : interestedUsers) {
            notificationService.createNotification(user, article, message);
        }

        log.info("알림 생성 완료: articleId={}, count={}",
                article.getId(), interestedUsers.size());
    }
}
