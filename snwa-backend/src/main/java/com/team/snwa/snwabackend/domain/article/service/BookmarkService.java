package com.team.snwa.snwabackend.domain.article.service;

import com.team.snwa.snwabackend.domain.article.dto.ArticleListResponseDto;
import com.team.snwa.snwabackend.domain.article.entity.Article;
import com.team.snwa.snwabackend.domain.article.entity.Bookmark;
import com.team.snwa.snwabackend.domain.article.repository.ArticleRepository;
import com.team.snwa.snwabackend.domain.article.repository.BookmarkRepository;
import com.team.snwa.snwabackend.domain.user.entity.User;
import com.team.snwa.snwabackend.global.exception.CustomException;
import com.team.snwa.snwabackend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final ArticleRepository articleRepository;

    /**
     * 즐겨찾기 추가
     * @param user 사용자
     * @param articleId 기사 ID
     * @throws CustomException 기사를 찾을 수 없거나 이미 즐겨찾기한 경우
     */
    @Transactional
    public void addBookmark(User user, Long articleId) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new CustomException(ErrorCode.ARTICLE_NOT_FOUND));

        // 이미 즐겨찾기한 경우 예외 발생
        if (bookmarkRepository.existsByUserAndArticle(user, article)) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        Bookmark bookmark = Bookmark.builder()
                .user(user)
                .article(article)
                .build();

        bookmarkRepository.save(bookmark);
    }

    /**
     * 즐겨찾기 삭제
     * @param user 사용자
     * @param articleId 기사 ID
     * @throws CustomException 기사를 찾을 수 없거나 즐겨찾기가 없는 경우
     */
    @Transactional
    public void removeBookmark(User user, Long articleId) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new CustomException(ErrorCode.ARTICLE_NOT_FOUND));

        Bookmark bookmark = bookmarkRepository.findByUserAndArticle(user, article)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_REQUEST));

        bookmarkRepository.delete(bookmark);
    }

    /**
     * 사용자의 즐겨찾기 목록 조회
     * @param user 사용자
     * @param pageable 페이지 정보
     * @return 즐겨찾기한 기사 목록
     */
    public Page<ArticleListResponseDto> getBookmarks(User user, Pageable pageable) {
        Page<Bookmark> bookmarks = bookmarkRepository.findByUserWithArticle(user, pageable);
        return bookmarks.map(bookmark -> ArticleListResponseDto.from(bookmark.getArticle(), true));
    }

    /**
     * 특정 기사의 즐겨찾기 여부 확인
     * @param user 사용자
     * @param articleId 기사 ID
     * @return 즐겨찾기 여부
     */
    public boolean isBookmarked(User user, Long articleId) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new CustomException(ErrorCode.ARTICLE_NOT_FOUND));

        return bookmarkRepository.existsByUserAndArticle(user, article);
    }

    /**
     * 사용자의 즐겨찾기 개수 조회
     * @param user 사용자
     * @return 즐겨찾기 개수
     */
    public long getBookmarkCount(User user) {
        return bookmarkRepository.countByUser(user);
    }

    /**
     * 특정 기사 ID 목록 중 사용자가 즐겨찾기한 기사 ID 집합 조회 (배치용)
     * @param user 사용자 (null이면 빈 집합 반환)
     * @param articleIds 기사 ID 목록
     * @return 즐겨찾기한 기사 ID 집합
     */
    public Set<Long> getBookmarkedArticleIds(User user, List<Long> articleIds) {
        if (user == null || articleIds == null || articleIds.isEmpty()) {
            return Set.of();
        }
        return new HashSet<>(bookmarkRepository.findArticleIdsByUserAndArticleIdIn(user, articleIds));
    }
}
