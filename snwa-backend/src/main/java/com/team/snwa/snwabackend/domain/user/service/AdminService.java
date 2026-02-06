package com.team.snwa.snwabackend.domain.user.service;

import com.team.snwa.snwabackend.domain.article.dto.response.AdminArticleListResponse;
import com.team.snwa.snwabackend.domain.article.entity.Article;
import com.team.snwa.snwabackend.domain.article.repository.ArticleRepository;
import com.team.snwa.snwabackend.domain.payment.dto.response.PaymentHistoryResponse;
import com.team.snwa.snwabackend.domain.payment.service.PaymentService;
import com.team.snwa.snwabackend.domain.user.dto.request.AdminUserUpdateRequest;
import com.team.snwa.snwabackend.domain.user.dto.response.AdminUserResponse;
import com.team.snwa.snwabackend.domain.user.entity.User;
import com.team.snwa.snwabackend.domain.user.entity.enums.UserRole;
import com.team.snwa.snwabackend.domain.user.repository.UserRepository;
import com.team.snwa.snwabackend.global.exception.CustomException;
import com.team.snwa.snwabackend.global.exception.ErrorCode;
import com.team.snwa.snwabackend.global.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminService {
    private final UserRepository userRepository;
    private final ArticleRepository articleRepository;
    private final PaymentService paymentService;
    private final S3Service s3Service;

    /**
     * 관리자 권한 확인
     */
    private void checkAdminRole(User user) {
        if (user.getRole() != UserRole.ADMIN) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }
    }

    /**
     * 전체 회원 조회 (관리자 전용)
     */
    public List<AdminUserResponse> getAllUsers(User adminUser) {
        // 관리자 권한 확인
        checkAdminRole(adminUser);

        // 모든 회원 조회
        List<User> users = userRepository.findAll();

        // DTO로 변환
        return users.stream()
                .map(AdminUserResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 관리자가 특정 사용자의 정보 수정
     */
    @Transactional
    public AdminUserResponse updateUser(User adminUser, Long userId, AdminUserUpdateRequest request) {
        // 관리자 권한 확인
        checkAdminRole(adminUser);

        // 대상 사용자 조회
        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 프로필 정보 업데이트
        String newNickname = request.nickname() != null ? request.nickname().trim() : targetUser.getNickname();
        String newIntro = request.introduction() != null ? request.introduction() : targetUser.getIntroduction();
        String newPhone = request.phoneNumber() != null ? request.phoneNumber() : targetUser.getPhoneNumber();

        // 닉네임 중복 체크 (자기 자신 제외)
        if (request.nickname() != null && !newNickname.equals(targetUser.getNickname())) {
            if (userRepository.existsByNicknameAndIdNot(newNickname, targetUser.getId())) {
                throw new CustomException(ErrorCode.NICKNAME_ALREADY_EXISTS);
            }
        }

        // 프로필 정보 업데이트
        targetUser.updateProfile(newNickname, newIntro, newPhone);

        // 프로필 이미지 업데이트
        if (request.profileImageUrl() != null) {
            String oldImageUrl = targetUser.getProfileImageUrl();
            if (oldImageUrl != null && !oldImageUrl.isBlank() && !oldImageUrl.equals(request.profileImageUrl())) {
                s3Service.deleteObject(oldImageUrl);
            }
            targetUser.updateImageUrl(request.profileImageUrl());
        }

        // 상태 변경 (관리자 전용)
        if (request.status() != null) {
            targetUser.changeStatus(request.status());
        }

        // 이메일 인증 상태 변경 (관리자 전용)
        if (request.emailVerified() != null) {
            targetUser.setEmailVerified(request.emailVerified());
        }

        userRepository.save(targetUser);
        return AdminUserResponse.from(targetUser);
    }

    /**
     * 관리자가 사용자 소프트 삭제 (로그인 불가능하게)
     */
    @Transactional
    public void deleteUser(User adminUser, Long userId) {
        // 관리자 권한 확인
        checkAdminRole(adminUser);

        // 대상 사용자 조회
        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 소프트 삭제 처리 (deletedAt 설정, status를 DELETE로 변경)
        targetUser.softDelete();

        userRepository.save(targetUser);
    }

    /**
     * 전체 글 목록 조회 (관리자 전용) - 삭제되지 않은 글만
     */
    public List<AdminArticleListResponse> getAllArticles(User adminUser) {
        // 관리자 권한 확인
        checkAdminRole(adminUser);

        // 삭제되지 않은 글만 조회 (등록 날짜 내림차순)
        List<Article> articles = articleRepository.findAllByDeletedAtIsNull(
            Sort.by("createdDate").descending()
        );

        // DTO로 변환
        return articles.stream()
                .map(article -> new AdminArticleListResponse(
                    article.getId(),
                    article.getTitle(),
                    article.getUser() != null ? article.getUser().getNickname() :
                        (article.getAuthorName() != null ? article.getAuthorName() : "알 수 없음"),
                    article.getCreatedDate()
                ))
                .collect(Collectors.toList());
    }

    /**
     * 관리자가 글 소프트 삭제 (다른 사람들에게 안 보이게)
     */
    @Transactional
    public void deleteArticle(User adminUser, Long articleId) {
        // 관리자 권한 확인
        checkAdminRole(adminUser);

        // 대상 글 조회
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new CustomException(ErrorCode.ARTICLE_NOT_FOUND));

        // 소프트 삭제 처리
        article.softDelete();

        articleRepository.save(article);
    }

    /**
     * 관리자가 특정 사용자의 결제 내역 조회 (관리자 전용)
     */
    public PaymentHistoryResponse getPaymentHistoryByUserId(User adminUser, Long userId) {
        checkAdminRole(adminUser);
        return paymentService.getHistoryByUser(userId);
    }
}
