import { useState, useEffect } from 'react';
import { useParams, Link, useNavigate } from 'react-router';
import { ArrowLeft, Bookmark } from 'lucide-react';
import { useExpToast } from '../contexts/ExpToastContext';
import Header from '../components/Header';
import ArticleCard from '../components/ArticleCard';
import { LevelBadge } from '../components/LevelBadge';
import { formatDate, Article } from '../data/mockArticles';

type ReactionType = 'LIKE' | 'DISLIKE' | 'SAD' | 'ANGRY';

type ApiArticleDetail = {
    id: number;
    title: string;
    translatedTitle: string | null;
    content: string;
    translatedContent: string | null;
    summary: string | null;
    originalUrl: string | null;
    categoryName: string;
    authorName: string | null;
    publisherName: string | null;
    imageUrl: string | null;
    createdDate: string;
    updatedDate: string;
    isBookmarked: boolean;
    clickCount: number;
    /** 해당 기사에 코인을 사용했는지 (true면 번역 본문 공개) */
    hasUsedCoin: boolean;
    likeCount: number;
    dislikeCount: number;
    sadCount: number;
    angryCount: number;
    userReaction: ReactionType | null;
};

type ReactionCounts = {
    likeCount: number;
    dislikeCount: number;
    sadCount: number;
    angryCount: number;
    userReaction: ReactionType | null;
};

type ApiArticleListItem = {
    id: number;
    title: string;
    translatedTitle: string | null;
    summary: string | null;
    categoryName: string;
    authorName: string | null;
    publisherName: string | null;
    imageUrl: string | null;
    createdDate: string;
    clickCount: number | null;
};

type ApiComment = {
    commentId: number;
    content: string;
    userId: number;
    nickname: string;
    authorLevel?: number;
    isAdmin: boolean;
    isMine?: boolean;
    createdAt: string;
    updatedAt?: string;
};

const API_CATEGORY_TO_DISPLAY: Record<string, 'Football' | 'Soccer' | 'Basketball' | 'Baseball' | 'Esports'> = {
    BASKETBALL: 'Basketball',
    SOCCER: 'Soccer',
    BASEBALL: 'Baseball',
    FOOTBALL: 'Football',
};

function getAuthHeader(): Record<string, string> | null {
    const token = sessionStorage.getItem('snwa_token');
    if (!token) return null;
    return { Authorization: `Bearer ${token}` };
}

function mapDetailToArticle(d: ApiArticleDetail): Article {
    const createdStr = d.createdDate?.replace('Z', '') ?? new Date().toISOString().slice(0, 19);
    return {
        id: String(d.id),
        category: API_CATEGORY_TO_DISPLAY[d.categoryName] ?? 'Football',
        translatedTitle: d.translatedTitle ?? d.title,
        originalTitle: d.title,
        source: d.publisherName ?? d.authorName ?? '',
        publishedAt: createdStr,
        thumbnail: d.imageUrl ?? '',
        translatedContent: d.translatedContent ?? '',
        originalContent: d.content ?? '',
        clickCount: d.clickCount ?? 0,
    };
}

function mapListItemToArticle(item: ApiArticleListItem): Article {
    const createdStr = item.createdDate?.replace('Z', '') ?? new Date().toISOString().slice(0, 19);
    return {
        id: String(item.id),
        category: API_CATEGORY_TO_DISPLAY[item.categoryName] ?? 'Football',
        translatedTitle: item.translatedTitle ?? item.title,
        originalTitle: item.title,
        source: item.publisherName ?? item.authorName ?? '',
        publishedAt: createdStr,
        thumbnail: item.imageUrl ?? '',
        translatedContent: '',
        originalContent: '',
        clickCount: item.clickCount ?? 0,
    };
}

// 반응 버튼 설정
const REACTION_CONFIG: { type: ReactionType; emoji: string; label: string }[] = [
    { type: 'LIKE', emoji: '👍', label: '좋아요' },
    { type: 'DISLIKE', emoji: '👎', label: '싫어요' },
    { type: 'SAD', emoji: '😢', label: '슬퍼요' },
    { type: 'ANGRY', emoji: '😠', label: '화나요' },
];

export default function ArticleDetailPage() {
    const { id } = useParams<{ id: string }>();
    const navigate = useNavigate();
    const expToast = useExpToast();
    const [article, setArticle] = useState<Article | null>(null);
    const [relatedArticles, setRelatedArticles] = useState<Article[]>([]);
    const [showOriginal, setShowOriginal] = useState(false);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    /** API에서 내려준 코인 사용 여부 (false면 summary만 표시, true면 translatedContent 표시) */
    const [hasUsedCoin, setHasUsedCoin] = useState(false);
    const [articleSummary, setArticleSummary] = useState<string | null>(null);
    const [useCoinLoading, setUseCoinLoading] = useState(false);

    // 감정 반응 상태
    const [reactions, setReactions] = useState<ReactionCounts>({
        likeCount: 0,
        dislikeCount: 0,
        sadCount: 0,
        angryCount: 0,
        userReaction: null,
    });
    const [reactionLoading, setReactionLoading] = useState(false);

    // 북마크 상태 (API 응답에서 초기화, 토글 시 로컬 업데이트)
    const [isBookmarked, setIsBookmarked] = useState(false);
    const [bookmarkLoading, setBookmarkLoading] = useState(false);

    // 댓글 상태
    const [comments, setComments] = useState<ApiComment[]>([]);
    const [commentsLoading, setCommentsLoading] = useState(false);
    const [commentContent, setCommentContent] = useState('');
    const [commentSubmitting, setCommentSubmitting] = useState(false);
    const [hasMoreComments, setHasMoreComments] = useState(true);
    const [commentPage, setCommentPage] = useState(0);
    const [totalCommentCount, setTotalCommentCount] = useState(0);

    // 반응 타입별 카운트 가져오기
    const getReactionCount = (type: ReactionType): number => {
        switch (type) {
            case 'LIKE': return reactions.likeCount;
            case 'DISLIKE': return reactions.dislikeCount;
            case 'SAD': return reactions.sadCount;
            case 'ANGRY': return reactions.angryCount;
        }
    };

    // 애니메이션 상태 (클릭된 버튼)
    const [animatingType, setAnimatingType] = useState<ReactionType | null>(null);

    // 반응 클릭 핸들러
    const handleReaction = async (reactionType: ReactionType) => {
        const auth = getAuthHeader();
        if (!auth) {
            alert('로그인이 필요합니다.');
            navigate('/login');
            return;
        }
        if (!id || reactionLoading) return;

        // 애니메이션 시작
        setAnimatingType(reactionType);
        setTimeout(() => setAnimatingType(null), 300);

        setReactionLoading(true);
        try {
            const res = await fetch(`/api/articles/${id}/reactions`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    ...auth,
                },
                body: JSON.stringify({ reactionType }),
            });

            if (res.ok) {
                const data = await res.json();
                // userReaction이 빈 문자열이면 null로 처리
                const userReactionValue = data.userReaction && data.userReaction !== ''
                    ? (data.userReaction as ReactionType)
                    : null;
                setReactions({
                    likeCount: data.likeCount ?? 0,
                    dislikeCount: data.dislikeCount ?? 0,
                    sadCount: data.sadCount ?? 0,
                    angryCount: data.angryCount ?? 0,
                    userReaction: userReactionValue,
                });
            } else {
                console.error('반응 등록 실패: HTTP', res.status);
            }
        } catch (err) {
            console.error('반응 등록 실패:', err);
        } finally {
            setReactionLoading(false);
        }
    };

    /** 기사 정보 다시 가져오기 (코인 사용 후 번역본 업데이트용) */
    const refetchArticle = async () => {
        if (!id) return;
        const headers: Record<string, string> = {};
        const auth = getAuthHeader();
        if (auth) Object.assign(headers, auth);
        try {
            const res = await fetch(`/api/articles/${id}?recordView=false`, { headers });
            if (!res.ok) return;
            const data: ApiArticleDetail = await res.json();
            setArticle(mapDetailToArticle(data));
            setHasUsedCoin(data.hasUsedCoin ?? false);
            setIsBookmarked(data.isBookmarked ?? false);
            setArticleSummary(data.summary ?? null);
            setReactions({
                likeCount: data.likeCount ?? 0,
                dislikeCount: data.dislikeCount ?? 0,
                sadCount: data.sadCount ?? 0,
                angryCount: data.angryCount ?? 0,
                userReaction: data.userReaction ?? null,
            });
        } catch (e) {
            console.error('기사 정보 다시 가져오기 실패:', e);
        }
    };

    /** 코인 사용하기: 기사 전체 번역 본문 열기 */
    const handleUseCoin = async () => {
        const auth = getAuthHeader();
        if (!auth) {
            alert('로그인이 필요합니다.');
            navigate('/login');
            return;
        }
        if (!id || useCoinLoading) return;
        setUseCoinLoading(true);
        try {
            const res = await fetch('/api/coins/use', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json', ...auth },
                body: JSON.stringify({ amount: 1, externalRef: `ARTICLE_${id}` }),
            });
            if (!res.ok) {
                const err = await res.json().catch(() => ({}));
                alert(err.message ?? '코인 사용에 실패했습니다. 잔액을 확인해 주세요.');
                return;
            }
            const data = await res.json().catch(() => ({}));
            if (data?.expGrantInfo && expToast) {
                expToast.showExpToast(data.expGrantInfo);
            }
            // 코인 사용 성공 후 기사 정보 다시 가져오기
            await refetchArticle();
            setShowOriginal(false); // 번역본을 기본으로 표시
        } catch (e) {
            console.error(e);
            alert('코인 사용에 실패했습니다.');
        } finally {
            setUseCoinLoading(false);
        }
    };

    /** 북마크 토글: 추가된 상태에서 한 번 더 누르면 삭제 */
    const handleBookmarkToggle = async () => {
        const auth = getAuthHeader();
        if (!auth) {
            alert('로그인이 필요합니다.');
            navigate('/login');
            return;
        }
        if (!id || bookmarkLoading) return;
        setBookmarkLoading(true);
        try {
            const method = isBookmarked ? 'DELETE' : 'POST';
            const res = await fetch(`/api/bookmarks/${id}`, {
                method,
                headers: auth,
            });
            if (res.ok) {
                setIsBookmarked(!isBookmarked);
            } else {
                const err = await res.json().catch(() => ({}));
                alert(err.message ?? '북마크 처리에 실패했습니다.');
            }
        } catch (e) {
            console.error(e);
            alert('북마크 처리에 실패했습니다.');
        } finally {
            setBookmarkLoading(false);
        }
    };

    /** 댓글 목록 로드 */
    const fetchComments = async (page = 0, append = false) => {
        if (!id) return;
        setCommentsLoading(true);
        const auth = getAuthHeader();
        const headers: Record<string, string> = {};
        if (auth) Object.assign(headers, auth);
        try {
            const res = await fetch(`/api/articles/${id}/comments?page=${page}&size=10`, { headers });
            if (!res.ok) return;
            const data = await res.json();
            const list = data?.content ?? [];
            setComments((prev) => (append ? [...prev, ...list] : list));
            setHasMoreComments(!(data?.last ?? true));
            setCommentPage(page);
            if (!append || page === 0) {
                setTotalCommentCount(data?.totalElements ?? list.length);
            }
        } catch {
            setComments([]);
            setHasMoreComments(false);
        } finally {
            setCommentsLoading(false);
        }
    };

    /** 댓글 더 보기 */
    const loadMoreComments = () => {
        fetchComments(commentPage + 1, true);
    };

    /** 댓글 작성 */
    const handleSubmitComment = async () => {
        const auth = getAuthHeader();
        if (!auth) {
            alert('로그인이 필요합니다.');
            navigate('/login');
            return;
        }
        if (!id || !commentContent.trim() || commentSubmitting) return;
        setCommentSubmitting(true);
        try {
            const res = await fetch(`/api/articles/${id}/comments`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json', ...auth },
                body: JSON.stringify({ content: commentContent.trim() }),
            });
            if (res.ok) {
                const data = await res.json().catch(() => ({}));
                if (data?.expGrantInfo && expToast) {
                    expToast.showExpToast(data.expGrantInfo);
                }
                setCommentContent('');
                setTotalCommentCount((prev) => prev + 1);
                fetchComments(0, false);
            } else {
                const err = await res.json().catch(() => ({}));
                alert(err.message ?? '댓글 작성에 실패했습니다.');
            }
        } catch (e) {
            console.error(e);
            alert('댓글 작성에 실패했습니다.');
        } finally {
            setCommentSubmitting(false);
        }
    };

    /** 댓글 삭제 (본인 댓글) */
    const handleDeleteComment = async (commentId: number) => {
        const auth = getAuthHeader();
        if (!auth) return;
        if (!confirm('댓글을 삭제하시겠습니까?')) return;
        try {
            const res = await fetch(`/api/comments/${commentId}`, {
                method: 'DELETE',
                headers: auth,
            });
            if (res.ok) {
                setComments((prev) => prev.filter((c) => c.commentId !== commentId));
                setTotalCommentCount((prev) => Math.max(0, prev - 1));
            }
        } catch {
            alert('삭제에 실패했습니다.');
        }
    };

    useEffect(() => {
        if (!id) {
            setLoading(false);
            return;
        }
        const headers: Record<string, string> = {};
        const auth = getAuthHeader();
        if (auth) Object.assign(headers, auth);

        setLoading(true);
        setError(null);

        fetch(`/api/articles/${id}`, { headers })
            .then((res) => {
                if (!res.ok) throw new Error('기사를 찾을 수 없습니다.');
                return res.json();
            })
            .then((data: ApiArticleDetail) => {
                setArticle(mapDetailToArticle(data));
                setHasUsedCoin(data.hasUsedCoin ?? false);
                setIsBookmarked(data.isBookmarked ?? false);
                setArticleSummary(data.summary ?? null);
                setReactions({
                    likeCount: data.likeCount ?? 0,
                    dislikeCount: data.dislikeCount ?? 0,
                    sadCount: data.sadCount ?? 0,
                    angryCount: data.angryCount ?? 0,
                    userReaction: data.userReaction ?? null,
                });
                const viewed = JSON.parse(localStorage.getItem('snwa_viewed_articles') || '[]');
                const updated = [id, ...viewed.filter((x: string) => x !== id)].slice(0, 10);
                localStorage.setItem('snwa_viewed_articles', JSON.stringify(updated));
                return fetch(`/api/articles/${id}/related`, { headers });
            })
            .then((res) => (res.ok ? res.json() : []))
            .then((list: ApiArticleListItem[]) => {
                setRelatedArticles((list ?? []).map(mapListItemToArticle));
            })
            .then(() => fetchComments(0, false))
            .catch((e) => {
                setError(e instanceof Error ? e.message : '기사를 불러오지 못했습니다.');
                setArticle(null);
                setRelatedArticles([]);
            })
            .finally(() => setLoading(false));
    }, [id]);

    if (loading) {
        return (
            <div className="min-h-screen bg-gray-50">
                <Header />
                <div className="max-w-4xl mx-auto px-4 py-16 text-center">
                    <p className="text-gray-500">기사를 불러오는 중...</p>
                </div>
            </div>
        );
    }

    if (error || !article) {
        return (
            <div className="min-h-screen bg-gray-50">
                <Header />
                <div className="max-w-4xl mx-auto px-4 py-16 text-center">
                    <p className="text-gray-500">{error ?? '기사를 찾을 수 없습니다.'}</p>
                    <Link to="/" className="text-gray-900 font-medium hover:underline mt-4 inline-block">
                        메인으로 돌아가기
                    </Link>
                </div>
            </div>
        );
    }

    return (
        <div className="min-h-screen bg-gray-50">
            <Header />
            <main className="max-w-4xl mx-auto px-4 py-8">
                <Link to="/" className="inline-flex items-center gap-2 text-sm text-gray-600 hover:underline mb-6">
                    <ArrowLeft className="w-4 h-4" /> 목록으로
                </Link>
                <article className="bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden">
                    <img src={article.thumbnail} alt={article.translatedTitle} className="w-full aspect-video object-cover" />
                    <div className="p-6 md:p-8">
                        <div className="inline-block px-3 py-1 text-sm font-medium text-gray-700 bg-gray-100 rounded mb-4">
                            {article.category}
                        </div>
                        <h1 className="text-3xl md:text-4xl font-bold text-gray-900 mb-3">{article.translatedTitle}</h1>
                        <p className="text-lg text-gray-500 mb-6">{article.originalTitle}</p>
                        <div className="flex items-center gap-3 text-sm text-gray-500 pb-6 border-b border-gray-200">
                            <span className="font-medium">{article.source}</span>
                            <span>·</span>
                            <span>{formatDate(article.publishedAt)}</span>
                            <span>·</span>
                            <span>조회 {article.clickCount ?? 0}</span>
                        </div>
                        {/* 원문 또는 번역본 표시 */}
                        <div className="prose prose-gray max-w-none mb-6">
                            {/* 번역하기 버튼 또는 원문/번역본 토글 버튼 - 본문 영역 위쪽 */}
                            <div className="flex justify-end mb-6">
                                {!hasUsedCoin ? (
                                    <button
                                        type="button"
                                        onClick={() => {
                                            if (confirm('코인 1개를 사용해서 번역본을 열람합니다.')) {
                                                handleUseCoin();
                                            }
                                        }}
                                        disabled={useCoinLoading}
                                        className="px-4 py-2 text-sm font-medium text-gray-700 bg-gray-100 rounded-lg hover:bg-gray-200 disabled:opacity-60 disabled:cursor-not-allowed transition-colors whitespace-nowrap shadow-sm mr-4"
                                    >
                                        {useCoinLoading ? '처리 중...' : '번역 · 요약하기'}
                                    </button>
                                ) : (
                                    <button
                                        onClick={() => setShowOriginal(!showOriginal)}
                                        className="px-4 py-2 text-sm font-medium text-gray-700 bg-gray-100 rounded-lg hover:bg-gray-200 transition-colors whitespace-nowrap shadow-sm mr-4"
                                    >
                                        {showOriginal ? '번역 · 요약하기' : '원문 보기'}
                                    </button>
                                )}
                            </div>
                            {hasUsedCoin && !showOriginal ? (
                                <>
                                    {articleSummary && (
                                        <ul className="list-disc list-inside space-y-1 text-gray-700 mb-6 pl-1">
                                            {articleSummary
                                                .split('\n')
                                                .filter((line) => line.trim())
                                                .map((line, i) => (
                                                    <li key={i}>{line.replace(/^\s*-\s*/, '')}</li>
                                                ))}
                                        </ul>
                                    )}
                                    <div className="text-gray-900 leading-relaxed whitespace-pre-line">{article.translatedContent}</div>
                                </>
                            ) : (
                                <div className="text-gray-700 leading-relaxed whitespace-pre-line">{article.originalContent}</div>
                            )}
                        </div>

                        {/* 감정 반응 버튼 + 북마크 */}
                        <div className="mt-8 pt-6 border-t border-gray-200">
                            <p className="text-sm text-gray-500 mb-3">이 기사에 대한 반응을 남겨주세요</p>
                            <div className="flex flex-wrap items-center gap-2">
                                {REACTION_CONFIG.map(({ type, emoji, label }) => {
                                    const isSelected = reactions.userReaction === type;
                                    const isAnimating = animatingType === type;
                                    const count = getReactionCount(type);
                                    return (
                                        <button
                                            key={type}
                                            onClick={() => handleReaction(type)}
                                            disabled={reactionLoading}
                                            className={`
                                                inline-flex items-center gap-1.5 px-3 py-1.5 text-sm font-medium rounded-full
                                                border transform transition-all duration-200
                                                ${isAnimating ? 'scale-110' : 'scale-100'}
                                                ${isSelected
                                                    ? 'bg-blue-50 border-blue-300 text-blue-700 shadow-md ring-2 ring-blue-200'
                                                    : 'bg-gray-50 border-gray-200 text-gray-600 hover:bg-gray-100 hover:border-gray-300 hover:scale-105'
                                                }
                                                ${reactionLoading ? 'opacity-50 cursor-not-allowed' : 'cursor-pointer active:scale-95'}
                                            `}
                                        >
                                            <span className={`text-base transition-transform duration-200 ${isAnimating ? 'scale-125' : ''}`}>
                                                {emoji}
                                            </span>
                                            <span>{label}</span>
                                            <span
                                                className={`
                                                    ml-0.5 min-w-[1.25rem] text-center font-semibold
                                                    transition-all duration-300
                                                    ${isSelected ? 'text-blue-600' : 'text-gray-500'}
                                                    ${isAnimating ? 'scale-125 text-blue-500' : ''}
                                                `}
                                            >
                                                {count}
                                            </span>
                                        </button>
                                    );
                                })}
                                <button
                                    type="button"
                                    onClick={handleBookmarkToggle}
                                    disabled={bookmarkLoading}
                                    title={isBookmarked ? '북마크 해제' : '북마크 추가'}
                                    className={`
                                        inline-flex items-center gap-1.5 px-3 py-1.5 text-sm font-medium rounded-full
                                        border transform transition-all duration-200
                                        ${isBookmarked
                                            ? 'bg-amber-50 border-amber-300 text-amber-700 shadow-md ring-2 ring-amber-200 fill-amber-600'
                                            : 'bg-gray-50 border-gray-200 text-gray-600 hover:bg-gray-100 hover:border-gray-300 hover:scale-105'
                                        }
                                        ${bookmarkLoading ? 'opacity-50 cursor-not-allowed' : 'cursor-pointer active:scale-95'}
                                    `}
                                >
                                    <Bookmark className={`w-4 h-4 ${isBookmarked ? 'fill-current' : ''}`} strokeWidth={1.5} />
                                    <span>{isBookmarked ? '북마크됨' : '북마크'}</span>
                                </button>
                            </div>
                        </div>
                    </div>
                </article>

                {/* 댓글 섹션 */}
                <section className="mt-12">
                    <h2 className="text-xl font-bold text-gray-900 mb-4">
                        댓글 ({totalCommentCount > 0 ? totalCommentCount : comments.length})
                    </h2>

                    {/* 댓글 작성 폼 */}
                    <div className="mb-6">
                        <textarea
                            value={commentContent}
                            onChange={(e) => setCommentContent(e.target.value)}
                            placeholder="댓글을 입력하세요..."
                            rows={3}
                            className="w-full px-4 py-3 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-gray-900 focus:border-transparent resize-none"
                            disabled={commentSubmitting}
                        />
                        <button
                            type="button"
                            onClick={handleSubmitComment}
                            disabled={!commentContent.trim() || commentSubmitting}
                            className="mt-2 px-4 py-2 text-sm font-medium text-white bg-gray-900 rounded-lg hover:bg-gray-800 disabled:opacity-50 disabled:cursor-not-allowed"
                        >
                            {commentSubmitting ? '등록 중...' : '댓글 등록'}
                        </button>
                    </div>

                    {/* 댓글 목록 */}
                    {commentsLoading ? (
                        <p className="text-sm text-gray-500">댓글 불러오는 중...</p>
                    ) : comments.length === 0 ? (
                        <p className="text-sm text-gray-500 py-4">아직 댓글이 없습니다.</p>
                    ) : (
                        <>
                            <ul className="space-y-4">
                                {comments.map((c) => (
                                    <li key={c.commentId} className="flex flex-col gap-1 p-4 bg-gray-50 rounded-lg border border-gray-100">
                                        <div className="flex items-center justify-between gap-2">
                                            <div className="flex items-center gap-2 flex-wrap">
                                                <span className="font-medium text-gray-900">
                                                    {c.nickname ?? '알 수 없음'}
                                                </span>
                                                {c.authorLevel != null && c.authorLevel > 0 && (
                                                    <LevelBadge level={c.authorLevel} />
                                                )}
                                                {c.isAdmin && (
                                                    <span className="text-xs font-semibold text-gray-900">관리자</span>
                                                )}
                                                <span className="text-xs text-gray-500">{formatDate(c.createdAt)}</span>
                                            </div>
                                            {c.isMine && (
                                                <button
                                                    type="button"
                                                    onClick={() => handleDeleteComment(c.commentId)}
                                                    className="text-xs text-gray-500 hover:text-red-600"
                                                >
                                                    삭제
                                                </button>
                                            )}
                                        </div>
                                        <p className="text-gray-700 whitespace-pre-wrap">{c.content}</p>
                                    </li>
                                ))}
                            </ul>
                            {hasMoreComments && comments.length > 0 && !commentsLoading && (
                                <button
                                    type="button"
                                    onClick={loadMoreComments}
                                    disabled={commentsLoading}
                                    className="mt-4 w-full py-2 text-sm font-medium text-gray-600 bg-gray-100 rounded-lg hover:bg-gray-200 disabled:opacity-50"
                                >
                                    댓글 더 보기
                                </button>
                            )}
                            {hasMoreComments && commentsLoading && comments.length > 10 && (
                                <p className="mt-4 text-center text-sm text-gray-500">댓글 불러오는 중...</p>
                            )}
                        </>
                    )}
                </section>

                {relatedArticles.length > 0 && (
                    <div className="mt-12">
                        <h2 className="text-xl font-bold text-gray-900 mb-6">관련 기사</h2>
                        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                            {relatedArticles.map((relatedArticle) => (
                                <ArticleCard key={relatedArticle.id} article={relatedArticle} />
                            ))}
                        </div>
                    </div>
                )}
            </main>
        </div>
    );
}