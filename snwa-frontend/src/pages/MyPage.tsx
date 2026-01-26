import { useEffect, useState } from 'react';
import { useNavigate, Link } from 'react-router';
import { useAuth } from '../contexts/AuthContext';
import Header from '../components/Header';
import ArticleCard from '../components/ArticleCard';
import { getArticleById, Article } from '../data/mockArticles';

export default function MyPage() {
  const { user, logout, updatePreferences } = useAuth();
  const navigate = useNavigate();
  const [selectedSports, setSelectedSports] = useState<string[]>([]);
  const [recentArticles, setRecentArticles] = useState<Article[]>([]);
  const [saved, setSaved] = useState(false);

  const sports = ['Football', 'Basketball', 'Baseball', 'Esports'];

  useEffect(() => {
    if (!user) {
      navigate('/login');
      return;
    }

    setSelectedSports(user.preferredSports || []);

    // Load recent viewed articles
    const viewedIds = JSON.parse(localStorage.getItem('snwa_viewed_articles') || '[]');
    const articles = viewedIds
      .map((id: string) => getArticleById(id))
      .filter((article: Article | undefined): article is Article => article !== undefined)
      .slice(0, 6);
    setRecentArticles(articles);
  }, [user, navigate]);

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

        {/* User Info */}
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

        {/* Preferred Sports */}
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

        {/* Recent Articles */}
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