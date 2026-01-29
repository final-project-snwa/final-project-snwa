package com.team.snwa.snwabackend.domain.payment.dto;

public record WebhookAckResponse(String status) {
    public static WebhookAckResponse ok() { return new WebhookAckResponse("OK"); }
}
