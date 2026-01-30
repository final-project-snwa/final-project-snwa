package com.team.snwa.snwabackend.domain.interest.service;

import com.team.snwa.snwabackend.domain.interest.dto.response.InterestTargetResponse;
import com.team.snwa.snwabackend.domain.interest.dto.response.SubscriptionResponse;
import com.team.snwa.snwabackend.domain.interest.entity.InterestTarget;
import com.team.snwa.snwabackend.domain.interest.entity.UserSubscription;
import com.team.snwa.snwabackend.domain.interest.repository.InterestTargetRepository;
import com.team.snwa.snwabackend.domain.interest.repository.UserSubscriptionRepository;
import com.team.snwa.snwabackend.domain.user.entity.User;
import com.team.snwa.snwabackend.domain.user.repository.UserRepository;
import com.team.snwa.snwabackend.global.exception.CustomException;
import com.team.snwa.snwabackend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.team.snwa.snwabackend.domain.interest.entity.InterestType;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InterestService {

        private final InterestTargetRepository interestTargetRepository;
        private final UserSubscriptionRepository userSubscriptionRepository;
        private final UserRepository userRepository;

        public List<InterestTargetResponse> searchTargets(String keyword) {
                return interestTargetRepository.findByNameContaining(keyword).stream()
                                .map(InterestTargetResponse::from)
                                .collect(Collectors.toList());
        }

        @Transactional
        public Map<String, Boolean> toggleSubscription(Long userId, Long targetId) {
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

                InterestTarget target = interestTargetRepository.findById(targetId)
                                .orElseThrow(() -> new IllegalArgumentException("Target not found")); // TODO: Define
                                                                                                      // proper
                                                                                                      // ErrorCode

                boolean subscribed = false;
                if (userSubscriptionRepository.existsByUserAndInterestTarget(user, target)) {
                        userSubscriptionRepository.deleteByUserAndInterestTarget(user, target);
                        subscribed = false;
                } else {
                        UserSubscription subscription = UserSubscription.builder()
                                        .user(user)
                                        .interestTarget(target)
                                        .isAlarmOn(true)
                                        .build();
                        userSubscriptionRepository.save(subscription);
                        subscribed = true;
                }

                Map<String, Boolean> result = new HashMap<>();
                result.put("subscribed", subscribed);
                return result;
        }

        public List<SubscriptionResponse> getMySubscriptions(Long userId) {
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

                return userSubscriptionRepository.findByUser(user).stream()
                                .map(SubscriptionResponse::from)
                                .collect(Collectors.toList());
        }

        @Transactional
        public List<User> findSubscribersForTags(List<String> tags) {
                // 1. 새로운 태그 자동 등록 (Auto Registration)
                registerNewTags(tags);

                // 2. 알림 대상 조회 (기존 로직)
                return userSubscriptionRepository.findSubscribedUsersByTagKeys(tags).stream()
                                .distinct()
                                .collect(Collectors.toList());
        }

        private void registerNewTags(List<String> tags) {
                // DB에 이미 존재하는 태그 키 조회
                List<InterestTarget> existingTargets = interestTargetRepository.findByTagKeyIn(tags);

                Set<String> existingTagKeys = existingTargets.stream()
                                .map(InterestTarget::getTagKey)
                                .collect(Collectors.toSet());

                List<InterestTarget> newTargets = tags.stream()
                                .filter(tag -> !existingTagKeys.contains(tag))
                                .distinct()
                                .map(tag -> InterestTarget.builder()
                                                .type(InterestType.ETC)
                                                .name(tag) // 이름도 태그 키와 동일하게 설정 (추후 관리자 수정 가능)
                                                .tagKey(tag)
                                                .build())
                                .collect(Collectors.toList());

                if (!newTargets.isEmpty()) {
                        interestTargetRepository.saveAll(newTargets);
                }
        }
}
