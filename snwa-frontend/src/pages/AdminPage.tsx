import { useEffect, useState, useMemo } from 'react';
import { useNavigate } from 'react-router';
import Header from '../components/Header';
import { Users, FileText } from 'lucide-react';

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
  const token = sessionStorage.getItem('snwa_token');
  if (!token) return null;
  return { Authorization: `Bearer ${token}` };
}

type Section = 'users' | 'articles';

export default function AdminPage() {
  const navigate = useNavigate();
  const [section, setSection] = useState<Section>('users');
  const [users, setUsers] = useState<AdminUser[]>([]);
  const [articles, setArticles] = useState<AdminArticle[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [actionMessage, setActionMessage] = useState('');
  const [memberSearch, setMemberSearch] = useState('');
  const [articleSearch, setArticleSearch] = useState('');

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

  const filteredUsers = useMemo(() => {
    if (!memberSearch.trim()) return users;
    const q = memberSearch.trim().toLowerCase();
    return users.filter(
      (u) =>
        u.email.toLowerCase().includes(q) ||
        (u.nickname ?? '').toLowerCase().includes(q) ||
        String(u.id).includes(q) ||
        u.status.toLowerCase().includes(q) ||
        (u.role ?? '').toLowerCase().includes(q)
    );
  }, [users, memberSearch]);

  const filteredArticles = useMemo(() => {
    if (!articleSearch.trim()) return articles;
    const q = articleSearch.trim().toLowerCase();
    return articles.filter(
      (a) =>
        a.title.toLowerCase().includes(q) ||
        (a.userNickname ?? '').toLowerCase().includes(q) ||
        String(a.id).includes(q)
    );
  }, [articles, articleSearch]);

  const statusLabel: Record<string, string> = {
    ACTIVE: '정상',
    INACTIVE: '정지',
    DELETE: '삭제',
  };

  const updateUserStatus = async (userId: number, nextStatus: 'ACTIVE' | 'INACTIVE') => {
    const auth = getAuthHeader();
    if (!auth) {
      navigate('/login');
      return;
    }

    setError('');
    setActionMessage('');
    try {
      const res = await fetch(`/api/admin/users/${userId}`, {
        method: 'PATCH',
        headers: {
          ...auth,
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ status: nextStatus }),
      });

      if (res.status === 401 || res.status === 403) {
        setError('관리자 권한이 없습니다.');
        return;
      }

      if (!res.ok) {
        const text = await res.text();
        throw new Error(text || '상태 변경 실패');
      }

      const updated = (await res.json()) as AdminUser;
      setUsers((prev) => prev.map((u) => (u.id === updated.id ? updated : u)));
      setActionMessage('회원 상태가 변경되었습니다.');
      window.setTimeout(() => setActionMessage(''), 2000);
    } catch (e) {
      setError(e instanceof Error ? e.message : '상태 변경 실패');
    }
  };

  const deleteArticle = async (articleId: number) => {
    const auth = getAuthHeader();
    if (!auth) {
      navigate('/login');
      return;
    }

    setError('');
    setActionMessage('');
    try {
      const res = await fetch(`/api/admin/articles/${articleId}`, {
        method: 'DELETE',
        headers: { ...auth },
      });

      if (res.status === 401 || res.status === 403) {
        setError('관리자 권한이 없습니다.');
        return;
      }

      if (!res.ok) {
        const text = await res.text();
        throw new Error(text || '게시물 삭제 실패');
      }

      setArticles((prev) => prev.filter((a) => a.id !== articleId));
      setActionMessage('게시물이 삭제되었습니다.');
      window.setTimeout(() => setActionMessage(''), 2000);
    } catch (e) {
      setError(e instanceof Error ? e.message : '게시물 삭제 실패');
    }
  };

  return (
    <div className="min-h-screen bg-gray-100">
      <Header />

      <div className="flex min-h-[calc(100vh-4rem)]">
        {/* Left Sidebar - 너비 고정·전체 높이 같은 진한 회색 */}
        <aside
          className="flex-shrink-0 w-full"
          style={{
            display: 'flex',
            flexDirection: 'column',
            backgroundColor: '#282828',
            color: '#f9fafb',
            width: '22rem',
            minWidth: '22rem',
            minHeight: 'calc(100vh - 4rem)',
          }}
        >
          {/* 상단: Site Admin → SNWA Admin Page (세로 배치) */}
          <div
            className="p-4 flex-shrink-0 w-full flex flex-col"
            style={{ backgroundColor: '#282828' }}
          >
            <div className="text-xs tracking-widest uppercase" style={{ color: '#d1d5db' }}>
              Site Admin
            </div>
            <h2 className="text-lg font-bold mt-1" style={{ color: '#ffffff' }}>
              SNWA Admin Page
            </h2>
          </div>
          {/* 그 아래: Management + 메뉴 (세로 배치) */}
          <nav
            className="py-3 flex-1 w-full flex flex-col"
            style={{ backgroundColor: '#282828' }}
          >
            <div
              className="px-3 text-xs font-medium uppercase tracking-wider mb-2"
              style={{ color: '#d1d5db' }}
            >
              Management
            </div>
            <button
              onClick={() => setSection('users')}
              className="flex items-center gap-3 w-full px-4 py-3 text-left border-l-4"
              style={
                section === 'users'
                  ? { backgroundColor: '#00a6ed', color: '#fff', borderColor: '#00a6ed' }
                  : { backgroundColor: '#282828', color: '#f3f4f6', borderColor: 'transparent' }
              }
            >
              <Users className="w-5 h-5 flex-shrink-0" />
              전체 회원 조회
            </button>
            <button
              onClick={() => setSection('articles')}
              className="flex items-center gap-3 w-full px-4 py-3 text-left border-l-4"
              style={
                section === 'articles'
                  ? { backgroundColor: '#00a6ed', color: '#fff', borderColor: '#00a6ed' }
                  : { backgroundColor: '#282828', color: '#f3f4f6', borderColor: 'transparent' }
              }
            >
              <FileText className="w-5 h-5 flex-shrink-0" />
              전체 게시물 조회
            </button>
          </nav>
        </aside>

        <main className="flex-1 p-4 sm:p-6 overflow-auto">
          <div className="max-w-6xl mx-auto">
            <div className="flex items-center gap-2 text-sm text-gray-600 mb-4">
              <span>관리</span>
              <span>/</span>
              <span className="font-medium text-gray-900">
                {section === 'users' ? '전체 회원 조회' : '전체 게시물 조회'}
              </span>
            </div>

            {actionMessage && (
              <div className="mb-4 text-sm text-emerald-700 bg-emerald-50 px-4 py-3 rounded-lg border border-emerald-200">
                {actionMessage}
              </div>
            )}

            {error && (
              <div className="mb-4 text-sm text-red-600 bg-red-50 px-4 py-3 rounded-lg border border-red-200">
                {error}
              </div>
            )}

            {loading ? (
              <div className="text-gray-600">불러오는 중...</div>
            ) : (
              <>
                {section === 'users' && (
                  <>
                    <div className="mb-4 flex items-center gap-4 flex-nowrap">
                      <div className="text-gray-700 whitespace-nowrap shrink-0">
                        <span className="font-medium text-gray-900">현재 회원 수</span>
                        <span className="ml-2 font-semibold">{filteredUsers.length}명</span>
                        {memberSearch && (
                          <span className="ml-2 text-sm text-gray-500">
                            (검색 / 전체 {users.length}명)
                          </span>
                        )}
                      </div>
                      <div className="min-w-0 flex-1 max-w-[240px]">
                        <input
                          type="text"
                          placeholder="이메일, 닉네임, 상태로 검색..."
                          value={memberSearch}
                          onChange={(e) => setMemberSearch(e.target.value)}
                          className="w-full pl-3 pr-3 py-2 text-sm border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-gray-900 focus:border-transparent"
                        />
                      </div>
                    </div>
                    <div className="bg-white rounded-lg border border-gray-200 shadow-sm">
                      <div className="overflow-x-auto">
                        <table className="min-w-[980px] w-full text-sm">
                          <thead className="bg-gray-50 text-gray-600 border-b border-gray-200">
                            <tr>
                              <th className="text-left px-6 py-3 font-medium">ID</th>
                              <th className="text-left px-6 py-3 font-medium">이메일</th>
                              <th className="text-left px-6 py-3 font-medium">닉네임</th>
                              <th className="text-left px-6 py-3 font-medium">역할</th>
                              <th className="text-left px-6 py-3 font-medium">계정상태</th>
                              <th className="text-left px-6 py-3 font-medium">이메일인증</th>
                              <th className="text-left px-6 py-3 font-medium whitespace-nowrap">가입일</th>
                            </tr>
                          </thead>
                          <tbody className="divide-y divide-gray-200">
                            {filteredUsers.length === 0 ? (
                              <tr>
                                <td colSpan={7} className="px-6 py-8 text-center text-gray-500">
                                  {memberSearch ? '검색 결과가 없습니다.' : '등록된 회원이 없습니다.'}
                                </td>
                              </tr>
                            ) : (
                              filteredUsers.map((u) => (
                                <tr key={u.id} className="text-gray-900 hover:bg-gray-50">
                                  <td className="px-6 py-3">{u.id}</td>
                                  <td className="px-6 py-3">{u.email}</td>
                                  <td className="px-6 py-3">{u.nickname ?? '-'}</td>
                                  <td className="px-6 py-3">{u.role ?? 'USER'}</td>
                                  <td className="px-6 py-3">
                                    {u.status === 'DELETE' ? (
                                      <span className="text-gray-500">{statusLabel[u.status] ?? u.status}</span>
                                    ) : (
                                      <select
                                        value={u.status}
                                        onChange={(e) =>
                                          updateUserStatus(u.id, e.target.value as 'ACTIVE' | 'INACTIVE')
                                        }
                                        className="text-sm border border-gray-300 rounded-md px-2 py-1 bg-white focus:outline-none focus:ring-2 focus:ring-gray-900"
                                        aria-label="계정상태 변경"
                                      >
                                        <option value="ACTIVE">정상</option>
                                        <option value="INACTIVE">정지</option>
                                      </select>
                                    )}
                                  </td>
                                  <td className="px-6 py-3">{u.emailVerified ? 'Y' : 'N'}</td>
                                  <td className="px-6 py-3 whitespace-nowrap">
                                    {new Date(u.createdDate).toLocaleString('ko-KR')}
                                  </td>
                                </tr>
                              ))
                            )}
                          </tbody>
                        </table>
                      </div>
                    </div>
                  </>
                )}

                {section === 'articles' && (
                  <>
                    <div className="mb-4 flex items-center gap-4 flex-nowrap">
                      <div className="text-gray-700 whitespace-nowrap shrink-0">
                        <span className="font-medium text-gray-900">현재 게시물 수</span>
                        <span className="ml-2 font-semibold">{filteredArticles.length}건</span>
                        {articleSearch && (
                          <span className="ml-2 text-sm text-gray-500">
                            (검색 / 전체 {articles.length}건)
                          </span>
                        )}
                      </div>
                      <div className="min-w-0 flex-1 max-w-[240px]">
                        <input
                          type="text"
                          placeholder="제목, 작성자로 검색..."
                          value={articleSearch}
                          onChange={(e) => setArticleSearch(e.target.value)}
                          className="w-full pl-3 pr-3 py-2 text-sm border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-gray-900 focus:border-transparent"
                        />
                      </div>
                    </div>
                    <div className="bg-white rounded-lg border border-gray-200 shadow-sm">
                      <div className="overflow-x-auto">
                        <table className="min-w-[980px] w-full text-sm">
                          <thead className="bg-gray-50 text-gray-600 border-b border-gray-200">
                            <tr>
                              <th className="text-left px-6 py-3 font-medium">ID</th>
                              <th className="text-left px-6 py-3 font-medium">제목</th>
                              <th className="text-left px-6 py-3 font-medium">작성자</th>
                              <th className="text-left px-6 py-3 font-medium whitespace-nowrap">등록일</th>
                              <th className="text-left px-6 py-3 font-medium">관리</th>
                            </tr>
                          </thead>
                          <tbody className="divide-y divide-gray-200">
                            {filteredArticles.length === 0 ? (
                              <tr>
                                <td colSpan={5} className="px-6 py-8 text-center text-gray-500">
                                  {articleSearch ? '검색 결과가 없습니다.' : '등록된 게시물이 없습니다.'}
                                </td>
                              </tr>
                            ) : (
                              filteredArticles.map((a) => (
                                <tr key={a.id} className="text-gray-900 hover:bg-gray-50">
                                  <td className="px-6 py-3">{a.id}</td>
                                  <td className="px-6 py-3">{a.title}</td>
                                  <td className="px-6 py-3">{a.userNickname ?? '-'}</td>
                                  <td className="px-6 py-3 whitespace-nowrap">
                                    {new Date(a.createdDate).toLocaleString('ko-KR')}
                                  </td>
                                  <td className="px-6 py-3">
                                    <button
                                      onClick={() => deleteArticle(a.id)}
                                      className="px-3 py-1.5 text-sm font-medium rounded-md border border-red-200 text-red-700 bg-red-50 hover:bg-red-100"
                                    >
                                      삭제
                                    </button>
                                  </td>
                                </tr>
                              ))
                            )}
                          </tbody>
                        </table>
                      </div>
                    </div>
                  </>
                )}
              </>
            )}
          </div>
        </main>
      </div>
    </div>
  );
}
