import { useState, useEffect } from 'react';
import { useParams, Link, useNavigate } from 'react-router';
import { ArrowLeft } from 'lucide-react';
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
    // 감정 반응 관련 (좋아요 포함)
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
    const [article, setArticle] = useState<Article | null>(null);
    const [relatedArticles, setRelatedArticles] = useState<Article[]>([]);
    const [showOriginal, setShowOriginal] = useState(false);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    
    // 감정 반응 상태
    const [reactions, setReactions] = useState<ReactionCounts>({
        likeCount: 0,
        dislikeCount: 0,
        sadCount: 0,
        angryCount: 0,
        userReaction: null,
    });
    const [reactionLoading, setReactionLoading] = useState(false);

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
                // 반응 정보 설정
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
                        <div className="flex justify-end mt-6 mb-6">
                            <button
                                onClick={() => setShowOriginal(!showOriginal)}
                                className="px-4 py-2 text-sm font-medium text-gray-700 bg-gray-100 rounded-lg hover:bg-gray-200 transition-colors"
                            >
                                {showOriginal ? '번역 보기' : '원문 보기'}
                            </button>
                        </div>
                        <div className="prose prose-gray max-w-none">
                            {showOriginal ? (
                                <div className="text-gray-700 leading-relaxed whitespace-pre-line">{article.originalContent}</div>
                            ) : (
                                <div className="text-gray-900 leading-relaxed whitespace-pre-line">{article.translatedContent}</div>
                            )}
                        </div>

                        {/* 감정 반응 버튼 */}
                        <div className="mt-8 pt-6 border-t border-gray-200">
                            <p className="text-sm text-gray-500 mb-3">이 기사에 대한 반응을 남겨주세요</p>
                            <div className="flex flex-wrap gap-2">
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
                            </div>
                        </div>
                    </div>
                </article>
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