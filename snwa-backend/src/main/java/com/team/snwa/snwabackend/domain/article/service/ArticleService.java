package com.team.snwa.snwabackend.domain.article.service;

import com.team.snwa.snwabackend.domain.article.dto.ArticleDetailResponseDto;
import com.team.snwa.snwabackend.domain.article.dto.ArticleListResponseDto;
import com.team.snwa.snwabackend.domain.article.dto.request.ArticleCreateRequestDto;
import com.team.snwa.snwabackend.domain.article.dto.response.ReactionCountResponseDto;
import com.team.snwa.snwabackend.domain.article.entity.Article;
import com.team.snwa.snwabackend.domain.article.entity.Category;
import com.team.snwa.snwabackend.domain.article.entity.ClickLog;
import com.team.snwa.snwabackend.domain.article.repository.ArticleRepository;
import com.team.snwa.snwabackend.domain.article.repository.CategoryRepository;
import com.team.snwa.snwabackend.domain.article.repository.ClickLogRepository;
import com.team.snwa.snwabackend.domain.user.entity.User;
import com.team.snwa.snwabackend.domain.user.entity.enums.UserRole;
import com.team.snwa.snwabackend.domain.wallet.service.CoinTransactionService;
import com.team.snwa.snwabackend.global.exception.CustomException;
import com.team.snwa.snwabackend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final CategoryRepository categoryRepository;
    private final BookmarkService bookmarkService;
    private final ReactionService reactionService;
    private final ClickLogRepository clickLogRepository;
    private final CoinTransactionService coinTransactionService;

    /**
     * 기사 생성
     * @param user 글을 등록한 사용자 (필수)
     * @param request 생성 요청 (categoryId, title, content 필수)
     * @return 생성된 기사 상세 정보
     * @throws CustomException 카테고리를 찾을 수 없거나 originalUrl 중복인 경우
     */
    @Transactional
    public ArticleDetailResponseDto createArticle(User user, ArticleCreateRequestDto request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_REQUEST));

        String originalUrl = (request.getOriginalUrl() != null && !request.getOriginalUrl().isBlank())
                ? request.getOriginalUrl().trim()
                : null;
        if (originalUrl != null && articleRepository.existsByOriginalUrl(originalUrl)) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        Article article = Article.builder()
                .category(category)
                .user(user)
                .title(request.getTitle())
                .content(request.getContent())
                .translatedContent(null)
                .summary(null)
                .originalUrl(originalUrl)
                .authorName(request.getAuthorName())
                .publisherName(request.getPublisherName())
                .imageUrl(request.getImageUrl())
                .build();

        Article saved = articleRepository.save(article);
        return ArticleDetailResponseDto.from(saved, false, saved.getClickCount(), null);
    }

    /**
     * 기사 목록 조회
     * @param categoryId 카테고리 ID (선택적, null이면 전체 조회)
     * @param pageable 페이지 정보
     * @param user 인증된 사용자 (null이면 비로그인, isBookmarked는 false)
     * @return 기사 목록
     */
    public Page<ArticleListResponseDto> getArticleList(Long categoryId, Pageable pageable, User user) {
        Page<Article> articles = articleRepository.findAllWithCategory(categoryId, pageable);
        Set<Long> bookmarkedIds = bookmarkService.getBookmarkedArticleIds(user,
                articles.getContent().stream().map(Article::getId).toList());
        return articles.map(a -> ArticleListResponseDto.from(a, bookmarkedIds.contains(a.getId())));
    }

    /**
     * 기사 상세 조회
     * @param id 기사 ID
     * @param user 인증된 사용자 (null이면 비로그인, isBookmarked는 false)
     * @return 기사 상세 정보
     * @throws CustomException 기사를 찾을 수 없을 경우
     */
    @Transactional
    public ArticleDetailResponseDto getArticleDetail(Long id, User user, boolean recordView) {
        Article article = articleRepository.findByIdWithCategory(id)
                .orElseThrow(() -> new CustomException(ErrorCode.ARTICLE_NOT_FOUND));

        Long displayClickCount;
        if (recordView) {
            // 조회수 증가
            articleRepository.incrementClickCountById(id);
            // 클릭 로그 저장 (비로그인 유저는 조회수만 +1 로그에는 저장 X)
            if (user != null) {
                ClickLog clickLog = ClickLog.builder()
                        .user(user)
                        .article(article)
                        .build();
                clickLogRepository.save(clickLog);
            }
            displayClickCount = (article.getClickCount() == null ? 0L : article.getClickCount()) + 1;
        } else {
            displayClickCount = article.getClickCount() == null ? 0L : article.getClickCount();
        }

        boolean isBookmarked = user != null && bookmarkService.isBookmarked(user, id);

        // admin이면 true, 아니면 해당 기사에 코인 사용 이력이 있으면 true
        boolean hasUsedCoin = user != null && (
                user.getRole() == UserRole.ADMIN || coinTransactionService.hasUsedCoinForArticle(user.getId(), id));

        // 감정 반응 정보 조회
        Long userId = user != null ? user.getId() : null;
        ReactionCountResponseDto reactionCounts = reactionService.getReactionCounts(id, userId);

        return ArticleDetailResponseDto.from(article, isBookmarked, displayClickCount, reactionCounts, hasUsedCoin);
    }

    /**
     * 기사 삭제 (소프트 삭제)
     * @param id 기사 ID
     * @throws CustomException 기사를 찾을 수 없을 경우
     */
    @Transactional
    public void deleteArticle(Long id) {
        Article article = articleRepository.findByIdWithCategory(id)
                .orElseThrow(() -> new CustomException(ErrorCode.ARTICLE_NOT_FOUND));
        article.softDelete();
        articleRepository.save(article);
    }

    /**
     * 기사 검색 (제목 + 내용)
     * @param keyword 검색어
     * @param pageable 페이지 정보
     * @param user 인증된 사용자 (null이면 비로그인)
     * @return 검색된 기사 목록
     */
    public Page<ArticleListResponseDto> searchArticles(String keyword, Pageable pageable, User user) {
        Page<Article> articles = articleRepository.searchByKeyword(keyword, pageable);
        Set<Long> bookmarkedIds = bookmarkService.getBookmarkedArticleIds(user,
                articles.getContent().stream().map(Article::getId).toList());
        return articles.map(a -> ArticleListResponseDto.from(a, bookmarkedIds.contains(a.getId())));
    }

    /**
     * 제목만 검색
     * @param keyword 검색어
     * @param pageable 페이지 정보
     * @param user 인증된 사용자 (null이면 비로그인)
     * @return 검색된 기사 목록
     */
    public Page<ArticleListResponseDto> searchByTitle(String keyword, Pageable pageable, User user) {
        Page<Article> articles = articleRepository.searchByTitle(keyword, pageable);
        Set<Long> bookmarkedIds = bookmarkService.getBookmarkedArticleIds(user,
                articles.getContent().stream().map(Article::getId).toList());
        return articles.map(a -> ArticleListResponseDto.from(a, bookmarkedIds.contains(a.getId())));
    }

    /**
     * 내용만 검색
     * @param keyword 검색어
     * @param pageable 페이지 정보
     * @param user 인증된 사용자 (null이면 비로그인)
     * @return 검색된 기사 목록
     */
    public Page<ArticleListResponseDto> searchByContent(String keyword, Pageable pageable, User user) {
        Page<Article> articles = articleRepository.searchByContent(keyword, pageable);
        Set<Long> bookmarkedIds = bookmarkService.getBookmarkedArticleIds(user,
                articles.getContent().stream().map(Article::getId).toList());
        return articles.map(a -> ArticleListResponseDto.from(a, bookmarkedIds.contains(a.getId())));
    }

    /**
     * 관련 기사 조회 (같은 카테고리의 최신 기사 3개, 현재 기사 제외)
     * @param articleId 현재 기사 ID
     * @param user 인증된 사용자 (null이면 비로그인)
     * @return 관련 기사 목록 (최대 3개)
     */
    public List<ArticleListResponseDto> getRelatedArticles(Long articleId, User user) {
        Article article = articleRepository.findByIdWithCategory(articleId)
                .orElseThrow(() -> new CustomException(ErrorCode.ARTICLE_NOT_FOUND));

        // 카테고리가 없는 경우 빈 리스트 반환
        if (article.getCategory() == null) {
            return List.of();
        }

        Long categoryId = article.getCategory().getId();
        Pageable pageable = PageRequest.of(0, 3);
        List<Article> relatedArticles = articleRepository.findRelatedArticles(
                categoryId,
                articleId,
                pageable
        );

        Set<Long> bookmarkedIds = bookmarkService.getBookmarkedArticleIds(user,
                relatedArticles.stream().map(Article::getId).toList());

        return relatedArticles.stream()
                .map(a -> ArticleListResponseDto.from(a, bookmarkedIds.contains(a.getId())))
                .collect(Collectors.toList());
    }
}
