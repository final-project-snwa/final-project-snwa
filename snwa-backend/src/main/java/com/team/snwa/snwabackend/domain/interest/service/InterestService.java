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

import java.util.*;
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
                                .orElseThrow(() -> new IllegalArgumentException("Target not found"));

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

        /**
         * AI가 분류한 타입 정보를 포함하여 태그를 등록하고 구독자를 조회
         * 
         * @param typedTags Map<키워드, InterestType>
         * @return 구독자 목록
         */
        @Transactional
        public List<User> findSubscribersForTypedTags(Map<String, InterestType> typedTags) {
                // 1. 새로운 태그 자동 등록 (AI 분류 타입 적용)
                registerNewTypedTags(typedTags);

                // 2. 알림 대상 조회
                List<String> tagKeys = new ArrayList<>(typedTags.keySet());
                return userSubscriptionRepository.findSubscribedUsersByTagKeys(tagKeys).stream()
                                .distinct()
                                .collect(Collectors.toList());
        }

        /**
         * 기존 메서드 유지 (하위 호환성)
         */
        @Transactional
        public List<User> findSubscribersForTags(List<String> tags) {
                Map<String, InterestType> typedTags = new HashMap<>();
                for (String tag : tags) {
                        typedTags.put(tag, InterestType.OTHER);
                }
                return findSubscribersForTypedTags(typedTags);
        }

        /**
         * AI가 분류한 타입으로 새로운 태그 등록
         */
        private void registerNewTypedTags(Map<String, InterestType> typedTags) {
                List<String> tagKeys = new ArrayList<>(typedTags.keySet());

                // DB에 이미 존재하는 태그 키 조회
                List<InterestTarget> existingTargets = interestTargetRepository.findByTagKeyIn(tagKeys);

                Set<String> existingTagKeys = existingTargets.stream()
                                .map(InterestTarget::getTagKey)
                                .collect(Collectors.toSet());

                List<InterestTarget> newTargets = typedTags.entrySet().stream()
                                .filter(entry -> !existingTagKeys.contains(entry.getKey()))
                                .map(entry -> InterestTarget.builder()
                                                .type(entry.getValue())
                                                .name(entry.getKey())
                                                .tagKey(entry.getKey())
                                                .build())
                                .collect(Collectors.toList());

                if (!newTargets.isEmpty()) {
                        interestTargetRepository.saveAll(newTargets);
                }
        }
}
