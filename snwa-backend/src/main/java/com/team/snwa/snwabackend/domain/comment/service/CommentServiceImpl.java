package com.team.snwa.snwabackend.domain.comment.service;

import com.team.snwa.snwabackend.domain.article.entity.Article;
import com.team.snwa.snwabackend.domain.article.repository.ArticleRepository;
import com.team.snwa.snwabackend.domain.comment.dto.request.CommentRequestDto;
import com.team.snwa.snwabackend.domain.comment.dto.response.CommentResponseDto;
import com.team.snwa.snwabackend.domain.comment.entity.Comment;
import com.team.snwa.snwabackend.domain.comment.repository.CommentRepository;
import com.team.snwa.snwabackend.domain.exp.dto.ExpGrantInfoDto;
import com.team.snwa.snwabackend.domain.exp.entity.UserExp;
import com.team.snwa.snwabackend.domain.exp.repository.UserExpRepository;
import com.team.snwa.snwabackend.domain.exp.service.ExpGrantService;
import com.team.snwa.snwabackend.domain.exp.util.LevelCalculator;
import com.team.snwa.snwabackend.domain.user.entity.User;
import com.team.snwa.snwabackend.global.exception.CustomException;
import com.team.snwa.snwabackend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * 댓글 관련 비즈니스 로직을 처리하는 서비스 구현체
 * 댓글 작성, 조회, 수정, 삭제 및 작성자 검증 기능을 수행함
 *
 * @author 허준형
 * @DateOfCreated 2026-02-03
 * @DateOfEdit 2026-02-03
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final ArticleRepository articleRepository;
    private final ExpGrantService expGrantService;
    private final UserExpRepository userExpRepository;
    private final LevelCalculator levelCalculator;

    /**
     * 특정 기사에 새로운 댓글을 작성함
     *
     * @param articleId 댓글을 작성할 기사의 ID
     * @param requestDto 댓글 내용이 담긴 요청 객체
     * @param user      작성자 정보 (User 엔티티)
     * @return 생성된 댓글의 응답 DTO
     *
     * @DateOfCreated 2026-02-03
     * @DateOfEdit 2026-02-03
     */
    @Transactional
    public CommentResponseDto createComment(Long articleId, CommentRequestDto requestDto, User user) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new CustomException(ErrorCode.ARTICLE_NOT_FOUND));

        Comment comment = Comment.builder()
                .content(requestDto.getContent())
                .article(article)
                .user(user)
                .build();

        commentRepository.save(comment);
        var expGrantInfo = expGrantService.grantCommentCreate(user.getId(), comment.getId());
        int authorLevel = getAuthorLevel(user.getId());
        CommentResponseDto dto = CommentResponseDto.from(comment, user.getId(), authorLevel);
        if (expGrantInfo != null) {
            dto = CommentResponseDto.builder()
                    .commentId(dto.getCommentId())
                    .content(dto.getContent())
                    .userId(dto.getUserId())
                    .nickname(dto.getNickname())
                    .authorLevel(authorLevel)
                    .profileImageUrl(dto.getProfileImageUrl())
                    .isAdmin(dto.isAdmin())
                    .isMine(dto.isMine())
                    .createdAt(dto.getCreatedAt())
                    .updatedAt(dto.getUpdatedAt())
                    .expGrantInfo(new ExpGrantInfoDto(
                            expGrantInfo.expGained(), expGrantInfo.levelUp(), expGrantInfo.newLevel()))
                    .build();
        }
        return dto;
    }

    /**
     * 특정 기사의 댓글 목록을 페이징하여 조회함
     *
     * @param articleId 조회할 기사의 ID
     * @param pageable  페이징 정보
     * @return 댓글 목록 페이지 객체
     *
     * @DateOfCreated 2026-02-03
     * @DateOfEdit 2026-02-03
     */
    public Page<CommentResponseDto> getComments(Long articleId, Pageable pageable, Long currentUserId) {
        if (!articleRepository.existsById(articleId)) {
            throw new CustomException(ErrorCode.ARTICLE_NOT_FOUND);
        }

        Page<Comment> commentPage = commentRepository.findByArticleId(articleId, pageable);
        Set<Long> userIds = commentPage.getContent().stream()
                .map(c -> c.getUser().getId())
                .collect(Collectors.toSet());
        Map<Long, Integer> levelMap = getAuthorLevelMap(userIds);

        return commentPage.map(c -> CommentResponseDto.from(c, currentUserId, levelMap.getOrDefault(c.getUser().getId(), 1)));
    }

    /**
     * 댓글 내용을 수정함
     * 작성자 본인인지 검증 후 수정을 진행함
     *
     * @param commentId 수정할 댓글의 ID
     * @param requestDto 수정할 내용이 담긴 요청 객체
     * @param user      요청을 보낸 사용자 정보
     * @return 수정된 댓글의 응답 DTO
     *
     * @DateOfCreated 2026-02-03
     * @DateOfEdit 2026-02-03
     */
    @Transactional
    public CommentResponseDto updateComment(Long commentId, CommentRequestDto requestDto, User user) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 댓글입니다."));

        validateAuthor(comment, user);

        comment.updateContent(requestDto.getContent());
        int authorLevel = getAuthorLevel(comment.getUser().getId());
        return CommentResponseDto.from(comment, user.getId(), authorLevel);
    }

    /**
     * 댓글을 삭제함
     * 작성자 본인인지 검증 후 삭제를 진행함
     *
     * @param commentId 삭제할 댓글의 ID
     * @param user      요청을 보낸 사용자 정보
     *
     * @DateOfCreated 2026-02-03
     * @DateOfEdit 2026-02-03
     */
    @Transactional
    public void deleteComment(Long commentId, User user) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 댓글입니다."));

        validateAuthor(comment, user);

        commentRepository.delete(comment);
    }

    /**
     * 댓글 작성자와 요청자가 일치하는지 검증함
     *
     * @param comment 검증할 댓글 엔티티
     * @param user    요청을 보낸 사용자 엔티티
     * @throws IllegalArgumentException 작성자가 아닐 경우 예외 발생
     *
     * @DateOfCreated 2026-02-03
     * @DateOfEdit 2026-02-03
     */
    private void validateAuthor(Comment comment, User user) {
        if (!comment.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("작성자만 수정/삭제할 수 있습니다.");
        }
    }

    private Map<Long, Integer> getAuthorLevelMap(Set<Long> userIds) {
        if (userIds.isEmpty()) return Map.of();
        List<UserExp> userExps = userExpRepository.findByUserIdIn(userIds);
        return userExps.stream()
                .collect(Collectors.toMap(UserExp::getUserId, ue -> levelCalculator.calculateLevel(ue.getTotalExp())));
    }

    private int getAuthorLevel(Long userId) {
        return userExpRepository.findByUserId(userId)
                .map(ue -> levelCalculator.calculateLevel(ue.getTotalExp()))
                .orElse(1);
    }
}