import { useState, useEffect, useCallback } from 'react';
import { Link, useNavigate } from 'react-router';
import { Search, Bookmark, ShoppingBag } from 'lucide-react';
import Header from '../components/Header';
import ArticleCard from '../components/ArticleCard';
import { Article } from '../data/mockArticles';

type ListMode = 'all' | 'purchased' | 'bookmarks';

type CoinTransaction = {
    type: string;
    externalRef?: string | null;
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

type ApiArticleListResponse = {
    content: ApiArticleListItem[];
    totalElements: number;
    totalPages: number;
    number: number;
    size: number;
};

const API_CATEGORY_TO_DISPLAY: Record<string, 'Soccer' | 'Basketball' | 'Baseball'> = {
    BASKETBALL: 'Basketball',
    SOCCER: 'Soccer',
    BASEBALL: 'Baseball',
};

type ApiCategory = { id: number; categoryName: string };

function getAuthHeader(): Record<string, string> | null {
    const token = sessionStorage.getItem('snwa_token');
    if (!token) return null;
    return { Authorization: `Bearer ${token}` };
}

function mapListItemToArticle(item: ApiArticleListItem): Article {
    const createdStr = item.createdDate?.replace('Z', '') ?? new Date().toISOString().slice(0, 19);
    return {
        id: String(item.id),
        category: API_CATEGORY_TO_DISPLAY[item.categoryName] ?? 'Soccer',
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
    const navigate = useNavigate();
    const PAGE_SIZE = 12;

    const [articles, setArticles] = useState<Article[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [searchInput, setSearchInput] = useState('');
    /** 목록 모드: 전체 기사 / 구매한 기사 / 북마크한 기사 */
    const [listMode, setListMode] = useState<ListMode>('all');
    const [purchasedArticles, setPurchasedArticles] = useState<Article[]>([]);
    const [purchasedLoading, setPurchasedLoading] = useState(false);
    const [bookmarkArticles, setBookmarkArticles] = useState<Article[]>([]);
    const [bookmarkLoading, setBookmarkLoading] = useState(false);
    /** 검색 시 사용하는 키워드 (검색 버튼/엔터 시 반영) */
    const [searchKeyword, setSearchKeyword] = useState('');
    /** 검색 범위: 전체(제목+내용), 제목, 내용 */
    const [searchScope, setSearchScope] = useState<'all' | 'title' | 'content'>('all');
    /** 현재 페이지 (0-based, API와 동일) */
    const [currentPage, setCurrentPage] = useState(0);
    /** 전체 페이지 수 (API 응답 기준) */
    const [totalPages, setTotalPages] = useState(0);
    /** 전체 기사 수 */
    const [totalElements, setTotalElements] = useState(0);
    /** 스포츠 종목 태그 선택 (카테고리 ID, null이면 전체) */
    const [selectedCategoryId, setSelectedCategoryId] = useState<number | null>(null);
    /** 퍼블리셔 태그 선택 (null이면 전체) */
    const [selectedPublisher, setSelectedPublisher] = useState<string | null>(null);
    /** 카테고리 목록 (태그용) */
    const [categories, setCategories] = useState<ApiCategory[]>([]);
    /** 퍼블리셔 목록 (태그용) */
    const [publishers, setPublishers] = useState<string[]>([]);

    const fetchArticles = useCallback(() => {
        const headers: Record<string, string> = {};
        const auth = getAuthHeader();
        if (auth) Object.assign(headers, auth);

        const pageParam = `page=${currentPage}&size=${PAGE_SIZE}`;
        let url: string;
        if (!searchKeyword) {
            const params = new URLSearchParams();
            params.set('page', String(currentPage));
            params.set('size', String(PAGE_SIZE));
            if (selectedCategoryId != null) params.set('categoryId', String(selectedCategoryId));
            if (selectedPublisher != null && selectedPublisher !== '') params.set('publisherName', selectedPublisher);
            url = `/api/articles?${params.toString()}`;
        } else {
            const encoded = encodeURIComponent(searchKeyword);
            if (searchScope === 'title') url = `/api/articles/search/title?keyword=${encoded}&${pageParam}`;
            else if (searchScope === 'content') url = `/api/articles/search/content?keyword=${encoded}&${pageParam}`;
            else url = `/api/articles/search?keyword=${encoded}&${pageParam}`;
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
                setTotalPages(data.totalPages ?? 0);
                setTotalElements(data.totalElements ?? 0);
            })
            .catch((e) => {
                setError(e instanceof Error ? e.message : '기사를 불러오지 못했습니다.');
                setArticles([]);
                setTotalPages(0);
                setTotalElements(0);
            })
            .finally(() => setLoading(false));
    }, [searchKeyword, searchScope, currentPage, selectedCategoryId, selectedPublisher]);

    useEffect(() => {
        fetchArticles();
    }, [fetchArticles]);

    /** 카테고리·퍼블리셔 목록 로드 */
    useEffect(() => {
        const headers: Record<string, string> = {};
        const auth = getAuthHeader();
        if (auth) Object.assign(headers, auth);
        Promise.all([
            fetch('/api/articles/categories', { headers }).then((r) => (r.ok ? r.json() : [])),
            fetch('/api/articles/publishers', { headers }).then((r) => (r.ok ? r.json() : [])),
        ])
            .then(([catList, pubList]) => {
                const orderedList = Array.isArray(catList) ? catList.sort((a, b) => {
                    const order: Record<string, number> = { SOCCER: 1, BASKETBALL: 2, BASEBALL: 3 };
                    return (order[a.categoryName] || 99) - (order[b.categoryName] || 99);
                }) : [];
                setCategories(orderedList);
                setPublishers(Array.isArray(pubList) ? pubList : []);
            })
            .catch(() => {});
    }, []);

    /** 검색 범위 / 태그 변경 시 첫 페이지로 */
    useEffect(() => {
        setCurrentPage(0);
    }, [searchScope, selectedCategoryId, selectedPublisher]);

    /** 구매한 기사 로드: 코인 히스토리에서 ARTICLE_ id 추출 후 기사 상세 조회 */
    useEffect(() => {
        if (listMode !== 'purchased') return;
        const auth = getAuthHeader();
        if (!auth) {
            setPurchasedArticles([]);
            setPurchasedLoading(false);
            return;
        }
        setPurchasedLoading(true);
        fetch('/api/coins/history', { headers: auth })
            .then((res) => (res.ok ? res.json() : []))
            .then((data: CoinTransaction[]) => {
                const ids = [...new Set(
                    (data ?? [])
                        .filter((tx) => tx.type === 'SPEND')
                        .map((tx) => {
                            if (tx.externalRef?.startsWith('ARTICLE_')) {
                                return tx.externalRef.replace('ARTICLE_', '');
                            }
                            if (tx.externalRef?.startsWith('TRANS_')) {
                                // 형식: TRANS_{articleId}_{lang}_{timestamp}
                                const parts = tx.externalRef.split('_');
                                return parts[1]; // articleId 반환
                            }
                            return null;
                        })
                        .filter(Boolean)
                )];
                if (ids.length === 0) {
                    setPurchasedArticles([]);
                    setPurchasedLoading(false);
                    return;
                }
                return Promise.all(
                    ids.slice(0, 50).map((id) =>
                        fetch(`/api/articles/${id}?recordView=false`, { headers: auth })
                            .then((r) => (r.ok ? r.json() : null))
                    )
                ).then((results) => {
                    const list = (results ?? []).filter(Boolean).map((d: ApiArticleListItem & { createdDate?: string }) =>
                        mapListItemToArticle({
                            id: d.id,
                            title: d.title,
                            translatedTitle: d.translatedTitle ?? d.title,
                            summary: d.summary ?? null,
                            categoryName: d.categoryName,
                            authorName: d.authorName ?? null,
                            publisherName: d.publisherName ?? null,
                            imageUrl: d.imageUrl ?? null,
                            createdDate: d.createdDate ?? new Date().toISOString(),
                            clickCount: d.clickCount ?? null,
                        })
                    );
                    setPurchasedArticles(list);
                });
            })
            .catch(() => setPurchasedArticles([]))
            .finally(() => setPurchasedLoading(false));
    }, [listMode, navigate]);

    /** 북마크한 기사 로드 */
    useEffect(() => {
        if (listMode !== 'bookmarks') return;
        const auth = getAuthHeader();
        if (!auth) {
            setBookmarkArticles([]);
            setBookmarkLoading(false);
            return;
        }
        setBookmarkLoading(true);
        fetch('/api/bookmarks/me?size=50', { headers: auth })
            .then((res) => (res.ok ? res.json() : { content: [] }))
            .then((data: ApiArticleListResponse) => {
                const list = (data.content ?? []).map(mapListItemToArticle);
                setBookmarkArticles(list);
            })
            .catch(() => setBookmarkArticles([]))
            .finally(() => setBookmarkLoading(false));
    }, [listMode, navigate]);

    const handleSearch = () => {
        setSearchKeyword(searchInput.trim());
        setCurrentPage(0);
    };

    const handleClearSearch = () => {
        setSearchInput('');
        setSearchKeyword('');
    };

    const categoryLabel = (name: string) => API_CATEGORY_TO_DISPLAY[name] ?? name;

    /** 태그(종목/퍼블리셔)에 맞게 기사 목록 필터링 */
    const filterArticlesByTags = (list: Article[]) => {
        return list.filter((article) => {
            if (selectedCategoryId != null) {
                const cat = categories.find((c) => c.id === selectedCategoryId);
                if (!cat || article.category !== categoryLabel(cat.categoryName)) return false;
            }
            if (selectedPublisher != null && selectedPublisher !== '') {
                if (article.source !== selectedPublisher) return false;
            }
            return true;
        });
    };

    const filteredPurchased = filterArticlesByTags(purchasedArticles);
    const filteredBookmarks = filterArticlesByTags(bookmarkArticles);

    const isLoggedIn = !!getAuthHeader();

    const listModeTabs = !searchKeyword && (
        <div className="mb-6 flex flex-wrap items-center gap-2">
            <span className="text-sm font-medium text-gray-600 mr-1">보기:</span>
            <button
                type="button"
                onClick={() => setListMode('all')}
                className={`inline-flex items-center gap-1.5 px-3 py-1.5 rounded-full text-sm font-medium transition-colors ${
                    listMode === 'all' ? 'bg-gray-900 text-white' : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                }`}
            >
                기사 목록
            </button>
            <button
                type="button"
                onClick={() => setListMode('purchased')}
                className={`inline-flex items-center gap-1.5 px-3 py-1.5 rounded-full text-sm font-medium transition-colors ${
                    listMode === 'purchased' ? 'bg-gray-900 text-white' : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                }`}
            >
                <ShoppingBag className="w-4 h-4" />
                구매한 기사
            </button>
            <button
                type="button"
                onClick={() => setListMode('bookmarks')}
                className={`inline-flex items-center gap-1.5 px-3 py-1.5 rounded-full text-sm font-medium transition-colors ${
                    listMode === 'bookmarks' ? 'bg-gray-900 text-white' : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                }`}
            >
                <Bookmark className="w-4 h-4" />
                북마크한 기사
            </button>
        </div>
    );

    const filterTags = (
        <div className="mb-6 flex flex-col gap-4">
            <div className="flex flex-col gap-2">
                <span className="text-sm font-medium text-gray-700">스포츠 종목 123</span>
                <div className="flex flex-wrap gap-2">
                    <button
                        type="button"
                        onClick={() => setSelectedCategoryId(null)}
                        className={`px-3 py-1.5 rounded-full text-sm font-medium transition-colors ${
                            selectedCategoryId === null
                                ? 'bg-gray-900 text-white'
                                : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                        }`}
                    >
                        전체
                    </button>
                    {categories.map((c) => (
                        <button
                            key={c.id}
                            type="button"
                            onClick={() => setSelectedCategoryId(selectedCategoryId === c.id ? null : c.id)}
                            className={`px-3 py-1.5 rounded-full text-sm font-medium transition-colors ${
                                selectedCategoryId === c.id
                                    ? 'bg-gray-900 text-white'
                                    : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                            }`}
                        >
                            {categoryLabel(c.categoryName)}
                        </button>
                    ))}
                </div>
            </div>
            <div className="flex flex-col gap-2">
                <span className="text-sm font-medium text-gray-700">퍼블리셔</span>
                <div className="flex flex-wrap gap-2">
                    <button
                        type="button"
                        onClick={() => setSelectedPublisher(null)}
                        className={`px-3 py-1.5 rounded-full text-sm font-medium transition-colors ${
                            selectedPublisher === null
                                ? 'bg-gray-900 text-white'
                                : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                        }`}
                    >
                        전체
                    </button>
                    {publishers.map((p) => (
                        <button
                            key={p}
                            type="button"
                            onClick={() => setSelectedPublisher(selectedPublisher === p ? null : p)}
                            className={`px-3 py-1.5 rounded-full text-sm font-medium transition-colors ${
                                selectedPublisher === p ? 'bg-gray-900 text-white' : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                            }`}
                        >
                            {p}
                        </button>
                    ))}
                </div>
            </div>
        </div>
    );

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

    if (listMode === 'all' && loading) {
        return (
            <div className="min-h-screen bg-gray-50">
                <Header />
                <main className="max-w-4xl mx-auto px-4 py-8">
                    {searchBar}
                    {!searchKeyword && filterTags}
                    <div className="py-16 text-center">
                        <p className="text-gray-500">기사를 불러오는 중...</p>
                    </div>
                </main>
            </div>
        );
    }

    if (listMode === 'all' && error) {
        return (
            <div className="min-h-screen bg-gray-50">
                <Header />
                <main className="max-w-4xl mx-auto px-4 py-8">
                    {searchBar}
                    {!searchKeyword && filterTags}
                    {!searchKeyword && listModeTabs}
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

    if (listMode === 'all' && articles.length === 0) {
        return (
            <div className="min-h-screen bg-gray-50">
                <Header />
                <main className="max-w-4xl mx-auto px-4 py-8">
                    {searchBar}
                    {!searchKeyword && filterTags}
                    {!searchKeyword && listModeTabs}
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
                {!searchKeyword && filterTags}
                {!searchKeyword && listModeTabs}
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
                <h1 className="text-2xl font-bold text-gray-900 mb-6">
                    {listMode === 'purchased' ? '구매한 기사' : listMode === 'bookmarks' ? '북마크한 기사' : '기사 목록'}
                </h1>

                {listMode === 'purchased' && (
                    <>
                        {purchasedLoading ? (
                            <div className="py-16 text-center">
                                <p className="text-gray-500">구매한 기사를 불러오는 중...</p>
                            </div>
                        ) : purchasedArticles.length === 0 ? (
                            <div className="py-16 text-center">
                                <ShoppingBag className="w-12 h-12 text-gray-300 mx-auto mb-3" />
                                <p className="text-gray-500">
                                    {isLoggedIn
                                        ? '구매한 기사가 없습니다.'
                                        : '로그인하면 구매한 기사를 볼 수 있습니다.'}
                                </p>
                                <p className="text-sm text-gray-400 mt-1">
                                    {isLoggedIn
                                        ? '기사에서 코인으로 번역을 열어보면 여기에 표시됩니다.'
                                        : '기사에서 코인으로 번역을 열어보면 여기에 표시됩니다.'}
                                </p>
                                {!isLoggedIn && (
                                    <Link
                                        to="/login"
                                        className="inline-block mt-4 px-4 py-2 text-sm font-medium text-white bg-gray-900 rounded-lg hover:bg-gray-800"
                                    >
                                        로그인하기
                                    </Link>
                                )}
                            </div>
                        ) : filteredPurchased.length === 0 ? (
                            <div className="py-16 text-center">
                                <p className="text-gray-500">선택한 종목·퍼블리셔에 맞는 구매한 기사가 없습니다.</p>
                                <p className="text-sm text-gray-400 mt-1">위 태그에서 다른 조건을 선택해 보세요.</p>
                            </div>
                        ) : (
                            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                                {filteredPurchased.map((article) => (
                                    <ArticleCard key={article.id} article={article} />
                                ))}
                            </div>
                        )}
                    </>
                )}

                {listMode === 'bookmarks' && (
                    <>
                        {bookmarkLoading ? (
                            <div className="py-16 text-center">
                                <p className="text-gray-500">북마크한 기사를 불러오는 중...</p>
                            </div>
                        ) : bookmarkArticles.length === 0 ? (
                            <div className="py-16 text-center">
                                <Bookmark className="w-12 h-12 text-gray-300 mx-auto mb-3" />
                                <p className="text-gray-500">
                                    {isLoggedIn
                                        ? '북마크한 기사가 없습니다.'
                                        : '로그인하면 북마크한 기사를 볼 수 있습니다.'}
                                </p>
                                <p className="text-sm text-gray-400 mt-1">
                                    {isLoggedIn
                                        ? '기사에서 북마크 버튼을 누르면 여기에 표시됩니다.'
                                        : '기사에서 북마크 버튼을 누르면 여기에 표시됩니다.'}
                                </p>
                                {!isLoggedIn && (
                                    <Link
                                        to="/login"
                                        className="inline-block mt-4 px-4 py-2 text-sm font-medium text-white bg-gray-900 rounded-lg hover:bg-gray-800"
                                    >
                                        로그인하기
                                    </Link>
                                )}
                            </div>
                        ) : filteredBookmarks.length === 0 ? (
                            <div className="py-16 text-center">
                                <p className="text-gray-500">선택한 종목·퍼블리셔에 맞는 북마크한 기사가 없습니다.</p>
                                <p className="text-sm text-gray-400 mt-1">위 태그에서 다른 조건을 선택해 보세요.</p>
                            </div>
                        ) : (
                            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                                {filteredBookmarks.map((article) => (
                                    <ArticleCard key={article.id} article={article} />
                                ))}
                            </div>
                        )}
                    </>
                )}

                {listMode === 'all' && (
                    <>
                        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                            {articles.map((article) => (
                                <ArticleCard key={article.id} article={article} />
                            ))}
                        </div>
                    </>
                )}

                {listMode === 'all' && totalPages > 0 && (
                    <nav className="mt-10 flex flex-col items-center gap-2" aria-label="페이지 네비게이션">
                        <p className="text-sm text-gray-500">
                            전체 {totalElements}건 · {currentPage + 1} / {totalPages} 페이지
                        </p>
                        <div className="flex flex-wrap items-center justify-center gap-1">
                            <button
                                type="button"
                                onClick={() => setCurrentPage((p) => Math.max(0, p - 1))}
                                disabled={currentPage === 0}
                                className="min-w-[2.25rem] h-9 px-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-md hover:bg-gray-50 disabled:opacity-50 disabled:pointer-events-none"
                            >
                                이전
                            </button>
                            {Array.from({ length: totalPages }, (_, i) => i).map((page) => (
                                <button
                                    key={page}
                                    type="button"
                                    onClick={() => setCurrentPage(page)}
                                    className={`min-w-[2.25rem] h-9 px-2 text-sm font-medium rounded-md transition-colors ${
                                        page === currentPage
                                            ? 'bg-gray-900 text-white'
                                            : 'bg-white text-gray-700 border border-gray-300 hover:bg-gray-50'
                                    }`}
                                >
                                    {page + 1}
                                </button>
                            ))}
                            <button
                                type="button"
                                onClick={() => setCurrentPage((p) => Math.min(totalPages - 1, p + 1))}
                                disabled={currentPage >= totalPages - 1}
                                className="min-w-[2.25rem] h-9 px-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-md hover:bg-gray-50 disabled:opacity-50 disabled:pointer-events-none"
                            >
                                다음
                            </button>
                        </div>
                    </nav>
                )}
            </main>
        </div>
    );
}
