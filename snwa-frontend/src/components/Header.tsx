import { Link, useNavigate } from 'react-router';
import { useAuth } from '../contexts/AuthContext';
import { User, LogOut, Settings, Coins, Bell, Trophy } from 'lucide-react';
import { LevelBadge } from './LevelBadge';
import { useState, useRef, useEffect } from 'react';

interface HeaderProps {
  showCategories?: boolean;
  selectedCategory?: string;
  onCategoryChange?: (category: string) => void;
}

export default function Header({ showCategories = false, selectedCategory, onCategoryChange }: HeaderProps) {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [showDropdown, setShowDropdown] = useState(false);
  const dropdownRef = useRef<HTMLDivElement>(null);
  const [unreadCount, setUnreadCount] = useState(0);
  const [userLevel, setUserLevel] = useState<number | null>(null);
  const eventSourceRef = useRef<EventSource | null>(null);

  const categories = ['All', 'Football', 'Basketball', 'Baseball', 'Esports'];

  useEffect(() => {
    function handleClickOutside(event: MouseEvent) {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target as Node)) {
        setShowDropdown(false);
      }
    }

    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  useEffect(() => {
    if (!user) {
      setUnreadCount(0);
      setUserLevel(null);
      if (eventSourceRef.current) {
        eventSourceRef.current.close();
        eventSourceRef.current = null;
      }
      return;
    }

    const token = sessionStorage.getItem('snwa_token');
    if (!token) return;

    fetch('/api/exp/me', { headers: { Authorization: `Bearer ${token}` } })
      .then((res) => (res.ok ? res.json() : null))
      .then((data) => {
        if (data && typeof data.level === 'number') setUserLevel(data.level);
        else setUserLevel(null);
      })
      .catch(() => setUserLevel(null));

    fetch('/api/notifications/unread/count', {
      headers: { Authorization: `Bearer ${token}` },
    })
      .then((res) => (res.ok ? res.json() : null))
      .then((data) => {
        if (data && typeof data.unreadCount === 'number') {
          setUnreadCount(data.unreadCount);
        }
      })
      .catch(() => setUnreadCount(0));

    const es = new EventSource(`/api/notifications/subscribe?token=${encodeURIComponent(token)}`);
    eventSourceRef.current = es;

    const handleNotification = (event: MessageEvent) => {
      try {
        const data = JSON.parse(event.data);
        if (data && data.notificationId) {
          setUnreadCount((c) => c + 1);
        }
      } catch {
        // ignore
      }
    };

    es.addEventListener('notification', handleNotification);
    es.onerror = () => {
      es.close();
      eventSourceRef.current = null;
    };

    return () => {
      es.removeEventListener('notification', handleNotification);
      es.close();
      eventSourceRef.current = null;
    };
  }, [user]);

  const handleLogout = () => {
    logout();
    setShowDropdown(false);
    navigate('/');
  };

  return (
    <header className="sticky top-0 z-50 bg-white border-b border-gray-200">
      <div className="max-w-6xl mx-auto px-4">
        <div className="flex items-center justify-between h-16">
          {/* Logo */}
          <Link to="/" className="text-2xl font-bold text-gray-900 tracking-tight">
            SNWA
          </Link>

          {/* Categories */}
          {showCategories && (
            <nav className="hidden md:flex items-center gap-8">
              {categories.map(category => (
                <button
                  key={category}
                  onClick={() => onCategoryChange?.(category)}
                  className={`text-sm transition-colors ${
                    selectedCategory === category
                      ? 'text-gray-900 font-medium'
                      : 'text-gray-500 hover:text-gray-900'
                  }`}
                >
                  {category}
                </button>
              ))}
            </nav>
          )}

          {/* Auth Section */}
          <div className="flex items-center gap-4">
            {user ? (
              <>
                {user.email !== 'admin@snwa.com' && unreadCount > 0 && (
                  <div className="relative inline-flex items-center justify-center w-7 h-7 rounded-full bg-red-500 text-white text-[11px] font-semibold">
                    {unreadCount > 99 ? '99+' : unreadCount}
                  </div>
                )}
                <div className="relative" ref={dropdownRef}>
                  <button
                    onClick={() => setShowDropdown(!showDropdown)}
                    className="flex items-center gap-2 px-3 py-2 rounded-lg hover:bg-gray-50 transition-colors"
                  >
                    <div className="w-8 h-8 rounded-full bg-gray-900 flex items-center justify-center">
                      <User className="w-4 h-4 text-white" />
                    </div>
                    {userLevel != null && (
                      <LevelBadge level={userLevel} className="hidden sm:inline-block" />
                    )}
                    <span className="text-sm text-gray-700 hidden sm:inline">
                      {user.nickname || user.email.split('@')[0]}
                    </span>
                  </button>

                  {showDropdown && (
                    <div className="absolute right-0 mt-2 w-48 bg-white rounded-lg shadow-lg border border-gray-200 py-1">
                      {user.email !== 'admin@snwa.com' && (
                        <>
                          <Link
                            to="/leaderboard"
                            onClick={() => setShowDropdown(false)}
                            className="flex items-center gap-2 px-4 py-2 text-sm text-gray-700 hover:bg-gray-50"
                          >
                            <Trophy className="w-4 h-4" />
                            랭킹
                          </Link>
                          <Link
                            to="/notifications"
                            onClick={() => setShowDropdown(false)}
                            className="flex items-center gap-2 px-4 py-2 text-sm text-gray-700 hover:bg-gray-50"
                          >
                            <Bell className="w-4 h-4" />
                            알림
                          </Link>
                          <Link
                            to="/mypage"
                            onClick={() => setShowDropdown(false)}
                            className="flex items-center gap-2 px-4 py-2 text-sm text-gray-700 hover:bg-gray-50"
                          >
                            <Settings className="w-4 h-4" />
                            마이페이지
                          </Link>
                          <Link
                            to="/coins"
                            onClick={() => setShowDropdown(false)}
                            className="flex items-center gap-2 px-4 py-2 text-sm text-gray-700 hover:bg-gray-50"
                          >
                            <Coins className="w-4 h-4" />
                            코인 구매
                          </Link>
                        </>
                      )}
                      {user.email === 'admin@snwa.com' && (
                        <>
                          <Link
                            to="/leaderboard"
                            onClick={() => setShowDropdown(false)}
                            className="flex items-center gap-2 px-4 py-2 text-sm text-gray-700 hover:bg-gray-50"
                          >
                            <Trophy className="w-4 h-4" />
                            랭킹
                          </Link>
                          <Link
                            to="/admin"
                            onClick={() => setShowDropdown(false)}
                            className="flex items-center gap-2 px-4 py-2 text-sm text-gray-700 hover:bg-gray-50"
                          >
                            <Settings className="w-4 h-4" />
                            관리자 페이지
                          </Link>
                        </>
                      )}
                      <button
                        onClick={handleLogout}
                        className="flex items-center gap-2 px-4 py-2 text-sm text-gray-700 hover:bg-gray-50 w-full text-left"
                      >
                        <LogOut className="w-4 h-4" />
                        로그아웃
                      </button>
                    </div>
                  )}
                </div>
              </>
            ) : (
              <Link
                to="/login"
                className="px-4 py-2 text-sm font-medium text-gray-900 hover:text-gray-600 transition-colors"
              >
                Login
              </Link>
            )}
          </div>
        </div>

        {/* Mobile Categories */}
        {showCategories && (
          <div className="md:hidden border-t border-gray-200 py-3 overflow-x-auto">
            <div className="flex gap-4 min-w-max">
              {categories.map(category => (
                <button
                  key={category}
                  onClick={() => onCategoryChange?.(category)}
                  className={`text-sm whitespace-nowrap transition-colors ${
                    selectedCategory === category
                      ? 'text-gray-900 font-medium'
                      : 'text-gray-500'
                  }`}
                >
                  {category}
                </button>
              ))}
            </div>
          </div>
        )}
      </div>
    </header>
  );
}
