package com.team.snwa.snwabackend.domain.user.service;

import com.team.snwa.snwabackend.domain.user.dto.request.UserProfileUpdateRequest;
import com.team.snwa.snwabackend.domain.user.entity.User;
import com.team.snwa.snwabackend.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@TestPropertySource(properties = {
    "toss.secret-key=test-toss-secret-key",
    "spring.mail.host=localhost",
    "spring.mail.port=2525",
    "spring.mail.username=test-user",
    "spring.mail.password=test-password",
    "spring.mail.properties.mail.smtp.auth=true",
    "spring.mail.properties.mail.smtp.starttls.enable=true"
})
class UserProfileServiceTest {

    @Autowired
    private UserProfileService userProfileService;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("사용자 프로필 업데이트 시 디스코드 웹후크 저장 확인")
    void updateProfile_ShouldUpdateDiscordWebhook() {
        // given
        User user = User.builder()
                .email("test@test.com")
                .password("password")
                .nickname("tester")
                .build();
        userRepository.save(user);

        String webhookUrl = "https://discord.com/api/webhooks/123/abc";
        UserProfileUpdateRequest request = new UserProfileUpdateRequest(
                "tester", "intro", "010-1234-5678", null, webhookUrl
        );

        // when
        userProfileService.updateProfile(user.getId(), request);

        // then
        User updatedUser = userRepository.findById(user.getId()).orElseThrow();
        assertThat(updatedUser.getDiscordWebhookUrl()).isEqualTo(webhookUrl);
    }
}
