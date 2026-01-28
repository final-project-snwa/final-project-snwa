import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router';
import Header from '../components/Header';

type AdminUser = {
  id: number;
  email: string;
  nickname: string;
  status: 'ACTIVE' | 'INACTIVE' | 'DELETE';
  role: 'USER' | 'ADMIN';
  emailVerified: boolean;
  createdDate: string;
  updatedDate: string;
};

type AdminArticle = {
  id: number;
  title: string;
  userNickname: string;
  createdDate: string;
};

function getAuthHeader() {
  const token = localStorage.getItem('snwa_token');
  if (!token) return null;
  return { Authorization: `Bearer ${token}` };
}

export default function AdminPage() {
  const navigate = useNavigate();
  const [tab, setTab] = useState<'users' | 'articles'>('users');
  const [users, setUsers] = useState<AdminUser[]>([]);
  const [articles, setArticles] = useState<AdminArticle[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const auth = getAuthHeader();
    if (!auth) {
      navigate('/login');
      return;
    }

    (async () => {
      setLoading(true);
      setError('');
      try {
        const [usersRes, articlesRes] = await Promise.all([
          fetch('/api/admin/users', { headers: { ...auth } }),
          fetch('/api/admin/articles', { headers: { ...auth } }),
        ]);

        if (usersRes.status === 401 || usersRes.status === 403 || articlesRes.status === 401 || articlesRes.status === 403) {
          setError('관리자 권한이 없습니다.');
          setLoading(false);
          return;
        }

        const usersJson = usersRes.ok ? await usersRes.json() : [];
        const articlesJson = articlesRes.ok ? await articlesRes.json() : [];
        setUsers(usersJson);
        setArticles(articlesJson);
      } catch (e) {
        setError(e instanceof Error ? e.message : '불러오기 실패');
      } finally {
        setLoading(false);
      }
    })();
  }, [navigate]);

  return (
    <div className="min-h-screen bg-gray-50">
      <Header />

      <main className="max-w-6xl mx-auto px-4 py-8">
        <div className="flex items-center justify-between mb-6">
          <h1 className="text-3xl font-bold text-gray-900">관리자 페이지</h1>
        </div>

        <div className="flex gap-2 mb-6">
          <button
            onClick={() => setTab('users')}
            className={`px-4 py-2 rounded-lg text-sm font-medium border ${
              tab === 'users' ? 'bg-gray-900 text-white border-gray-900' : 'bg-white text-gray-700 border-gray-200'
            }`}
          >
            전체 회원
          </button>
          <button
            onClick={() => setTab('articles')}
            className={`px-4 py-2 rounded-lg text-sm font-medium border ${
              tab === 'articles' ? 'bg-gray-900 text-white border-gray-900' : 'bg-white text-gray-700 border-gray-200'
            }`}
          >
            전체 글
          </button>
        </div>

        {loading && <div className="text-sm text-gray-600">불러오는 중...</div>}
        {error && <div className="text-sm text-red-600 bg-red-50 px-3 py-2 rounded">{error}</div>}

        {!loading && !error && tab === 'users' && (
          <div className="bg-white rounded-lg border border-gray-200 overflow-hidden">
            <div className="px-6 py-4 border-b border-gray-200">
              <h2 className="text-lg font-bold text-gray-900">전체 회원 목록</h2>
            </div>
            <div className="overflow-x-auto">
              <table className="min-w-full text-sm">
                <thead className="bg-gray-50 text-gray-600">
                  <tr>
                    <th className="text-left px-6 py-3">ID</th>
                    <th className="text-left px-6 py-3">이메일</th>
                    <th className="text-left px-6 py-3">닉네임</th>
                    <th className="text-left px-6 py-3">상태</th>
                    <th className="text-left px-6 py-3">인증</th>
                    <th className="text-left px-6 py-3">가입일</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-200">
                  {users.map((u) => (
                    <tr key={u.id} className="text-gray-900">
                      <td className="px-6 py-3">{u.id}</td>
                      <td className="px-6 py-3">{u.email}</td>
                      <td className="px-6 py-3">{u.nickname}</td>
                      <td className="px-6 py-3">{u.status}</td>
                      <td className="px-6 py-3">{u.emailVerified ? 'Y' : 'N'}</td>
                      <td className="px-6 py-3">{new Date(u.createdDate).toLocaleString()}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}

        {!loading && !error && tab === 'articles' && (
          <div className="bg-white rounded-lg border border-gray-200 overflow-hidden">
            <div className="px-6 py-4 border-b border-gray-200">
              <h2 className="text-lg font-bold text-gray-900">전체 글 목록</h2>
            </div>
            <div className="overflow-x-auto">
              <table className="min-w-full text-sm">
                <thead className="bg-gray-50 text-gray-600">
                  <tr>
                    <th className="text-left px-6 py-3">ID</th>
                    <th className="text-left px-6 py-3">제목</th>
                    <th className="text-left px-6 py-3">작성자</th>
                    <th className="text-left px-6 py-3">등록일</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-200">
                  {articles.map((a) => (
                    <tr key={a.id} className="text-gray-900">
                      <td className="px-6 py-3">{a.id}</td>
                      <td className="px-6 py-3">{a.title}</td>
                      <td className="px-6 py-3">{a.userNickname}</td>
                      <td className="px-6 py-3">{new Date(a.createdDate).toLocaleString()}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}
      </main>
    </div>
  );
}

