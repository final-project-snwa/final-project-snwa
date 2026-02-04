import { useState, useEffect } from 'react';
import { Link } from 'react-router';
import Header from '../components/Header';
import ArticleCard from '../components/ArticleCard';
import { Article } from '../data/mockArticles';

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

type ApiArticleListResponse = {
    content: ApiArticleListItem[];
    totalElements: number;
    totalPages: number;
    number: number;
    size: number;
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

export default function MainPage() {
    const [articles, setArticles] = useState<Article[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        const headers: Record<string, string> = {};
        const auth = getAuthHeader();
        if (auth) Object.assign(headers, auth);

        fetch('/api/articles', { headers })
            .then((res) => {
                if (!res.ok) throw new Error('기사 목록을 불러오지 못했습니다.');
                return res.json();
            })
            .then((data: ApiArticleListResponse) => {
                const list = data.content ?? [];
                setArticles(list.map(mapListItemToArticle));
                setError(null);
            })
            .catch((e) => {
                setError(e instanceof Error ? e.message : '기사 목록을 불러오지 못했습니다.');
                setArticles([]);
            })
            .finally(() => setLoading(false));
    }, []);

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

    if (error) {
        return (
            <div className="min-h-screen bg-gray-50">
                <Header />
                <div className="max-w-4xl mx-auto px-4 py-16 text-center">
                    <p className="text-gray-500">{error}</p>
                    <Link to="/" className="text-gray-900 font-medium hover:underline mt-4 inline-block">
                        메인으로 돌아가기
                    </Link>
                </div>
            </div>
        );
    }

    if (articles.length === 0) {
        return (
            <div className="min-h-screen bg-gray-50">
                <Header />
                <div className="max-w-4xl mx-auto px-4 py-16 text-center">
                    <p className="text-gray-500">등록된 기사가 없습니다.</p>
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
                <h1 className="text-2xl font-bold text-gray-900 mb-6">기사 목록</h1>
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                    {articles.map((article) => (
                        <ArticleCard key={article.id} article={article} />
                    ))}
                </div>
            </main>
        </div>
    );
}
