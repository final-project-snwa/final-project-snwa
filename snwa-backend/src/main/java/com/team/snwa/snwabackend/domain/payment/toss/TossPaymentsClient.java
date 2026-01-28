package com.team.snwa.snwabackend.domain.payment.toss;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Component
public class TossPaymentsClient {

    private final RestClient restClient;
    private final String secretKey;

    public TossPaymentsClient(RestClient restClient,
                              @Value("${toss.secret-key}") String secretKey) {
        this.restClient = restClient;
        this.secretKey = secretKey;
    }

    public TossPaymentResponse confirm(String paymentKey, String orderId, Long amount) {
        Map<String, Object> body = Map.of(
                "paymentKey", paymentKey,
                "orderId", orderId,
                "amount", amount
        );

        return restClient.post()
                .uri("https://api.tosspayments.com/v1/payments/confirm")
                .header("Authorization", basicAuth(secretKey))
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(TossPaymentResponse.class);
    }

    public TossCancelResponse cancel(String paymentKey, String reason, Long cancelAmount) {
        Map<String, Object> body = new HashMap<>();
        body.put("cancelReason", reason);
        if (cancelAmount != null) body.put("cancelAmount", cancelAmount);

        return restClient.post()
                .uri("https://api.tosspayments.com/v1/payments/{paymentKey}/cancel", paymentKey)
                .header("Authorization", basicAuth(secretKey))
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(TossCancelResponse.class);
    }

    private String basicAuth(String secretKey) {
        String token = Base64.getEncoder()
                .encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));
        return "Basic " + token;
    }
}