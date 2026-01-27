package com.team.snwa.snwabackend.domain.user.service;

import com.team.snwa.snwabackend.domain.user.dto.request.UserProfileUpdateRequest;
import com.team.snwa.snwabackend.domain.user.dto.request.UserUpdatePasswordRequestDto;
import com.team.snwa.snwabackend.domain.user.dto.response.UserProfileResponse;
import com.team.snwa.snwabackend.domain.user.entity.User;
import com.team.snwa.snwabackend.domain.user.repository.UserRepository;
import com.team.snwa.snwabackend.global.exception.CustomException;
import com.team.snwa.snwabackend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserProfileService {
    private final UserRepository userRepository;
    private final com.team.snwa.snwabackend.global.service.S3Service s3Service;

    public UserProfileResponse getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        return UserProfileResponse.from(user);
    }

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

        return UserProfileResponse.from(user);
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
