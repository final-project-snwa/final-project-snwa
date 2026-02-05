package com.team.snwa.snwabackend.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {


    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다."),
    MAIL_SEND_ERROR(HttpStatus.BAD_REQUEST, "잘못된 요청입니다"),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND,"존재하지 않는 유저입니다." ),
    ARTICLE_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 기사입니다."),
    RESET_TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND,"존재하지 않는 토큰입니다."),
    RESET_TOKEN_ALREADY_USED(HttpStatus.BAD_REQUEST,"이미 사용된 토큰입니다."),
    RESET_TOKEN_EXPIRED(HttpStatus.BAD_REQUEST,"유효기간이 지난 토큰입니다."),
    
    // 인증 관련 에러 코드
    EMAIL_NOT_VERIFIED(HttpStatus.FORBIDDEN, "이메일 인증이 완료되지 않았습니다."),
    INVALID_VERIFICATION_TOKEN(HttpStatus.BAD_REQUEST, "유효하지 않은 인증 토큰입니다."),
    EXPIRED_VERIFICATION_TOKEN(HttpStatus.BAD_REQUEST, "만료된 인증 토큰입니다."),
    ALREADY_VERIFIED_TOKEN(HttpStatus.BAD_REQUEST, "이미 인증된 토큰입니다."),
    USERNAME_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 존재하는 아이디입니다."),
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 존재하는 이메일입니다."),
    NICKNAME_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 존재하는 닉네임입니다."),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "비밀번호가 일치하지 않습니다."),
    USER_INACTIVE(HttpStatus.FORBIDDEN, "비활성화된 사용자입니다."),

    //코인관련 에러 코드
    WALLET_AMOUNT_INVALID(HttpStatus.BAD_REQUEST, "코인 수량은 0보다 커야 합니다."),
    WALLET_INSUFFICIENT_BALANCE(HttpStatus.BAD_REQUEST, "코인 잔액이 부족합니다."),
    WALLET_TRANSACTION_DUPLICATED(HttpStatus.CONFLICT, "이미 처리된 거래입니다."),
    WALLET_EXTERNAL_REF_REQUIRED(HttpStatus.BAD_REQUEST, "외부 참조값(externalRef)이 필요합니다."),

    // 결제(Payment) 관련 에러 코드
    PAYMENT_ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "결제 주문을 찾을 수 없습니다."),
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "결제 정보를 찾을 수 없습니다."),

    PAYMENT_AMOUNT_MISMATCH(HttpStatus.BAD_REQUEST, "결제 금액이 일치하지 않습니다."),
    PAYMENT_ALREADY_PAID(HttpStatus.CONFLICT, "이미 결제가 완료된 주문입니다."),
    PAYMENT_ALREADY_CANCELED(HttpStatus.CONFLICT, "이미 전체 취소된 결제입니다."),

    TOSS_ORDER_ID_MISMATCH(HttpStatus.BAD_REQUEST, "토스 주문 ID가 일치하지 않습니다."),
    TOSS_AMOUNT_MISMATCH(HttpStatus.BAD_REQUEST, "토스 결제 금액이 일치하지 않습니다."),

    PAYMENT_KEY_DUPLICATED(HttpStatus.CONFLICT, "이미 처리된 결제 키입니다."),
    PAYMENT_INCONSISTENT_STATE(HttpStatus.CONFLICT, "결제 상태가 일관되지 않습니다."),

    PAYMENT_ORDER_CANCELED(HttpStatus.BAD_REQUEST, "취소된 주문은 결제 승인할 수 없습니다."),
    PAYMENT_ORDER_EXPIRED(HttpStatus.BAD_REQUEST, "만료된 주문은 결제 승인할 수 없습니다."),
    TOSS_CONFIRM_FAILED(HttpStatus.BAD_GATEWAY, "토스 결제 승인 요청에 실패했습니다."),

    INVALID_CANCEL_AMOUNT(HttpStatus.BAD_REQUEST, "취소 금액이 올바르지 않습니다."),
    CANCEL_AMOUNT_EXCEEDS_REMAINING(HttpStatus.BAD_REQUEST, "취소 금액이 남은 결제 금액을 초과했습니다."),

    TOSS_CANCEL_FAILED(HttpStatus.BAD_GATEWAY, "토스 결제 취소 요청에 실패했습니다."),

    PAYMENT_CHARGE_NOT_FOUND(HttpStatus.NOT_FOUND, "결제 정보를 찾을 수 없습니다."),
    CANNOT_CANCEL_USED_COIN_PAYMENT(HttpStatus.CONFLICT, "이미 사용된 코인이 포함된 결제는 취소할 수 없습니다."),

    POLICY_NOT_FOUND(HttpStatus.NOT_FOUND, "결제 정책을 찾을 수 없습니다."),
    POLICY_INACTIVE(HttpStatus.BAD_REQUEST, "현재 사용 중인 결제 정책이 아닙니다."),

    //알림 관련 에러 코드
    NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 알림입니다."),
    NOTIFICATION_ACCESS_DENIED(HttpStatus.FORBIDDEN, "본인의 알림만 처리할 수 있습니다."),
    NOTIFICATION_SETTING_NOT_FOUND(HttpStatus.NOT_FOUND, "알림 설정이 존재하지 않습니다."),
    NOTIFICATION_DISABLED(HttpStatus.FORBIDDEN, "알림 수신이 비활성화된 사용자입니다.");

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
