package com.team.snwa.snwabackend.domain.user.service;

import jakarta.mail.AuthenticationFailedException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    public void sendVerificationEmail(String to, String token) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // 수신자: 회원가입 시 입력한 이메일 주소
            helper.setTo(to);
            helper.setSubject("[SNWA] 이메일 인증을 완료해주세요");

            String verificationUrl = baseUrl + "/verify-email?token=" + token;
            String htmlContent = buildVerificationEmailContent(verificationUrl);

            helper.setText(htmlContent, true);
            mailSender.send(message);
        } catch (AuthenticationFailedException e) {
            log.error("이메일 인증 실패: {}", e.getMessage(), e);
            throw new RuntimeException("이메일 인증 실패: Gmail 계정 인증에 실패했습니다. 앱 비밀번호가 올바른지 확인해주세요.", e);
        } catch (MessagingException e) {
            log.error("이메일 발송 실패: {}", e.getMessage(), e);
            String errorMsg = "이메일 발송 실패: " + e.getMessage();
            if (e.getCause() != null) {
                errorMsg += " (원인: " + e.getCause().getMessage() + ")";
            }
            throw new RuntimeException(errorMsg, e);
        }
    }

    public void sendPasswordResetEmail(String to, String token) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("[SNWA] 비밀번호 재설정");

            String resetUrl = baseUrl + "/reset-password?token=" + token;
            String htmlContent = buildPasswordResetEmailContent(resetUrl);

            helper.setText(htmlContent, true);
            mailSender.send(message);
        } catch (AuthenticationFailedException e) {
            log.error("이메일 인증 실패: {}", e.getMessage(), e);
            throw new RuntimeException("이메일 인증 실패: Gmail 계정 인증에 실패했습니다. 앱 비밀번호가 올바른지 확인해주세요.", e);
        } catch (MessagingException e) {
            log.error("이메일 발송 실패: {}", e.getMessage(), e);
            String errorMsg = "이메일 발송 실패: " + e.getMessage();
            if (e.getCause() != null) {
                errorMsg += " (원인: " + e.getCause().getMessage() + ")";
            }
            throw new RuntimeException(errorMsg, e);
        }
    }

    private String buildVerificationEmailContent(String verificationUrl) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head><meta charset='UTF-8'></head>" +
                "<body style='font-family: Arial, sans-serif; padding: 20px;'>" +
                "<h2>이메일 인증</h2>" +
                "<p>안녕하세요. SNWA 회원가입을 환영합니다.</p>" +
                "<p>아래 버튼을 클릭하여 이메일 인증을 완료해주세요.</p>" +
                "<a href='" + verificationUrl + "' " +
                "style='display: inline-block; padding: 10px 20px; background-color: #007bff; " +
                "color: white; text-decoration: none; border-radius: 5px; margin: 20px 0;'>" +
                "인증 완료</a>" +
                "<p>또는 아래 링크를 복사하여 브라우저에 붙여넣으세요:</p>" +
                "<p>" + verificationUrl + "</p>" +
                "</body></html>";
    }

    private String buildPasswordResetEmailContent(String resetUrl) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head><meta charset='UTF-8'></head>" +
                "<body style='font-family: Arial, sans-serif; padding: 20px;'>" +
                "<h2>비밀번호 재설정</h2>" +
                "<p>안녕하세요. SNWA입니다.</p>" +
                "<p>비밀번호 재설정을 요청하셨습니다. 아래 버튼을 클릭하여 비밀번호를 재설정해주세요.</p>" +
                "<p style='color: #dc2626; font-weight: bold;'>이 링크는 1시간 동안만 유효합니다.</p>" +
                "<a href='" + resetUrl + "' " +
                "style='display: inline-block; padding: 10px 20px; background-color: #dc2626; " +
                "color: white; text-decoration: none; border-radius: 5px; margin: 20px 0;'>" +
                "비밀번호 재설정</a>" +
                "<p>또는 아래 링크를 복사하여 브라우저에 붙여넣으세요:</p>" +
                "<p>" + resetUrl + "</p>" +
                "<p style='color: #666; font-size: 12px; margin-top: 30px;'>만약 비밀번호 재설정을 요청하지 않으셨다면, 이 이메일을 무시하셔도 됩니다.</p>" +
                "</body></html>";
    }
}
