import { useEffect, useMemo, useState } from 'react';
import { Link, useNavigate } from 'react-router';
import { Bell, CheckCircle2, Trash2 } from 'lucide-react';
import Header from '../components/Header';
import { useAuth } from '../contexts/AuthContext';

type NotificationItem = {
  notificationId: number;
  message: string;
  isRead: boolean;
  createdDate: string;
  articleId?: number | null;
  articleTitle?: string | null;
};

type PageResponse<T> = {
  content: T[];
  number: number;
  totalPages: number;
  last: boolean;
};

type NotificationSettingResponse = {
  enableNotification: boolean;
};

function getAuthHeader(): Record<string, string> | null {
  const token = sessionStorage.getItem('snwa_token');
  if (!token) return null;
  return { Authorization: `Bearer ${token}` };
}

function formatKst(dateString: string): string {
  if (!dateString) return '-';
  const normalized = /[zZ]|[+-]\d{2}:\d{2}$/.test(dateString) ? dateString : `${dateString}Z`;
  return new Date(normalized).toLocaleString('ko-KR', { timeZone: 'Asia/Seoul' });
}

export default function NotificationsPage() {
  const { user } = useAuth();
  const navigate = useNavigate();

  const [tab, setTab] = useState<'all' | 'unread'>('all');
  const [notifications, setNotifications] = useState<NotificationItem[]>([]);
  const [page, setPage] = useState(0);
  const [hasNext, setHasNext] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const [settings, setSettings] = useState<NotificationSettingResponse | null>({ enableNotification: true });
  const [settingsLoading, setSettingsLoading] = useState(false);
  const [settingsError, setSettingsError] = useState<string | null>(null);
  const [unreadCount, setUnreadCount] = useState<number>(0);

  const authHeader = useMemo(() => getAuthHeader(), []);

  useEffect(() => {
    if (!user) {
      navigate('/login');
    }
  }, [user, navigate]);

  const fetchSettings = async () => {
    if (!authHeader) return;
    setSettingsLoading(true);
    setSettingsError(null);
    try {
      const res = await fetch('/api/notifications/settings', { headers: { ...authHeader } });
      if (!res.ok) {
        const body = await res.json().catch(() => ({}));
        setSettingsError(body.message ?? '알림 설정을 불러오지 못했습니다.');
        setSettings(null);
        return;
      }
      const data = (await res.json()) as NotificationSettingResponse;
      setSettings(data);
    } catch (e) {
      setSettingsError('알림 설정을 불러오지 못했습니다.');
      setSettings(null);
    } finally {
      setSettingsLoading(false);
    }
  };

  const fetchUnreadCount = async () => {
    if (!authHeader) return;
    try {
      const res = await fetch('/api/notifications/unread/count', { headers: { ...authHeader } });
      if (!res.ok) return;
      const data = (await res.json()) as { unreadCount: number };
      setUnreadCount(Number(data.unreadCount) || 0);
    } catch {
      setUnreadCount(0);
    }
  };

  const fetchNotifications = async (nextPage = 0, mode: 'all' | 'unread' = tab) => {
    if (!authHeader) return;
    setLoading(true);
    setError(null);
    try {
      const endpoint =
        mode === 'unread'
          ? `/api/notifications/unread?page=${nextPage}&size=20`
          : `/api/notifications?page=${nextPage}&size=20`;
      const res = await fetch(endpoint, { headers: { ...authHeader } });
      if (!res.ok) {
        const body = await res.json().catch(() => ({}));
        setError(body.message ?? '알림을 불러오지 못했습니다.');
        setNotifications((prev) => (nextPage === 0 ? [] : prev));
        return;
      }
      const data = (await res.json()) as PageResponse<NotificationItem>;
      setNotifications((prev) => (nextPage === 0 ? data.content : [...prev, ...data.content]));
      setPage(data.number);
      setHasNext(!data.last && data.number + 1 < data.totalPages);
    } catch (e) {
      setError('알림을 불러오지 못했습니다.');
    } finally {
      setLoading(false);
    }
  };

  const handleToggleSetting = async () => {
    if (!authHeader || !settings) return;
    try {
      const res = await fetch('/api/notifications/settings', {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json', ...authHeader },
        body: JSON.stringify({ enableNotification: !settings.enableNotification }),
      });
      if (!res.ok) {
        const body = await res.json().catch(() => ({}));
        setSettingsError(body.message ?? '알림 설정 변경에 실패했습니다.');
        return;
      }
      const data = (await res.json()) as NotificationSettingResponse;
      setSettings(data);
      setSettingsError(null);
      if (data.enableNotification) {
        fetchNotifications(0, tab);
        fetchUnreadCount();
      } else {
        setNotifications([]);
        setUnreadCount(0);
      }
    } catch {
      setSettingsError('알림 설정 변경에 실패했습니다.');
    }
  };

  const handleRead = async (id: number) => {
    if (!authHeader) return;
    try {
      const res = await fetch(`/api/notifications/${id}/read`, {
        method: 'PATCH',
        headers: { ...authHeader },
      });
      if (!res.ok) return;
      setNotifications((prev) =>
        tab === 'unread' ? prev.filter((n) => n.notificationId !== id) : prev.map((n) => (n.notificationId === id ? { ...n, isRead: true } : n))
      );
      setUnreadCount((c) => Math.max(0, c - 1));
    } catch {
      // ignore
    }
  };

  const handleReadAll = async () => {
    if (!authHeader) return;
    try {
      const res = await fetch('/api/notifications/read-all', {
        method: 'PATCH',
        headers: { ...authHeader },
      });
      if (!res.ok) return;
      setNotifications((prev) => (tab === 'unread' ? [] : prev.map((n) => ({ ...n, isRead: true }))));
      setUnreadCount(0);
    } catch {
      // ignore
    }
  };

  const handleDelete = async (id: number, wasUnread: boolean) => {
    if (!authHeader) return;
    try {
      const res = await fetch(`/api/notifications/${id}`, {
        method: 'DELETE',
        headers: { ...authHeader },
      });
      if (!res.ok) return;
      setNotifications((prev) => prev.filter((n) => n.notificationId !== id));
      if (wasUnread) {
        setUnreadCount((c) => Math.max(0, c - 1));
      }
    } catch {
      // ignore
    }
  };

  useEffect(() => {
    if (!user) return;
    fetchSettings();
    fetchUnreadCount();
  }, [user]);

  useEffect(() => {
    if (!user) return;
    fetchNotifications(0, tab);
  }, [user, tab]);


  if (!user) return null;

  return (
    <div className="min-h-screen bg-gray-50">
      <Header />
      <main className="max-w-4xl mx-auto px-4 py-8">
        <div className="flex items-center justify-between mb-6">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-full bg-gray-900 text-white flex items-center justify-center">
              <Bell className="w-5 h-5" />
            </div>
            <div>
              <h1 className="text-2xl font-bold text-gray-900">알림</h1>
              <p className="text-sm text-gray-500">새 기사 알림을 확인하세요</p>
            </div>
          </div>
          <button
            type="button"
            onClick={handleReadAll}
            className="px-4 py-2 text-sm font-medium text-gray-700 bg-gray-100 rounded-lg hover:bg-gray-200 transition-colors"
          >
            모두 읽음
          </button>
        </div>

        <div className="bg-white rounded-lg border border-gray-200 p-4 mb-6">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm font-medium text-gray-900">알림 수신</p>
              <p className="text-xs text-gray-500">관심사 기반 새 기사 알림을 받아볼 수 있어요.</p>
            </div>
            <button
              type="button"
              onClick={handleToggleSetting}
              disabled={settingsLoading}
              className={`relative inline-flex h-7 w-12 items-center rounded-full transition-colors ${
                settings?.enableNotification ? 'bg-gray-900' : 'bg-gray-300'
              } ${settingsLoading ? 'opacity-60 cursor-not-allowed' : ''}`}
            >
              <span
                className={`inline-block h-5 w-5 transform rounded-full bg-white transition-transform ${
                  settings?.enableNotification ? 'translate-x-6' : 'translate-x-1'
                }`}
              />
            </button>
          </div>
          {settingsError && <p className="text-xs text-red-600 mt-2">{settingsError}</p>}
        </div>

        <div className="flex items-center gap-2 mb-4">
          <button
            type="button"
            onClick={() => setTab('all')}
            className={`px-3 py-1.5 text-sm rounded-full ${
              tab === 'all' ? 'bg-gray-900 text-white' : 'bg-gray-100 text-gray-600'
            }`}
          >
            전체
          </button>
          <button
            type="button"
            onClick={() => setTab('unread')}
            className={`px-3 py-1.5 text-sm rounded-full ${
              tab === 'unread' ? 'bg-gray-900 text-white' : 'bg-gray-100 text-gray-600'
            }`}
          >
            읽지 않음 {unreadCount > 0 && <span className="ml-1 text-xs">({unreadCount})</span>}
          </button>
        </div>

        {loading && notifications.length === 0 && (
          <div className="bg-white rounded-lg border border-gray-200 p-12 text-center text-sm text-gray-500">
            알림을 불러오는 중...
          </div>
        )}

        {!loading && error && (
          <div className="bg-white rounded-lg border border-gray-200 p-12 text-center text-sm text-gray-500">
            {error}
          </div>
        )}

        {!loading && !error && notifications.length === 0 && (
          <div className="bg-white rounded-lg border border-gray-200 p-12 text-center text-sm text-gray-500">
            아직 알림이 없습니다.
          </div>
        )}

        <div className="space-y-3">
          {notifications.map((n) => (
            <div
              key={n.notificationId}
              className={`bg-white rounded-lg border p-4 flex gap-4 items-start ${
                n.isRead ? 'border-gray-200' : 'border-gray-900'
              }`}
            >
              <div className={`mt-1 h-2.5 w-2.5 rounded-full ${n.isRead ? 'bg-gray-300' : 'bg-gray-900'}`} />
              <div className="flex-1 min-w-0">
                <div className="flex items-center gap-2 mb-1">
                  <p className="text-sm font-medium text-gray-900">{n.message}</p>
                  {!n.isRead && (
                    <span className="text-xs font-semibold text-gray-900 bg-gray-100 px-2 py-0.5 rounded-full">
                      NEW
                    </span>
                  )}
                </div>
                {n.articleId && (
                  <Link to={`/articles/${n.articleId}`} className="text-xs text-gray-500 hover:underline">
                    {n.articleTitle ?? `기사 #${n.articleId}`}
                  </Link>
                )}
                <p className="text-xs text-gray-400 mt-1">{formatKst(n.createdDate)}</p>
              </div>
              <div className="flex items-center gap-2">
                {!n.isRead && (
                  <button
                    type="button"
                    onClick={() => handleRead(n.notificationId)}
                    className="text-gray-500 hover:text-gray-900"
                    title="읽음 처리"
                  >
                    <CheckCircle2 className="w-4 h-4" />
                  </button>
                )}
                <button
                  type="button"
                  onClick={() => handleDelete(n.notificationId, !n.isRead)}
                  className="text-gray-400 hover:text-gray-700"
                  title="삭제"
                >
                  <Trash2 className="w-4 h-4" />
                </button>
              </div>
            </div>
          ))}
        </div>

        {hasNext && (
          <div className="flex justify-center mt-6">
            <button
              type="button"
              onClick={() => fetchNotifications(page + 1, tab)}
              className="px-4 py-2 text-sm font-medium text-gray-700 bg-gray-100 rounded-lg hover:bg-gray-200 transition-colors"
            >
              더 보기
            </button>
          </div>
        )}
      </main>
    </div>
  );
}
