import { useEffect, useState, useMemo } from 'react';
import { useNavigate, Link } from 'react-router';
import { useAuth } from '../contexts/AuthContext';
import { Users, FileText, LogOut, Home, Search, Trash2 } from 'lucide-react';

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
  const { logout } = useAuth();
  const [menu, setMenu] = useState<'users' | 'articles'>('users');
  const [users, setUsers] = useState<AdminUser[]>([]);
  const [articles, setArticles] = useState<AdminArticle[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [searchQuery, setSearchQuery] = useState('');
  const [deleteTarget, setDeleteTarget] = useState<{ type: 'user' | 'article'; id: number; name: string } | null>(null);
  const [editingStatus, setEditingStatus] = useState<number | null>(null);

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

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  // 검색 필터링
  const filteredUsers = useMemo(() => {
    if (!searchQuery.trim()) return users;
    const query = searchQuery.toLowerCase();
    return users.filter(u => 
      u.email.toLowerCase().includes(query) ||
      u.nickname.toLowerCase().includes(query) ||
      u.id.toString().includes(query)
    );
  }, [users, searchQuery]);

  const filteredArticles = useMemo(() => {
    if (!searchQuery.trim()) return articles;
    const query = searchQuery.toLowerCase();
    return articles.filter(a => 
      a.title.toLowerCase().includes(query) ||
      a.userNickname.toLowerCase().includes(query) ||
      a.id.toString().includes(query)
    );
  }, [articles, searchQuery]);

  // 상태 변경 함수
  const handleStatusChange = async (userId: number, newStatus: 'ACTIVE' | 'INACTIVE' | 'DELETE') => {
    const auth = getAuthHeader();
    if (!auth) return;

    try {
      const response = await fetch(`/api/admin/users/${userId}`, {
        method: 'PATCH',
        headers: {
          ...auth,
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ status: newStatus }),
      });

      if (response.ok) {
        // 목록 새로고침
        const usersRes = await fetch('/api/admin/users', { headers: { ...auth } });
        if (usersRes.ok) {
          const usersJson = await usersRes.json();
          setUsers(usersJson);
        }
        setEditingStatus(null);
      } else {
        setError('상태 변경에 실패했습니다.');
      }
    } catch (e) {
      setError('상태 변경 중 오류가 발생했습니다.');
    }
  };

  // 삭제 함수
  const handleDelete = async (type: 'user' | 'article', id: number) => {
    const auth = getAuthHeader();
    if (!auth) return;

    try {
      const endpoint = type === 'user' ? `/api/admin/users/${id}` : `/api/admin/articles/${id}`;
      const response = await fetch(endpoint, {
        method: 'DELETE',
        headers: { ...auth },
      });

      if (response.ok) {
        // 목록 새로고침
        const [usersRes, articlesRes] = await Promise.all([
          fetch('/api/admin/users', { headers: { ...auth } }),
          fetch('/api/admin/articles', { headers: { ...auth } }),
        ]);

        if (usersRes.ok) {
          const usersJson = await usersRes.json();
          setUsers(usersJson);
        }
        if (articlesRes.ok) {
          const articlesJson = await articlesRes.json();
          setArticles(articlesJson);
        }
        setDeleteTarget(null);
      } else {
        setError('삭제에 실패했습니다.');
      }
    } catch (e) {
      setError('삭제 중 오류가 발생했습니다.');
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 flex">
      {/* 왼쪽 사이드바 */}
      <aside className="w-64 bg-gray-900 text-white flex flex-col">
        {/* 헤더 */}
        <div className="p-6 border-b border-gray-800">
          <div className="text-xl font-bold">SNWA</div>
          <p className="text-sm text-gray-400 mt-1">관리자 페이지</p>
        </div>

        {/* 메뉴 - 헤더 바로 아래 */}
        <nav className="p-4">
          <div className="space-y-2">
            <button
              onClick={() => setMenu('users')}
              className={`w-full flex items-center gap-3 px-4 py-3 rounded-lg text-sm font-medium transition-colors ${
                menu === 'users'
                  ? 'bg-blue-600 text-white'
                  : 'text-gray-300 hover:bg-gray-800 hover:text-white'
              }`}
            >
              <Users className="w-5 h-5" />
              전체 회원 조회
            </button>
            <button
              onClick={() => setMenu('articles')}
              className={`w-full flex items-center gap-3 px-4 py-3 rounded-lg text-sm font-medium transition-colors ${
                menu === 'articles'
                  ? 'bg-blue-600 text-white'
                  : 'text-gray-300 hover:bg-gray-800 hover:text-white'
              }`}
            >
              <FileText className="w-5 h-5" />
              전체 게시물 조회
            </button>
          </div>
        </nav>
      </aside>

      {/* 메인 콘텐츠 영역 */}
      <main className="flex-1 overflow-auto">
        <div className="max-w-7xl mx-auto p-8">
          {/* 상단 헤더 (홈으로, 로그아웃 버튼) */}
          <div className="flex items-center justify-between mb-6">
            <div>
              <h1 className="text-2xl font-bold text-gray-900">
                {menu === 'users' ? '전체 회원 조회' : '전체 게시물 조회'}
              </h1>
              <p className="text-sm text-gray-500 mt-1">
                {menu === 'users' 
                  ? `총 ${users.length}명의 회원이 등록되어 있습니다.`
                  : `총 ${articles.length}개의 게시물이 등록되어 있습니다.`
                }
              </p>
            </div>
            <div className="flex items-center gap-3">
              <Link
                to="/"
                className="flex items-center gap-2 px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors"
              >
                <Home className="w-4 h-4" />
                홈으로
              </Link>
              <button
                onClick={handleLogout}
                className="flex items-center gap-2 px-4 py-2 text-sm font-medium text-white bg-gray-900 rounded-lg hover:bg-gray-800 transition-colors"
              >
                <LogOut className="w-4 h-4" />
                로그아웃
              </button>
            </div>
          </div>

          {/* 검색 바 */}
          {!loading && !error && (
            <div className="mb-6">
              <div className="relative">
                <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-5 h-5 pointer-events-none" />
                <input
                  type="text"
                  placeholder={menu === 'users' ? '이메일, 닉네임, ID로 검색...' : '제목, 작성자, ID로 검색...'}
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                  className="w-full pl-12 pr-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-gray-900 focus:border-transparent"
                />
              </div>
            </div>
          )}

          {/* 로딩/에러 */}
          {loading && (
            <div className="bg-white rounded-lg border border-gray-200 p-8 text-center">
              <div className="text-gray-600">데이터를 불러오는 중...</div>
            </div>
          )}

          {error && (
            <div className="bg-red-50 border border-red-200 rounded-lg p-4 text-red-600">
              {error}
            </div>
          )}

          {/* 회원 목록 */}
          {!loading && !error && menu === 'users' && (
            <div className="bg-white rounded-lg border border-gray-200 overflow-hidden shadow-sm">
              <div className="px-6 py-4 border-b border-gray-200 bg-gray-50">
                <h2 className="text-lg font-semibold text-gray-900">
                  회원 목록 {searchQuery && `(${filteredUsers.length}개 검색 결과)`}
                </h2>
              </div>
              <div className="overflow-x-auto">
                <table className="min-w-full divide-y divide-gray-200">
                  <thead className="bg-gray-50">
                    <tr>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">ID</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">이메일</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">닉네임</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">상태</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">역할</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">이메일 인증</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">가입일</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">작업</th>
                    </tr>
                  </thead>
                  <tbody className="bg-white divide-y divide-gray-200">
                    {filteredUsers.length === 0 ? (
                      <tr>
                        <td colSpan={8} className="px-6 py-8 text-center text-gray-500">
                          {searchQuery ? '검색 결과가 없습니다.' : '등록된 회원이 없습니다.'}
                        </td>
                      </tr>
                    ) : (
                      filteredUsers.map((u) => (
                        <tr key={u.id} className="hover:bg-gray-50">
                          <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{u.id}</td>
                          <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{u.email}</td>
                          <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{u.nickname}</td>
                          <td className="px-6 py-4 whitespace-nowrap">
                            {editingStatus === u.id ? (
                              <select
                                value={u.status}
                                onChange={(e) => {
                                  const newStatus = e.target.value as 'ACTIVE' | 'INACTIVE' | 'DELETE';
                                  handleStatusChange(u.id, newStatus);
                                }}
                                onBlur={() => setEditingStatus(null)}
                                className="px-2 py-1 text-xs font-medium rounded-full border border-gray-300 focus:outline-none focus:ring-2 focus:ring-blue-500"
                                autoFocus
                              >
                                <option value="ACTIVE">ACTIVE</option>
                                <option value="INACTIVE">INACTIVE</option>
                                <option value="DELETE">DELETE</option>
                              </select>
                            ) : (
                              <button
                                onClick={() => setEditingStatus(u.id)}
                                className={`px-2 py-1 text-xs font-medium rounded-full cursor-pointer hover:opacity-80 transition-opacity ${
                                  u.status === 'ACTIVE' ? 'bg-green-100 text-green-800' :
                                  u.status === 'INACTIVE' ? 'bg-yellow-100 text-yellow-800' :
                                  'bg-red-100 text-red-800'
                                }`}
                                title="클릭하여 상태 변경"
                              >
                                {u.status}
                              </button>
                            )}
                          </td>
                          <td className="px-6 py-4 whitespace-nowrap">
                            <span className={`px-2 py-1 text-xs font-medium rounded-full ${
                              u.role === 'ADMIN' ? 'bg-purple-100 text-purple-800' : 'bg-blue-100 text-blue-800'
                            }`}>
                              {u.role}
                            </span>
                          </td>
                          <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                            {u.emailVerified ? (
                              <span className="text-green-600 font-medium">인증완료</span>
                            ) : (
                              <span className="text-gray-400">미인증</span>
                            )}
                          </td>
                          <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                            {new Date(u.createdDate).toLocaleString('ko-KR')}
                          </td>
                          <td className="px-6 py-4 whitespace-nowrap">
                            <button
                              onClick={() => setDeleteTarget({ type: 'user', id: u.id, name: u.email })}
                              className="text-red-600 hover:text-red-800 transition-colors"
                              title="삭제"
                            >
                              <Trash2 className="w-4 h-4" />
                            </button>
                          </td>
                        </tr>
                      ))
                    )}
                  </tbody>
                </table>
              </div>
            </div>
          )}

          {/* 게시물 목록 */}
          {!loading && !error && menu === 'articles' && (
            <div className="bg-white rounded-lg border border-gray-200 overflow-hidden shadow-sm">
              <div className="px-6 py-4 border-b border-gray-200 bg-gray-50">
                <h2 className="text-lg font-semibold text-gray-900">
                  게시물 목록 {searchQuery && `(${filteredArticles.length}개 검색 결과)`}
                </h2>
              </div>
              <div className="overflow-x-auto">
                <table className="min-w-full divide-y divide-gray-200">
                  <thead className="bg-gray-50">
                    <tr>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">ID</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">제목</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">작성자</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">등록일</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">작업</th>
                    </tr>
                  </thead>
                  <tbody className="bg-white divide-y divide-gray-200">
                    {filteredArticles.length === 0 ? (
                      <tr>
                        <td colSpan={5} className="px-6 py-8 text-center text-gray-500">
                          {searchQuery ? '검색 결과가 없습니다.' : '등록된 게시물이 없습니다.'}
                        </td>
                      </tr>
                    ) : (
                      filteredArticles.map((a) => (
                        <tr key={a.id} className="hover:bg-gray-50">
                          <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{a.id}</td>
                          <td className="px-6 py-4 text-sm text-gray-900">{a.title}</td>
                          <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{a.userNickname || '알 수 없음'}</td>
                          <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                            {new Date(a.createdDate).toLocaleString('ko-KR')}
                          </td>
                          <td className="px-6 py-4 whitespace-nowrap">
                            <button
                              onClick={() => setDeleteTarget({ type: 'article', id: a.id, name: a.title })}
                              className="text-red-600 hover:text-red-800 transition-colors"
                              title="삭제"
                            >
                              <Trash2 className="w-4 h-4" />
                            </button>
                          </td>
                        </tr>
                      ))
                    )}
                  </tbody>
                </table>
              </div>
            </div>
          )}

          {/* 삭제 확인 모달 */}
          {deleteTarget && (
            <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50" onClick={() => setDeleteTarget(null)}>
              <div className="bg-white rounded-lg p-6 max-w-md w-full mx-4 shadow-xl" onClick={(e) => e.stopPropagation()}>
                <h3 className="text-lg font-bold text-gray-900 mb-4">삭제 확인</h3>
                <p className="text-gray-700 mb-6">
                  {deleteTarget.type === 'user' 
                    ? `정말로 회원 "${deleteTarget.name}"을(를) 삭제하시겠습니까?`
                    : `정말로 게시물 "${deleteTarget.name}"을(를) 삭제하시겠습니까?`
                  }
                  <br />
                  <span className="text-sm text-gray-500">
                    {deleteTarget.type === 'user' 
                      ? '삭제된 회원은 로그인할 수 없게 됩니다.'
                      : '삭제된 게시물은 다른 사용자에게 보이지 않게 됩니다.'
                    }
                  </span>
                </p>
                <div className="flex gap-3 justify-end">
                  <button
                    onClick={() => setDeleteTarget(null)}
                    className="px-4 py-2 text-sm font-medium text-gray-700 bg-gray-100 rounded-lg hover:bg-gray-200 transition-colors"
                  >
                    취소
                  </button>
                  <button
                    onClick={() => {
                      handleDelete(deleteTarget.type, deleteTarget.id);
                    }}
                    className="px-4 py-2 text-sm font-medium text-white bg-red-600 rounded-lg hover:bg-red-700 transition-colors"
                  >
                    확인
                  </button>
                </div>
              </div>
            </div>
          )}
        </div>
      </main>
    </div>
  );
}

