import { useState, useEffect, useRef } from 'react';
import { useParams, Link, useNavigate } from 'react-router';
import { ArrowLeft, Bookmark, ChevronDown } from 'lucide-react';
import Header from '../components/Header';
import ArticleCard from '../components/ArticleCard';
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
    likeCount: number;
    dislikeCount: number;
    sadCount: number;
    angryCount: number;
    userReaction: ReactionType | null;
    purchasedTranslationLanguages?: string[];
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

function checkIsAdmin(): boolean {
    const token = sessionStorage.getItem('snwa_token');
    if (!token) return false;
    try {
        const payload = JSON.parse(atob(token.split('.')[1]));

        // payload 구조에따라 'auth' 또는 'role' 필드 확인 (프로젝트마다 다름, 보통 'auth': 'ROLE_ADMIN' 등)
        return payload.auth === 'ROLE_ADMIN' || payload.role === 'ROLE_ADMIN';
    } catch (e) {
        return false;
    }
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
        summary: d.summary ?? undefined,
        purchasedTranslationLanguages: d.purchasedTranslationLanguages ?? [],
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

// 언어 옵션
const LANGUAGE_OPTIONS = [
    { value: 'KO', label: '한국어' },
    { value: 'EN', label: 'English' },
    { value: 'JA', label: '日本語' },
    { value: 'ZH', label: '中文' },
] as const;

export default function ArticleDetailPage() {
    const { id } = useParams<{ id: string }>();
    const navigate = useNavigate();
    const [article, setArticle] = useState<Article | null>(null);
    const [relatedArticles, setRelatedArticles] = useState<Article[]>([]);
    const [showOriginal, setShowOriginal] = useState(true);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    // [변경] 언어 선택 상태 (Dropdown용)
    const [targetLang, setTargetLang] = useState<'KO' | 'EN' | 'JA' | 'ZH'>('KO');
    const [isDropdownOpen, setIsDropdownOpen] = useState(false);
    const dropdownRef = useRef<HTMLDivElement>(null);

    const [translateLoading, setTranslateLoading] = useState(false);

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

    // 드롭다운 외부 클릭 감지
    useEffect(() => {
        function handleClickOutside(event: MouseEvent) {
            if (dropdownRef.current && !dropdownRef.current.contains(event.target as Node)) {
                setIsDropdownOpen(false);
            }
        }
        document.addEventListener('mousedown', handleClickOutside);
        return () => {
            document.removeEventListener('mousedown', handleClickOutside);
        };
    }, []);

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

    /** 번역 요청 핸들러 */
    const handleTranslate = async () => {
        if (!id) return;

        const auth = getAuthHeader();
        if (!auth) {
            alert('로그인이 필요합니다.');
            navigate('/login');
            return;
        }

        const purchasedLangs = article?.purchasedTranslationLanguages ?? [];
        const alreadyPurchased = purchasedLangs.includes(targetLang);
        const isAdmin = checkIsAdmin();

        if (!alreadyPurchased && !isAdmin) {
            const langName = LANGUAGE_OPTIONS.find(o => o.value === targetLang)?.label || targetLang;
            if (!confirm(`${langName} 번역-요약하기 버튼을 누르면 코인 1개를 사용해서 번역본을 열람합니다.\n(이미 구매한 경우 차감되지 않습니다)\n계속하시겠습니까?`)) {
                return;
            }
        }

        setTranslateLoading(true);
        try {
            const res = await fetch(`/api/articles/${id}/translation?lang=${targetLang}`, {
                headers: auth,
            });
            if (!res.ok) throw new Error('번역을 불러오는데 실패했습니다.');

            const data = await res.json();

            setArticle(prev => prev ? {
                ...prev,
                translatedTitle: data.translatedTitle,
                translatedContent: data.translatedContent,
                summary: data.summary,
                purchasedTranslationLanguages: prev.purchasedTranslationLanguages?.includes(targetLang)
                    ? prev.purchasedTranslationLanguages
                    : [...(prev.purchasedTranslationLanguages ?? []), targetLang],
            } : null);

            setShowOriginal(false);
        } catch (e) {
            console.error(e);
            alert('번역을 가져오는데 실패했습니다.');
        } finally {
            setTranslateLoading(false);
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
        } catch {
            setComments([]);
        } finally {
            setCommentsLoading(false);
        }
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
                setCommentContent('');
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
                setIsBookmarked(data.isBookmarked ?? false);
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

                        {/* 번역 기능 컨트롤러 (드롭다운 UI) */}
                        <div className="flex items-center gap-2 mt-4 mb-6 relative">
                            {/* 언어 선택 드롭다운 */}
                            <div className="relative" ref={dropdownRef}>
                                <button
                                    onClick={() => setIsDropdownOpen(!isDropdownOpen)}
                                    className="flex items-center gap-2 px-4 py-2 bg-white border border-gray-300 rounded-lg text-sm font-medium text-gray-700 hover:bg-gray-50 transition-colors shadow-sm min-w-[120px] justify-between"
                                >
                                    {LANGUAGE_OPTIONS.find(opt => opt.value === targetLang)?.label}
                                    <ChevronDown className={`w-4 h-4 transition-transform ${isDropdownOpen ? 'rotate-180' : ''}`} />
                                </button>

                                {isDropdownOpen && (
                                    <div className="absolute top-full left-0 mt-1 w-full bg-white border border-gray-200 rounded-lg shadow-lg overflow-hidden z-10 py-1">
                                        {LANGUAGE_OPTIONS.map((option) => (
                                            <button
                                                key={option.value}
                                                onClick={() => {
                                                    setTargetLang(option.value);
                                                    setIsDropdownOpen(false);
                                                }}
                                                className={`w-full text-left px-4 py-2 text-sm hover:bg-blue-50 hover:text-blue-600 transition-colors ${targetLang === option.value ? 'bg-blue-50 text-blue-600 font-medium' : 'text-gray-700'
                                                    }`}
                                            >
                                                {option.label}
                                            </button>
                                        ))}
                                    </div>
                                )}
                            </div>

                            {/* 번역 실행 버튼 */}
                            <button
                                onClick={handleTranslate}
                                disabled={translateLoading}
                                className="px-5 py-2 text-sm font-medium text-white bg-blue-600 rounded-lg hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed transition-all shadow-md active:scale-95"
                            >
                                {translateLoading ? '번역 중...' : '번역 · 요약하기'}
                            </button>

                            {/* 원문 보기 버튼 (토글) */}
                            {!showOriginal ? (
                                <button
                                    onClick={() => setShowOriginal(true)}
                                    className="ml-auto px-4 py-2 text-sm font-medium text-gray-500 bg-gray-100 rounded-lg hover:text-gray-700 hover:bg-gray-200 transition-colors"
                                >
                                    원문 보기
                                </button>
                            ) : (
                                <span className="ml-auto text-sm text-gray-400">원문 보는 중</span>
                            )}
                        </div>

                        {/* 원문 또는 번역본 표시 */}
                        <div className="prose prose-gray max-w-none mb-6">
                            {!showOriginal ? (
                                <>
                                    {article.summary && (
                                        <div className="mb-6 p-4 bg-gray-50 rounded-lg border border-gray-200">
                                            <h3 className="text-sm font-semibold text-gray-600 mb-2">요약</h3>
                                            <div className="text-gray-900 leading-relaxed whitespace-pre-line">{article.summary}</div>
                                        </div>
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
                    <h2 className="text-xl font-bold text-gray-900 mb-4">댓글 ({comments.length})</h2>

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
                        <ul className="space-y-4">
                            {comments.map((c) => (
                                <li key={c.commentId} className="flex flex-col gap-1 p-4 bg-gray-50 rounded-lg border border-gray-100">
                                    <div className="flex items-center justify-between gap-2">
                                        <div className="flex items-center gap-2 flex-wrap">
                                            <span className="font-medium text-gray-900">
                                                {c.nickname ?? '알 수 없음'}
                                            </span>
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