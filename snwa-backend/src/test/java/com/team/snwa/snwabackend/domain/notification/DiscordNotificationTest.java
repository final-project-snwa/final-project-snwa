package com.team.snwa.snwabackend.domain.notification;

import com.team.snwa.snwabackend.domain.notification.service.DiscordNotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;


@SpringBootTest
@TestPropertySource(properties = {
    "toss.secret-key=test-toss-secret-key",
    "spring.mail.host=localhost",
    "spring.mail.port=2525",
    "spring.mail.username=test-user",
    "spring.mail.password=test-password",
    "spring.mail.properties.mail.smtp.auth=true",
    "spring.mail.properties.mail.smtp.starttls.enable=true"
})
public class DiscordNotificationTest {

    @Autowired
    private DiscordNotificationService discordNotificationService;

    @Test
    public void testSendNotification() {
        String webhookUrl = "https://discord.com/api/webhooks/1473895295447339132/ZmrffBXZhUGwwI0AD40TICrVH-zC87DQleYsvVBy4GqMzWyFFtHv0BN0wtX4zMGALYMU";
        String message = "테스트 알림입니다. 관심 기사가 발생했습니다!";
        
        discordNotificationService.sendNotification(webhookUrl, message);

        // 비동기 처리 대기
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
