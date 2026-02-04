import { useEffect, useState } from 'react';
import { useNavigate, Link } from 'react-router';
import { useAuth } from '../contexts/AuthContext';
import Header from '../components/Header';
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

export default function MyPage() {
    const { user, logout, updatePreferences } = useAuth();
    const navigate = useNavigate();
    const [selectedSports, setSelectedSports] = useState<string[]>([]);
    const [recentArticles, setRecentArticles] = useState<Article[]>([]);
    const [saved, setSaved] = useState(false);
    const [categoryClicks, setCategoryClicks] = useState<CategoryClick[]>([]);
    const [clicksLoading, setClicksLoading] = useState(false);

    const sports = ['Football', 'Basketball', 'Baseball', 'Esports'];

    useEffect(() => {
        if (!user) {
            navigate('/login');
            return;
        }
        setSelectedSports(user.preferredSports || []);

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
        setClicksLoading(true);
        fetch('/api/users/profile', { headers: { ...auth } })
            .then((res) => (res.ok ? res.json() : Promise.reject()))
            .then((data: { categoryClickCount?: CategoryClick[]; category_click_count?: CategoryClick[] }) => {
                const list = data.categoryClickCount ?? data.category_click_count ?? [];
                setCategoryClicks(Array.isArray(list) ? list : []);
            })
            .catch(() => setCategoryClicks([]))
            .finally(() => setClicksLoading(false));
    }, [user]);

    const toggleSport = (sport: string) => {
        setSelectedSports(prev =>
            prev.includes(sport)
                ? prev.filter(s => s !== sport)
                : [...prev, sport]
        );
    };

    const handleSave = () => {
        updatePreferences(selectedSports);
        setSaved(true);
        setTimeout(() => setSaved(false), 2000);
    };

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
                    <h2 className="text-lg font-bold text-gray-900 mb-4">계정 정보</h2>
                    <div className="flex items-center justify-between">
                        <div>
                            <p className="text-sm text-gray-500 mb-1">이메일</p>
                            <p className="text-gray-900">{user.email}</p>
                        </div>
                        <button
                            onClick={handleLogout}
                            className="px-4 py-2 text-sm font-medium text-gray-700 bg-gray-100 rounded-lg hover:bg-gray-200 transition-colors"
                        >
                            로그아웃
                        </button>
                    </div>
                </div>

                <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6 mb-6">
                    <h2 className="text-lg font-bold text-gray-900 mb-4">선호 스포츠 설정</h2>
                    <p className="text-sm text-gray-500 mb-4">
                        관심 있는 스포츠를 선택하세요. (복수 선택 가능)
                    </p>
                    <div className="grid grid-cols-2 gap-3 mb-4">
                        {sports.map(sport => (
                            <button
                                key={sport}
                                onClick={() => toggleSport(sport)}
                                className={`px-4 py-3 rounded-lg border-2 font-medium transition-all ${
                                    selectedSports.includes(sport)
                                        ? 'border-gray-900 bg-gray-900 text-white'
                                        : 'border-gray-200 bg-white text-gray-700 hover:border-gray-300'
                                }`}
                            >
                                {sport}
                            </button>
                        ))}
                    </div>
                    <button
                        onClick={handleSave}
                        className="w-full bg-gray-900 text-white py-3 rounded-lg font-medium hover:bg-gray-800 transition-colors"
                    >
                        {saved ? '저장되었습니다 ✓' : '저장하기'}
                    </button>
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
                                        <Tooltip formatter={(value: number) => `${value}회`} />
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