package com.team.snwa.snwabackend.domain.notification.service;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class SseEmitterService {

    // userId → emitter
    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    private static final long TIMEOUT = 60L * 60 * 1000; // 1시간

    public SseEmitter connect(Long userId) {
        SseEmitter emitter = new SseEmitter(TIMEOUT);
        SseEmitter previous = emitters.put(userId, emitter);
        if (previous != null) {
            try {
                previous.complete();
            } catch (Exception e) {
                log.debug("이전 SSE emitter 종료 실패 userId={}", userId, e);
            }
        }

        emitter.onCompletion(() -> emitters.remove(userId));
        emitter.onTimeout(() -> emitters.remove(userId));
        emitter.onError(e -> emitters.remove(userId));

        try {
            emitter.send(SseEmitter.event()
                    .name("connect")
                    .data("SSE connected"));
        } catch (IOException e) {
            log.error("SSE 연결 초기화 실패 userId={}", userId, e);
        }

        return emitter;
    }

    public void send(Long userId, Object data) {
        SseEmitter emitter = emitters.get(userId);
        if (emitter == null) {
            return; // 오프라인 상태
        }

        try {
            emitter.send(SseEmitter.event()
                    .name("notification")
                    .data(data));
        } catch (IOException e) {
            log.warn("SSE 전송 실패 userId={}", userId);
            emitters.remove(userId);
        }
    }
}
