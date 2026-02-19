package com.team.snwa.snwabackend.domain.user.service;

import com.team.snwa.snwabackend.domain.article.entity.enums.CategoryName;
import com.team.snwa.snwabackend.domain.article.repository.ClickLogRepository;
import com.team.snwa.snwabackend.domain.user.dto.request.UserProfileUpdateRequest;
import com.team.snwa.snwabackend.domain.user.dto.request.UserUpdatePasswordRequestDto;
import com.team.snwa.snwabackend.domain.user.dto.response.CategoryClickCountDto;
import com.team.snwa.snwabackend.domain.user.dto.response.UserProfileResponse;
import com.team.snwa.snwabackend.domain.user.entity.User;
import com.team.snwa.snwabackend.domain.user.repository.UserRepository;
import com.team.snwa.snwabackend.global.exception.CustomException;
import com.team.snwa.snwabackend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserProfileService {
    private final UserRepository userRepository;
    private final ClickLogRepository clickLogRepository;
    private final com.team.snwa.snwabackend.global.service.S3Service s3Service;

    public UserProfileResponse getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        List<CategoryClickCountDto> categoryClickCounts = getCategoryClickCounts(userId);
        return UserProfileResponse.from(user, categoryClickCounts);
    }
    /** 유저별 카테고리별 클릭 횟수 */
    private List<CategoryClickCountDto> getCategoryClickCounts(Long userId) {
        return clickLogRepository.countByUserIdGroupByCategory(userId).stream()
                .map(row -> new CategoryClickCountDto(
                        (CategoryName) row[0],
                        ((Number) row[1]).longValue()))
                .collect(Collectors.toList());
    }

    /**
     * 사용자 프로필 정보를 수정함
     * 닉네임, 소개유, 전화번호, 프로필 이미지, 디스코드 웹후크 URL을 업데이트함
     *
     * @param userId  수정할 사용자의 ID
     * @param request 수정할 프로필 정보 (닉네임, 소개글, 전화번호, 이미지 URL, 웹후크 URL)
     * @return 수정된 사용자 프로필 정보
     * @author 허준형
     * @DateOfCreated 2026-01-26
     * @DateOfEdit 2026-02-19
     */
    @Transactional
    public UserProfileResponse updateProfile(Long userId, UserProfileUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        String newNickname = request.nickname() == null ? null : request.nickname().trim();
        String newIntro = request.introduction();
        String newPhone = request.phoneNumber();

        if (newNickname != null && !newNickname.isBlank()) {
            if (userRepository.existsByNicknameAndIdNot(newNickname, user.getId())) {
                throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
            }
        }

        user.updateProfile(newNickname, newIntro, newPhone);


        if (request.profileImageUrl() != null) {
            // 기존 이미지가 있고, 새 이미지와 다르다면 S3에서 삭제
            String oldImageUrl = user.getProfileImageUrl();
            if (oldImageUrl != null && !oldImageUrl.isBlank() && !oldImageUrl.equals(request.profileImageUrl())) {
                s3Service.deleteObject(oldImageUrl);
            }
            user.updateImageUrl(request.profileImageUrl());
        }

        // 디스코드 웹후크 업데이트
        if (request.discordWebhookUrl() != null) {
            user.updateDiscordWebhook(request.discordWebhookUrl());
        }

        return UserProfileResponse.from(user, List.of());
    }

    @Transactional
    public void updatePassword(Long userId, UserUpdatePasswordRequestDto request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 로그인 파트 작성 후 PasswordEncoder를 사용한 기존 비밀번호 확인 로직 추가 필요

        user.changePassword(request.getNewPassword());
    }

    @Transactional
    public void withdrawUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        user.softDelete();
    }
}