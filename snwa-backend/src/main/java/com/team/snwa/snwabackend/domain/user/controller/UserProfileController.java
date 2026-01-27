package com.team.snwa.snwabackend.domain.user.controller;

import com.team.snwa.snwabackend.domain.user.dto.request.UserProfileUpdateRequest;
import com.team.snwa.snwabackend.domain.user.dto.request.UserUpdatePasswordRequestDto;
import com.team.snwa.snwabackend.domain.user.dto.response.UserProfileResponse;
import com.team.snwa.snwabackend.domain.user.service.UserProfileService;
import com.team.snwa.snwabackend.domain.user.entity.User;
import com.team.snwa.snwabackend.domain.user.repository.UserRepository;
import com.team.snwa.snwabackend.global.exception.CustomException;
import com.team.snwa.snwabackend.global.exception.ErrorCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;

@RestController
@RequestMapping("/api/users/profile")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;
    private final UserRepository userRepository;
    private final com.team.snwa.snwabackend.global.service.S3Service s3Service;

    private User getUser(Principal principal) {
        return userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    @GetMapping
    public ResponseEntity<UserProfileResponse> getProfile(Principal principal) {
        User user = getUser(principal);
        return ResponseEntity.ok(userProfileService.getProfile(user.getId()));
    }

    @PostMapping("/presigned-url")
    public ResponseEntity<com.team.snwa.snwabackend.domain.user.dto.response.ProfileImagePresignedUrlResponse> getPresignedUrl(
            Principal principal,
            @Valid @RequestBody com.team.snwa.snwabackend.domain.user.dto.request.ProfileImagePresignedUrlRequest request) {
        User user = getUser(principal);
        String[] result = s3Service.createPresignedUrl(user.getId(), request.contentType(), "users");
        return ResponseEntity
                .ok(new com.team.snwa.snwabackend.domain.user.dto.response.ProfileImagePresignedUrlResponse(result[0],
                        result[1]));
    }

    @PatchMapping
    public ResponseEntity<UserProfileResponse> updateProfile(
            Principal principal,
            @Valid @RequestBody UserProfileUpdateRequest request) {
        User user = getUser(principal);
        return ResponseEntity.ok(userProfileService.updateProfile(user.getId(), request));
    }

    @PatchMapping("/password")
    public ResponseEntity<Void> updatePassword(
            Principal principal,
            @Valid @RequestBody UserUpdatePasswordRequestDto request) {
        User user = getUser(principal);
        userProfileService.updatePassword(user.getId(), request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/withdraw")
    public ResponseEntity<Void> withdrawUser(Principal principal) {
        User user = getUser(principal);
        userProfileService.withdrawUser(user.getId());
        return ResponseEntity.ok().build();
    }
}
