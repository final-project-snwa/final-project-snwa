package com.team.snwa.snwabackend.domain.translation.client;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentConfig;
import com.team.snwa.snwabackend.domain.translation.config.GeminiConfig;
import com.team.snwa.snwabackend.global.exception.CustomException;
import com.team.snwa.snwabackend.global.exception.ErrorCode;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
@RequiredArgsConstructor
public class GeminiClientManager {

    private final GeminiConfig geminiConfig;
    private List<Client> clients;
    private final AtomicInteger currentKeyIndex = new AtomicInteger(0);

    @PostConstruct
    public void init() {
        List<String> keys = geminiConfig.getApiKeys();
        if (keys.isEmpty()) {
            throw new IllegalStateException("Gemini API 키가 설정되지 않았습니다. GEMINI_API_KEYS 환경변수를 확인해주세요.");
        }
        clients = keys.stream()
                .map(key -> Client.builder().apiKey(key).build())
                .toList();
        log.info("Gemini 클라이언트 초기화 완료: {}개의 API 키 등록", clients.size());
    }

    public String generate(String prompt) {
        int startIndex = currentKeyIndex.get();
        for (int i = 0; i < clients.size(); i++) {
            int index = (startIndex + i) % clients.size();
            try {
                String result = doGenerate(clients.get(index), prompt);
                currentKeyIndex.set(index);
                return result;
            } catch (CustomException e) {
                if (e.getErrorCode() == ErrorCode.AI_API_QUOTA_EXCEEDED) {
                    log.warn("Gemini 키 [{}] quota 초과, 다음 키로 전환 ({}/{})", index, i + 1, clients.size());
                    currentKeyIndex.set((index + 1) % clients.size());
                    continue;
                }
                throw e;
            }
        }
        log.error("모든 Gemini API 키 quota 초과");
        throw new CustomException(ErrorCode.AI_API_QUOTA_EXCEEDED);
    }

    private String doGenerate(Client client, String prompt) {
        try {
            GenerateContentConfig config = GenerateContentConfig.builder()
                    .temperature(geminiConfig.getTemperature())
                    .build();
            return client.models.generateContent(geminiConfig.getModel(), prompt, config).text();
        } catch (Exception e) {
            log.error("Gemini API 호출 중 오류 발생: {}", e.getMessage(), e);
            if (e.getMessage() != null &&
                    (e.getMessage().contains("429") || e.getMessage().contains("RESOURCE_EXHAUSTED"))) {
                throw new CustomException(ErrorCode.AI_API_QUOTA_EXCEEDED);
            }
            throw new CustomException(ErrorCode.AI_API_ERROR);
        }
    }
}
