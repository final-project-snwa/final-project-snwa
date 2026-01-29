import { createContext, useContext, useState, ReactNode, useEffect } from 'react';

interface User {
  email: string;
  nickname?: string;
  preferredSports: string[];
}

interface AuthContextType {
  user: User | null;
  login: (email: string, password: string) => Promise<boolean>;
  signup: (email: string, password: string) => Promise<boolean>;
  logout: () => void;
  updatePreferences: (sports: string[]) => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null);

  // Load user from localStorage on mount (requires token validation)
  useEffect(() => {
    const token = localStorage.getItem('snwa_token');
    const savedUser = localStorage.getItem('snwa_user');

    // 토큰이 없으면 로그인 상태로 복원하지 않음
    if (!token || !savedUser) {
      localStorage.removeItem('snwa_user');
      localStorage.removeItem('snwa_token');
      return;
    }

    // 토큰 유효성 검증
    (async () => {
      try {
        const response = await fetch('/api/auth/verify', {
          headers: { Authorization: `Bearer ${token}` },
        });

        if (!response.ok) {
          // 토큰이 유효하지 않으면 삭제
          localStorage.removeItem('snwa_user');
          localStorage.removeItem('snwa_token');
          return;
        }

        // 토큰이 유효하면 사용자 정보 복원
        const userData = JSON.parse(savedUser);
        setUser(userData);
      } catch (e) {
        console.error('Token validation failed:', e);
        localStorage.removeItem('snwa_user');
        localStorage.removeItem('snwa_token');
      }
    })();
  }, []);

  const login = async (email: string, password: string): Promise<boolean> => {
    try {
      const response = await fetch('/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, password }),
      });

      const contentType = response.headers.get('content-type');
      let data: { token?: string } = {};
      if (contentType && contentType.includes('application/json')) {
        const text = await response.text();
        if (text) data = JSON.parse(text);
      }

      if (!response.ok || !data.token) return false;

      localStorage.setItem('snwa_token', data.token);
      const userData: User = { email, preferredSports: [] };
      localStorage.setItem('snwa_user', JSON.stringify(userData));
      setUser(userData);
      return true;
    } catch {
      return false;
    }
  };

  const signup = async (email: string, password: string): Promise<boolean> => {
    try {
      const response = await fetch('/api/auth/signup', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, password }),
      });
      return response.ok;
    } catch {
      return false;
    }
  };

  const logout = () => {
    setUser(null);
    localStorage.removeItem('snwa_user');
    localStorage.removeItem('snwa_token');
  };

  const updatePreferences = (sports: string[]) => {
    if (user) {
      const updatedUser = { ...user, preferredSports: sports };
      setUser(updatedUser);
      localStorage.setItem('snwa_user', JSON.stringify(updatedUser));
    }
  };

  return (
    <AuthContext.Provider value={{ user, login, signup, logout, updatePreferences }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}
