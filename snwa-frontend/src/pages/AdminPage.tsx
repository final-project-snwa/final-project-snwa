import { useEffect, useState, useMemo } from 'react';
import { useNavigate } from 'react-router';
import Header from '../components/Header';
import { Users, FileText, ChevronRight, ChevronDown } from 'lucide-react';

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

/** 관리자용: 특정 사용자 결제 내역 한 건 */
type PaymentHistoryItem = {
  orderId: string;
  paymentKey: string;
  orderName: string;
  amount: number;
  method: string;
  status: string;
  approvedAt: string;
};

/** 관리자용: 사용자 결제 내역 응답 */
type PaymentHistoryResponse = {
  userId: number;
  items: PaymentHistoryItem[];
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
  const [managementOpen, setManagementOpen] = useState(true);
  const [users, setUsers] = useState<AdminUser[]>([]);
  const [articles, setArticles] = useState<AdminArticle[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [actionMessage, setActionMessage] = useState('');
  const [memberSearch, setMemberSearch] = useState('');
  const [articleSearch, setArticleSearch] = useState('');
  const [paymentModalOpen, setPaymentModalOpen] = useState(false);
  const [paymentModalUser, setPaymentModalUser] = useState<{ id: number; nickname: string | null } | null>(null);
  const [paymentHistory, setPaymentHistory] = useState<PaymentHistoryResponse | null>(null);
  const [paymentHistoryLoading, setPaymentHistoryLoading] = useState(false);

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

  const openPaymentHistory = async (user: AdminUser) => {
    const auth = getAuthHeader();
    if (!auth) {
      navigate('/login');
      return;
    }
    setPaymentModalUser({ id: user.id, nickname: user.nickname ?? null });
    setPaymentModalOpen(true);
    setPaymentHistory(null);
    setPaymentHistoryLoading(true);
    try {
      const res = await fetch(`/api/admin/users/${user.id}/payments`, { headers: { ...auth } });
      if (res.ok) {
        const data = (await res.json()) as PaymentHistoryResponse;
        setPaymentHistory(data);
      } else {
        setPaymentHistory({ userId: user.id, items: [] });
      }
    } catch {
      setPaymentHistory({ userId: user.id, items: [] });
    } finally {
      setPaymentHistoryLoading(false);
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

      <div className="flex" style={{ minHeight: 'calc(100vh - 4rem)', alignItems: 'stretch' }}>
        {/* Left Sidebar - 다우그룹 스타일 (어두운 배경, 검색창 없음) */}
        <aside
          className="flex-shrink-0 flex flex-col w-56"
          style={{ backgroundColor: '#1a1d24', height: '100%', minHeight: 'calc(100vh - 4rem)', position: 'sticky', top: '4rem' }}
        >
          {/* 상단: Site Admin + SNWA */}
          <div className="px-4 pt-5 pb-4" style={{ borderBottom: '1px solid rgba(255,255,255,0.1)' }}>
            <div className="text-xs font-semibold tracking-wide" style={{ color: '#ffffff' }}>
              Site Admin
            </div>
            <h2 className="text-lg font-bold mt-1.5" style={{ color: '#ffffff' }}>
              SNWA
            </h2>
          </div>

          {/* 네비게이션 - Management 섹션 */}
          <nav className="flex-1 overflow-y-auto pt-2">
            <div className="px-3">
              <button
                type="button"
                onClick={() => setManagementOpen(!managementOpen)}
                className="flex items-center justify-between w-full px-3 py-2 text-[11px] font-semibold uppercase tracking-widest rounded"
                style={{ color: '#9ca3af' }}
              >
                <span>Management</span>
                {managementOpen ? (
                  <ChevronDown className="w-4 h-4" style={{ color: '#9ca3af' }} />
                ) : (
                  <ChevronRight className="w-4 h-4" style={{ color: '#9ca3af' }} />
                )}
              </button>

              {managementOpen && (
                <div className="mt-1 pl-1" style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
                  <button
                    type="button"
                    onClick={() => setSection('users')}
                    className="flex items-center gap-2.5 w-full px-3 py-2.5 rounded"
                    style={{
                      backgroundColor: section === 'users' ? '#0ea5e9' : 'transparent',
                      fontWeight: section === 'users' ? 500 : 400,
                    }}
                  >
                    <Users className="w-5 h-5 flex-shrink-0" style={{ color: '#ffffff' }} />
                    <span style={{ color: '#ffffff', fontSize: '15px' }}>전체 회원 조회</span>
                  </button>
                  <button
                    type="button"
                    onClick={() => setSection('articles')}
                    className="flex items-center gap-2.5 w-full px-3 py-2.5 rounded"
                    style={{
                      backgroundColor: section === 'articles' ? '#0ea5e9' : 'transparent',
                      fontWeight: section === 'articles' ? 500 : 400,
                    }}
                  >
                    <FileText className="w-5 h-5 flex-shrink-0" style={{ color: '#ffffff' }} />
                    <span style={{ color: '#ffffff', fontSize: '15px' }}>전체 게시물 조회</span>
                  </button>
                </div>
              )}
            </div>
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
                              <th className="text-left px-6 py-3 font-medium">결제내역</th>
                            </tr>
                          </thead>
                          <tbody className="divide-y divide-gray-200">
                            {filteredUsers.length === 0 ? (
                              <tr>
                                <td colSpan={8} className="px-6 py-8 text-center text-gray-500">
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
                                  <td className="px-6 py-3">
                                    <button
                                      type="button"
                                      onClick={() => openPaymentHistory(u)}
                                      className="px-3 py-1.5 text-sm font-medium rounded-md border border-gray-300 text-gray-700 bg-white hover:bg-gray-50"
                                    >
                                      결제내역
                                    </button>
                                  </td>
                                </tr>
                              ))
                            )}
                          </tbody>
                        </table>
                      </div>
                    </div>

                    {/* 결제 내역 모달 */}
                    {paymentModalOpen && (
                      <div
                        className="fixed inset-0 z-50 flex items-center justify-center bg-black/50"
                        onClick={() => setPaymentModalOpen(false)}
                        role="dialog"
                        aria-modal="true"
                        aria-labelledby="payment-modal-title"
                      >
                        <div
                          className="bg-white rounded-lg shadow-xl max-w-2xl w-full mx-4 max-h-[80vh] flex flex-col"
                          onClick={(e) => e.stopPropagation()}
                        >
                          <div className="p-4 border-b border-gray-200 flex items-center justify-between">
                            <h3 id="payment-modal-title" className="text-lg font-semibold text-gray-900">
                              결제 내역 {paymentModalUser?.nickname ? `- ${paymentModalUser.nickname}` : ''}
                            </h3>
                            <button
                              type="button"
                              onClick={() => setPaymentModalOpen(false)}
                              className="p-1 rounded text-gray-500 hover:bg-gray-100"
                              aria-label="닫기"
                            >
                              ✕
                            </button>
                          </div>
                          <div className="p-4 overflow-auto flex-1">
                            {paymentHistoryLoading ? (
                              <div className="py-8 text-center text-gray-500">불러오는 중...</div>
                            ) : paymentHistory && paymentHistory.items.length === 0 ? (
                              <div className="py-8 text-center text-gray-500">결제 내역이 없습니다.</div>
                            ) : paymentHistory ? (
                              <div className="overflow-x-auto">
                                <table className="w-full text-sm">
                                  <thead className="bg-gray-50 text-gray-600 border-b border-gray-200">
                                    <tr>
                                      <th className="text-left px-4 py-2 font-medium">결제일시</th>
                                      <th className="text-left px-4 py-2 font-medium">주문명(코인)</th>
                                      <th className="text-right px-4 py-2 font-medium">결제금액</th>
                                    </tr>
                                  </thead>
                                  <tbody className="divide-y divide-gray-200">
                                    {paymentHistory.items.map((item) => (
                                      <tr key={item.paymentKey} className="text-gray-900">
                                        <td className="px-4 py-2 whitespace-nowrap">
                                          {item.approvedAt
                                            ? new Date(item.approvedAt).toLocaleString('ko-KR')
                                            : '-'}
                                        </td>
                                        <td className="px-4 py-2">{item.orderName || '-'}</td>
                                        <td className="px-4 py-2 text-right">
                                          {item.amount != null ? `${item.amount.toLocaleString()}원` : '-'}
                                        </td>
                                      </tr>
                                    ))}
                                  </tbody>
                                </table>
                              </div>
                            ) : null}
                          </div>
                        </div>
                      </div>
                    )}
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
