import { useEffect, useState } from 'react';
import { useNavigate, Link } from 'react-router';
import { useAuth } from '../contexts/AuthContext';
import Header from '../components/Header';
import { User, Bookmark, X } from 'lucide-react';
import ArticleCard from '../components/ArticleCard';
import { Article } from '../data/mockArticles';
import { PieChart, Pie, Cell, ResponsiveContainer, Tooltip, Legend } from 'recharts';

type CategoryClick = { categoryName: string; count: number };

function getAuthHeader(): Record<string, string> | null {
    const token = sessionStorage.getItem('snwa_token');
    if (!token) return null;
    return { Authorization: `Bearer ${token}` };
}

const API_CATEGORY_MAP: Record<string, 'Football' | 'Soccer' | 'Basketball' | 'Baseball' | 'Esports'> = {
    BASKETBALL: 'Basketball',
    SOCCER: 'Soccer',
    BASEBALL: 'Baseball',
    FOOTBALL: 'Football',
};

// 원형 그래프 색상 설정
const PIE_COLORS = ['#3b82f6', '#22c55e', '#eab308', '#ef4444'];

// 프로필 데이터 타입
interface ProfileData {
    nickname?: string;
    email?: string;
    introduction?: string;
    phoneNumber?: string;
    profileImageUrl?: string;
    status?: string;
    role?: string;
    createdAt?: string;
}

type CoinTransaction = {
    id: number;
    type: 'SPEND' | 'CHARGE' | 'ATTENDANCE_REWARD' | 'REFUND';
    status: 'SUCCESS' | 'FAIL';
    amount: number;
    balanceAfter: number;
    externalRef?: string | null;
    createdAt?: string;
    createdDate?: string;
};

export default function MyPage() {
    const { user, logout } = useAuth();
    const navigate = useNavigate();
    const [recentArticles, setRecentArticles] = useState<Article[]>([]);
    const [bookmarkArticles, setBookmarkArticles] = useState<Article[]>([]);
    const [bookmarkCount, setBookmarkCount] = useState(0);
    const [bookmarkLoading, setBookmarkLoading] = useState(false);
    const [categoryClicks, setCategoryClicks] = useState<CategoryClick[]>([]);
    const [clicksLoading, setClicksLoading] = useState(false);
    const [profile, setProfile] = useState<ProfileData | null>(null);
    const [coinHistory, setCoinHistory] = useState<CoinTransaction[]>([]);
    const [coinHistoryLoading, setCoinHistoryLoading] = useState(false);

    useEffect(() => {
        if (!user) {
            navigate('/login');
            return;
        }

        const viewedIds = JSON.parse(localStorage.getItem('snwa_viewed_articles') || '[]') as string[];
        const ids = viewedIds.slice(0, 6);
        if (ids.length === 0) {
            setRecentArticles([]);
            return;
        }
        const headers: Record<string, string> = {};
        const auth = getAuthHeader();
        if (auth) Object.assign(headers, auth);
        Promise.all(
            ids.map((articleId) =>
                fetch(`/api/articles/${articleId}?recordView=false`, { headers })
                    .then((res) => (res.ok ? res.json() : null))
                    .then((data: {
                        id: number;
                        title: string;
                        translatedTitle: string | null;
                        categoryName: string;
                        publisherName: string | null;
                        authorName: string | null;
                        imageUrl: string | null;
                        createdDate: string;
                        clickCount?: number | null;
                    } | null): Article | null => {
                        if (!data) return null;
                        const createdStr = data.createdDate?.replace('Z', '') || '';
                        return {
                            id: String(data.id),
                            category: API_CATEGORY_MAP[data.categoryName] ?? 'Football',
                            translatedTitle: data.translatedTitle ?? data.title,
                            originalTitle: data.title,
                            source: data.publisherName || data.authorName || '',
                            publishedAt: createdStr,
                            thumbnail: data.imageUrl || '',
                            translatedContent: '',
                            originalContent: '',
                            clickCount: data.clickCount ?? 0,
                        };
                    })
            )
        )
            .then((results) => setRecentArticles(results.filter((a): a is Article => a != null)))
            .catch(() => setRecentArticles([]));
    }, [user, navigate]);

    useEffect(() => {
        const auth = getAuthHeader();
        if (!auth || !user) return;
        setBookmarkLoading(true);
        Promise.all([
            fetch('/api/bookmarks/me?size=50', { headers: auth }),
            fetch('/api/bookmarks/me/count', { headers: auth }),
        ])
            .then(([listRes, countRes]) => Promise.all([listRes.json(), countRes.json()]))
            .then(([pageData, countData]) => {
                const items = pageData?.content ?? [];
                const list = items.map((d: {
                    id: number;
                    title: string;
                    translatedTitle: string | null;
                    categoryName: string;
                    publisherName: string | null;
                    authorName: string | null;
                    imageUrl: string | null;
                    createdDate: string;
                    clickCount?: number | null;
                }) => ({
                    id: String(d.id),
                    category: API_CATEGORY_MAP[d.categoryName] ?? 'Football',
                    translatedTitle: d.translatedTitle ?? d.title,
                    originalTitle: d.title,
                    source: d.publisherName || d.authorName || '',
                    publishedAt: d.createdDate?.replace('Z', '') || '',
                    thumbnail: d.imageUrl || '',
                    translatedContent: '',
                    originalContent: '',
                    clickCount: d.clickCount ?? 0,
                }));
                setBookmarkArticles(list);
                setBookmarkCount(countData?.count ?? list.length);
            })
            .catch(() => {
                setBookmarkArticles([]);
                setBookmarkCount(0);
            })
            .finally(() => setBookmarkLoading(false));
    }, [user]);

    const removeBookmark = async (articleId: string) => {
        const auth = getAuthHeader();
        if (!auth) return;
        try {
            const res = await fetch(`/api/bookmarks/${articleId}`, { method: 'DELETE', headers: auth });
            if (res.ok) {
                setBookmarkArticles((prev) => prev.filter((a) => a.id !== articleId));
                setBookmarkCount((c) => Math.max(0, c - 1));
            }
        } catch {
            // ignore
        }
    };

    useEffect(() => {
        const auth = getAuthHeader();
        if (!auth || !user) return;
        setClicksLoading(true);
        fetch('/api/users/profile', { headers: { ...auth } })
            .then((res) => (res.ok ? res.json() : Promise.reject()))
            .then((data: ProfileData & { categoryClickCount?: CategoryClick[]; category_click_count?: CategoryClick[] }) => {
                // 프로필 데이터 설정
                setProfile({
                    nickname: data.nickname,
                    email: data.email,
                    introduction: data.introduction,
                    phoneNumber: data.phoneNumber,
                    profileImageUrl: data.profileImageUrl,
                    status: data.status,
                    role: data.role,
                    createdAt: data.createdAt,
                });
                // 카테고리 클릭 데이터 설정
                const list = data.categoryClickCount ?? data.category_click_count ?? [];
                setCategoryClicks(Array.isArray(list) ? list : []);
            })
            .catch(() => {
                setProfile(null);
                setCategoryClicks([]);
            })
            .finally(() => setClicksLoading(false));
    }, [user]);

    useEffect(() => {
        const auth = getAuthHeader();
        if (!auth || !user) return;
        setCoinHistoryLoading(true);
        fetch('/api/coins/history', { headers: { ...auth } })
            .then((res) => (res.ok ? res.json() : Promise.reject()))
            .then((data: CoinTransaction[]) => {
                setCoinHistory(Array.isArray(data) ? data : []);
            })
            .catch(() => setCoinHistory([]))
            .finally(() => setCoinHistoryLoading(false));
    }, [user]);


    const handleLogout = () => {
        logout();
        navigate('/');
    };

    if (!user) {
        return null;
    }

    return (
        <div className="min-h-screen bg-gray-50">
            <Header />

            <main className="max-w-4xl mx-auto px-4 py-8">
                <h1 className="text-3xl font-bold text-gray-900 mb-8">마이페이지</h1>

                <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6 mb-6">
                    <div className="flex items-center justify-between mb-4">
                        <h2 className="text-lg font-bold text-gray-900">계정 정보</h2>
                        <button
                            onClick={handleLogout}
                            className="px-4 py-2 text-sm font-medium text-gray-700 bg-gray-100 rounded-lg hover:bg-gray-200 transition-colors"
                        >
                            로그아웃
                        </button>
                    </div>

                    {/* 프로필 이미지 및 기본 정보 */}
                    <div className="flex items-center gap-4 mb-6 pb-4 border-b border-gray-100">
                        <div className="w-20 h-20 rounded-full overflow-hidden bg-gray-100 border-2 border-gray-200 flex-shrink-0">
                            {profile?.profileImageUrl ? (
                                <img
                                    src={profile.profileImageUrl}
                                    alt="프로필"
                                    className="w-full h-full object-cover"
                                />
                            ) : (
                                <div className="w-full h-full flex items-center justify-center">
                                    <User className="w-10 h-10 text-gray-400" />
                                </div>
                            )}
                        </div>
                        <div className="flex-1 min-w-0">
                            <p className="text-lg font-semibold text-gray-900 truncate">
                                {profile?.nickname || user.email?.split('@')[0] || '사용자'}
                            </p>
                            <p className="text-sm text-gray-500 truncate">{user.email}</p>
                            {profile?.introduction && (
                                <p className="text-sm text-gray-600 mt-1 line-clamp-2">{profile.introduction}</p>
                            )}
                        </div>
                    </div>

                    {/* 상세 정보 */}
                    <div className="grid grid-cols-2 gap-4 mb-4">
                        <div>
                            <p className="text-xs text-gray-500 mb-1">상태</p>
                            <span className={`inline-block px-2 py-1 text-xs font-medium rounded-full ${profile?.status === 'ACTIVE' ? 'bg-green-100 text-green-700' :
                                    profile?.status === 'SUSPENDED' ? 'bg-yellow-100 text-yellow-700' :
                                        'bg-gray-100 text-gray-700'
                                }`}>
                                {profile?.status === 'ACTIVE' ? '활성' : profile?.status === 'SUSPENDED' ? '정지' : profile?.status || '확인 중'}
                            </span>
                        </div>
                        <div>
                            <p className="text-xs text-gray-500 mb-1">역할</p>
                            <span className={`inline-block px-2 py-1 text-xs font-medium rounded-full ${profile?.role === 'ADMIN' ? 'bg-purple-100 text-purple-700' : 'bg-blue-100 text-blue-700'
                                }`}>
                                {profile?.role === 'ADMIN' ? '관리자' : '일반 사용자'}
                            </span>
                        </div>
                        {profile?.phoneNumber && (
                            <div>
                                <p className="text-xs text-gray-500 mb-1">연락처</p>
                                <p className="text-sm text-gray-900">{profile.phoneNumber}</p>
                            </div>
                        )}
                        {profile?.createdAt && (
                            <div>
                                <p className="text-xs text-gray-500 mb-1">가입일</p>
                                <p className="text-sm text-gray-900">
                                    {new Date(profile.createdAt).toLocaleDateString('ko-KR')}
                                </p>
                            </div>
                        )}
                    </div>

                    <div className="flex gap-3 pt-4 border-t border-gray-100">
                        <Link
                            to="/profile"
                            className="flex-1 px-4 py-3 text-center text-sm font-medium text-white bg-gray-900 rounded-lg hover:bg-gray-800 transition-colors"
                        >
                            프로필 수정
                        </Link>
                    </div>
                </div>

                <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6 mb-6">
                    <div className="flex items-center justify-between">
                        <div>
                            <h2 className="text-lg font-bold text-gray-900">관심사 설정</h2>
                            <p className="text-sm text-gray-500 mt-1">
                                관심 있는 선수, 팀, 리그를 구독하고 새 기사 알림을 받아보세요
                            </p>
                        </div>
                        <Link
                            to="/interests"
                            className="px-6 py-3 text-sm font-medium text-white bg-gray-900 rounded-lg hover:bg-gray-800 transition-colors"
                        >
                            설정하기
                        </Link>
                    </div>
                </div>

                <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6 mb-6">
                    <div className="mb-4">
                        <h2 className="text-lg font-bold text-gray-900">북마크 목록</h2>
                        <p className="text-xs text-gray-500 mt-1">총 {bookmarkCount}개</p>
                    </div>
                    {bookmarkLoading ? (
                        <p className="text-sm text-gray-500">불러오는 중...</p>
                    ) : bookmarkArticles.length === 0 ? (
                        <div className="text-center py-8">
                            <Bookmark className="w-10 h-10 text-gray-300 mx-auto mb-2" />
                            <p className="text-sm text-gray-500">북마크한 기사가 없습니다</p>
                        </div>
                    ) : (
                        <div className="space-y-4">
                            {bookmarkArticles.map((article) => (
                                <div
                                    key={article.id}
                                    className="flex items-center gap-3 bg-gray-50 rounded-lg border border-gray-100 p-4"
                                >
                                    <div className="flex-1 min-w-0">
                                        <ArticleCard article={article} compact />
                                    </div>
                                    <button
                                        type="button"
                                        onClick={() => removeBookmark(article.id)}
                                        title="북마크 해제"
                                        className="flex-shrink-0 p-2 rounded-lg text-gray-500 hover:bg-red-50 hover:text-red-600 transition-colors"
                                    >
                                        <X className="w-5 h-5" />
                                    </button>
                                </div>
                            ))}
                        </div>
                    )}
                </div>

                <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6 mb-6">
                    <h2 className="text-lg font-bold text-gray-900 mb-4">카테고리별 기사 클릭</h2>
                    {clicksLoading ? (
                        <p className="text-sm text-gray-500">불러오는 중...</p>
                    ) : categoryClicks.length === 0 ? (
                        <p className="text-sm text-gray-500">아직 클릭 기록이 없습니다.</p>
                    ) : (
                        <>
                            <div className="w-full max-w-sm mx-auto" style={{ height: 280 }}>
                                <ResponsiveContainer width="100%" height="100%">
                                    <PieChart width={300} height={280}>
                                        <Pie
                                            data={categoryClicks.map((item) => ({
                                                name: API_CATEGORY_MAP[item.categoryName] ?? item.categoryName,
                                                value: Number(item.count) || 0,
                                            }))}
                                            cx="50%"
                                            cy="50%"
                                            innerRadius={60}
                                            outerRadius={90}
                                            paddingAngle={2}
                                            dataKey="value"
                                            label={({ name, percent }) => `${name} ${((percent ?? 0) * 100).toFixed(0)}%`}
                                        >
                                            {categoryClicks.map((_, index) => (
                                                <Cell key={index} fill={PIE_COLORS[index % PIE_COLORS.length]} />
                                            ))}
                                        </Pie>
                                        <Tooltip formatter={(value: number | undefined) => `${value}회`} />
                                        <Legend />
                                    </PieChart>
                                </ResponsiveContainer>
                            </div>
                            <ul className="mt-4 space-y-1 text-sm text-gray-600 text-center">
                                {categoryClicks.map((item) => (
                                    <li key={item.categoryName}>
                                        {API_CATEGORY_MAP[item.categoryName] ?? item.categoryName} : {item.count}회
                                    </li>
                                ))}
                            </ul>
                        </>
                    )}
                </div>

                <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6 mb-6">
                    <h2 className="text-lg font-bold text-gray-900 mb-4">코인 사용 내역</h2>
                    {coinHistoryLoading ? (
                        <p className="text-sm text-gray-500">불러오는 중...</p>
                    ) : coinHistory.length === 0 ? (
                        <p className="text-sm text-gray-500">코인 내역이 없습니다.</p>
                    ) : (
                        <div className="space-y-3">
                            {coinHistory.map((tx) => {
                                const created = tx.createdAt ?? tx.createdDate;
                                const normalizedCreated = created && /[zZ]|[+-]\d{2}:\d{2}$/.test(created)
                                    ? created
                                    : (created ? `${created}Z` : null);
                                const createdLabel = normalizedCreated
                                    ? new Date(normalizedCreated).toLocaleString('ko-KR', { timeZone: 'Asia/Seoul' })
                                    : '-';
                                const typeLabel =
                                    tx.type === 'SPEND'
                                        ? '사용'
                                        : tx.type === 'CHARGE'
                                            ? '충전'
                                            : tx.type === 'ATTENDANCE_REWARD'
                                                ? '출석 보상'
                                                : tx.type === 'REFUND'
                                                    ? '회수'
                                                    : tx.type;
                                return (
                                    <div
                                        key={tx.id}
                                        className="flex flex-col gap-2 rounded-lg border border-gray-200 p-4 sm:flex-row sm:items-center sm:justify-between"
                                    >
                                        <div className="min-w-0">
                                            <p className="text-sm font-medium text-gray-900">{typeLabel}</p>
                                            <p className="text-xs text-gray-500">{createdLabel}</p>
                                        </div>
                                        <div className="text-right">
                                            <p className={`text-sm font-semibold ${tx.type === 'SPEND' || tx.type === 'REFUND' ? 'text-red-600' : 'text-green-600'}`}>
                                                {tx.type === 'SPEND' || tx.type === 'REFUND' ? '-' : '+'}
                                                {tx.amount}
                                            </p>
                                            <p className="text-xs text-gray-500">잔액 {tx.balanceAfter}</p>
                                        </div>
                                    </div>
                                );
                            })}
                        </div>
                    )}
                </div>

                <div>
                    <h2 className="text-lg font-bold text-gray-900 mb-4">최근 본 기사</h2>
                    {recentArticles.length > 0 ? (
                        <div className="space-y-4">
                            {recentArticles.map(article => (
                                <div key={article.id} className="bg-white rounded-lg border border-gray-200 p-4">
                                    <ArticleCard article={article} compact />
                                </div>
                            ))}
                        </div>
                    ) : (
                        <div className="bg-white rounded-lg border border-gray-200 p-12 text-center">
                            <p className="text-gray-500 mb-4">최근 본 기사가 없습니다.</p>
                            <Link
                                to="/"
                                className="inline-block px-6 py-2 bg-gray-900 text-white rounded-lg font-medium hover:bg-gray-800 transition-colors"
                            >
                                기사 둘러보기
                            </Link>
                        </div>
                    )}
                </div>
            </main>
        </div>
    );
}
