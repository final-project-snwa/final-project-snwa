package com.team.snwa.snwabackend.domain.payment.dto.response;

public record WebhookAckResponse(String status) {
    public static WebhookAckResponse ok() { return new WebhookAckResponse("OK"); }
}
