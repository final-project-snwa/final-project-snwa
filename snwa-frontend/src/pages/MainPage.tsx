import { useState, useEffect, useCallback } from 'react';
import { Link } from 'react-router';
import { Search } from 'lucide-react';
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
    const [searchInput, setSearchInput] = useState('');
    /** 검색 시 사용하는 키워드 (검색 버튼/엔터 시 반영) */
    const [searchKeyword, setSearchKeyword] = useState('');
    /** 검색 범위: 전체(제목+내용), 제목, 내용 */
    const [searchScope, setSearchScope] = useState<'all' | 'title' | 'content'>('all');

    const fetchArticles = useCallback(() => {
        const headers: Record<string, string> = {};
        const auth = getAuthHeader();
        if (auth) Object.assign(headers, auth);

        let url: string;
        if (!searchKeyword) {
            url = '/api/articles';
        } else {
            const encoded = encodeURIComponent(searchKeyword);
            if (searchScope === 'title') url = `/api/articles/search/title?keyword=${encoded}`;
            else if (searchScope === 'content') url = `/api/articles/search/content?keyword=${encoded}`;
            else url = `/api/articles/search?keyword=${encoded}`;
        }

        setLoading(true);
        setError(null);
        fetch(url, { headers })
            .then((res) => {
                if (!res.ok) throw new Error(searchKeyword ? '검색 결과를 불러오지 못했습니다.' : '기사 목록을 불러오지 못했습니다.');
                return res.json();
            })
            .then((data: ApiArticleListResponse) => {
                const list = data.content ?? [];
                setArticles(list.map(mapListItemToArticle));
            })
            .catch((e) => {
                setError(e instanceof Error ? e.message : '기사를 불러오지 못했습니다.');
                setArticles([]);
            })
            .finally(() => setLoading(false));
    }, [searchKeyword, searchScope]);

    useEffect(() => {
        fetchArticles();
    }, [fetchArticles]);

    const handleSearch = () => {
        setSearchKeyword(searchInput.trim());
    };

    const handleClearSearch = () => {
        setSearchInput('');
        setSearchKeyword('');
    };

    const searchBar = (
        <div className="mb-6 flex flex-col gap-3">
            <div className="flex flex-col sm:flex-row gap-2 sm:items-center">
                <div className="relative flex-1 min-w-0">
                    <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400 pointer-events-none" />
                    <input
                        type="text"
                        value={searchInput}
                        onChange={(e) => setSearchInput(e.target.value)}
                        onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
                        placeholder="검색어 입력"
                        className="w-full pl-9 pr-4 py-2 text-sm border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-gray-300 focus:border-transparent"
                    />
                </div>
                <div className="flex items-center gap-2 shrink-0">
                    <span className="text-sm text-gray-500">검색 범위:</span>
                    <select
                        value={searchScope}
                        onChange={(e) => setSearchScope(e.target.value as 'all' | 'title' | 'content')}
                        className="py-2 pl-3 pr-8 text-sm border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-gray-300 focus:border-transparent bg-white"
                    >
                        <option value="all">전체</option>
                        <option value="title">제목</option>
                        <option value="content">내용</option>
                    </select>
                </div>
                <button
                    type="button"
                    onClick={handleSearch}
                    className="px-4 py-2 text-sm font-medium text-gray-700 bg-gray-100 rounded-lg hover:bg-gray-200 transition-colors shrink-0"
                >
                    검색
                </button>
            </div>
        </div>
    );

    if (loading) {
        return (
            <div className="min-h-screen bg-gray-50">
                <Header />
                <main className="max-w-4xl mx-auto px-4 py-8">
                    {searchBar}
                    <div className="py-16 text-center">
                        <p className="text-gray-500">기사를 불러오는 중...</p>
                    </div>
                </main>
            </div>
        );
    }

    if (error) {
        return (
            <div className="min-h-screen bg-gray-50">
                <Header />
                <main className="max-w-4xl mx-auto px-4 py-8">
                    {searchBar}
                    <div className="py-16 text-center">
                        <p className="text-gray-500">{error}</p>
                        <Link to="/" className="text-gray-900 font-medium hover:underline mt-4 inline-block">
                            메인으로 돌아가기
                        </Link>
                    </div>
                </main>
            </div>
        );
    }

    const emptyMessage = searchKeyword ? `"${searchKeyword}"에 대한 검색 결과가 없습니다.` : '등록된 기사가 없습니다.';

    if (articles.length === 0) {
        return (
            <div className="min-h-screen bg-gray-50">
                <Header />
                <main className="max-w-4xl mx-auto px-4 py-8">
                    {searchBar}
                    <div className="py-16 text-center">
                        <p className="text-gray-500">{emptyMessage}</p>
                        {searchKeyword && (
                            <button
                                type="button"
                                onClick={handleClearSearch}
                                className="text-gray-900 font-medium hover:underline mt-4 inline-block"
                            >
                                전체 목록 보기
                            </button>
                        )}
                        {!searchKeyword && (
                            <Link to="/" className="text-gray-900 font-medium hover:underline mt-4 inline-block">
                                메인으로 돌아가기
                            </Link>
                        )}
                    </div>
                </main>
            </div>
        );
    }

    return (
        <div className="min-h-screen bg-gray-50">
            <Header />
            <main className="max-w-4xl mx-auto px-4 py-8">
                {searchBar}
                {searchKeyword && (
                    <div className="mb-4 flex items-center gap-2 text-sm text-gray-500">
                        <span>검색어: &quot;{searchKeyword}&quot;</span>
                        <button
                            type="button"
                            onClick={handleClearSearch}
                            className="text-gray-700 font-medium hover:underline"
                        >
                            전체 목록 보기
                        </button>
                    </div>
                )}
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
