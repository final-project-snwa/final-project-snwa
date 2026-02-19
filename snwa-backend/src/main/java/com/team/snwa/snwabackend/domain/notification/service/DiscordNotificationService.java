package com.team.snwa.snwabackend.domain.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.Map;

/**
 * 디스코드 웹후크를 사용하여 알림 메시지를 전송하는 서비스
 * WebClient를 사용하여 비동기적으로 POST 요청을 보냄
 *
 * @author 허준형
 * @DateOfCreated 2026-02-19
 * @DateOfEdit 2026-02-19
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DiscordNotificationService {

    private final WebClient.Builder webClientBuilder;

    /**
     * 디스코드 웹후크로 메시지 전송 (비동기 처리)
     *
     * @param webhookUrl 디스코드 웹후크 URL
     * @param message    전송할 메시지 내용
     * @author 허준형
     * @DateOfCreated 2026-02-19
     * @DateOfEdit 2026-02-19
     */
    @Async
    public void sendNotification(String webhookUrl, String message) {
        if (webhookUrl == null || webhookUrl.isBlank()) {
            return;
        }

        try {
            Map<String, String> body = new HashMap<>();
            body.put("content", message);

            webClientBuilder.build()
                    .post()
                    .uri(webhookUrl)
                    .bodyValue(body)
                    .retrieve()
                    .toBodilessEntity()
                    .subscribe(
                            response -> log.info("디스코드 알림 전송 성공"),
                            error -> log.error("디스코드 알림 전송 실패: {}", error.getMessage())
                    );

        } catch (Exception e) {
            log.error("디스코드 알림 전송 중 오류 발생", e);
        }
    }
}
