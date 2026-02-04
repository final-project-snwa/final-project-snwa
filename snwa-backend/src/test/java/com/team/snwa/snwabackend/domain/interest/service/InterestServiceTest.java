package com.team.snwa.snwabackend.domain.interest.service;

import com.team.snwa.snwabackend.domain.interest.dto.response.InterestTargetResponse;
import com.team.snwa.snwabackend.domain.interest.dto.response.SubscriptionResponse;
import com.team.snwa.snwabackend.domain.interest.entity.InterestTarget;
import com.team.snwa.snwabackend.domain.interest.entity.InterestType;
import com.team.snwa.snwabackend.domain.interest.entity.UserSubscription;
import com.team.snwa.snwabackend.domain.interest.repository.InterestTargetRepository;
import com.team.snwa.snwabackend.domain.interest.repository.UserSubscriptionRepository;
import com.team.snwa.snwabackend.domain.user.entity.User;
import com.team.snwa.snwabackend.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class InterestServiceTest {

    @InjectMocks
    private InterestService interestService;

    @Mock
    private InterestTargetRepository interestTargetRepository;
    @Mock
    private UserSubscriptionRepository userSubscriptionRepository;
    @Mock
    private UserRepository userRepository;

    @Test
    @DisplayName("Search targets by keyword")
    void searchTargets() {
        // given
        String keyword = "손흥민";
        InterestTarget target = InterestTarget.builder()
                .type(InterestType.PLAYER)
                .name("손흥민")
                .tagKey("손흥민")
                .build();
        org.springframework.test.util.ReflectionTestUtils.setField(target, "id", 1L);
        given(interestTargetRepository.findByNameContaining(keyword)).willReturn(List.of(target));

        // when
        List<InterestTargetResponse> result = interestService.searchTargets(keyword);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("손흥민");
    }

    @Test
    @DisplayName("Subscribe to a new target")
    void subscribe() {
        // given
        Long userId = 1L;
        Long targetId = 1L;
        User user = User.builder().id(userId).build();
        InterestTarget target = InterestTarget.builder().build();
        org.springframework.test.util.ReflectionTestUtils.setField(target, "id", targetId);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(interestTargetRepository.findById(targetId)).willReturn(Optional.of(target));
        given(userSubscriptionRepository.existsByUserAndInterestTarget(user, target)).willReturn(false);

        // when
        Map<String, Boolean> result = interestService.toggleSubscription(userId, targetId);

        // then
        assertThat(result.get("subscribed")).isTrue();
        verify(userSubscriptionRepository).save(any(UserSubscription.class));
    }

    @Test
    @DisplayName("Unsubscribe from an existing target")
    void unsubscribe() {
        // given
        Long userId = 1L;
        Long targetId = 1L;
        User user = User.builder().id(userId).build();
        InterestTarget target = InterestTarget.builder().build();
        org.springframework.test.util.ReflectionTestUtils.setField(target, "id", targetId);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(interestTargetRepository.findById(targetId)).willReturn(Optional.of(target));
        given(userSubscriptionRepository.existsByUserAndInterestTarget(user, target)).willReturn(true);

        // when
        Map<String, Boolean> result = interestService.toggleSubscription(userId, targetId);

        // then
        assertThat(result.get("subscribed")).isFalse();
        verify(userSubscriptionRepository).deleteByUserAndInterestTarget(user, target);
    }

    @Test
    @DisplayName("Get my subscriptions")
    void getMySubscriptions() {
        // given
        Long userId = 1L;
        User user = User.builder().id(userId).build();
        InterestTarget target = InterestTarget.builder()
                .type(InterestType.TEAM)
                .name("토트넘")
                .tagKey("토트넘")
                .build();
        UserSubscription subscription = UserSubscription.builder()
                .user(user)
                .interestTarget(target)
                .isAlarmOn(true)
                .build();

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(userSubscriptionRepository.findByUser(user)).willReturn(List.of(subscription));

        // when
        List<SubscriptionResponse> result = interestService.getMySubscriptions(userId);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTarget().getTagKey()).isEqualTo("토트넘");
    }

    @Test
    @DisplayName("Find subscribers for tags and auto-register new tags")
    void findSubscribersForTags() {
        // given
        List<String> tags = List.of("손흥민", "새로운_태그_메시");
        InterestTarget sonTarget = InterestTarget.builder().tagKey("손흥민").build();
        User user1 = User.builder().id(1L).build();

        // Mocking existing targets
        // given(interestTargetRepository.findAll()).willReturn(List.of(sonTarget));
        // Optimized:
        given(interestTargetRepository.findByTagKeyIn(tags)).willReturn(List.of(sonTarget));

        given(userSubscriptionRepository.findSubscribedUsersByTagKeys(tags)).willReturn(List.of(user1));

        // when
        List<User> result = interestService.findSubscribersForTags(tags);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);

        // Verify that '새로운_태그_메시' was saved
        verify(interestTargetRepository).saveAll(org.mockito.ArgumentMatchers.argThat(iterable -> {
            List<InterestTarget> list = (List<InterestTarget>) iterable;
            return list.size() == 1 &&
                    list.get(0).getTagKey().equals("새로운_태그_메시") &&
                    list.get(0).getType() == InterestType.OTHER;
        }));
    }
}
