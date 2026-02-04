import { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router';
import { ArrowLeft } from 'lucide-react';
import Header from '../components/Header';
import ArticleCard from '../components/ArticleCard';
import { formatDate, Article } from '../data/mockArticles';

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
    isLiked: boolean;
    likeCount: number;
    clickCount: number;
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

export default function ArticleDetailPage() {
    const { id } = useParams<{ id: string }>();
    const [article, setArticle] = useState<Article | null>(null);
    const [relatedArticles, setRelatedArticles] = useState<Article[]>([]);
    const [showOriginal, setShowOriginal] = useState(false);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

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