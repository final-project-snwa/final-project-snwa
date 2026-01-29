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

  // Load user from sessionStorage on mount (탭/창 닫으면 로그아웃됨)
  useEffect(() => {
    const token = sessionStorage.getItem('snwa_token');
    const savedUser = sessionStorage.getItem('snwa_user');

    if (!token || !savedUser) {
      sessionStorage.removeItem('snwa_user');
      return;
    }

    try {
      setUser(JSON.parse(savedUser));
    } catch (e) {
      console.error('Failed to parse user data:', e);
      sessionStorage.removeItem('snwa_user');
    }
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

      sessionStorage.setItem('snwa_token', data.token);
      const userData: User = { email, preferredSports: [] };
      sessionStorage.setItem('snwa_user', JSON.stringify(userData));
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
    sessionStorage.removeItem('snwa_user');
    sessionStorage.removeItem('snwa_token');
  };

  const updatePreferences = (sports: string[]) => {
    if (user) {
      const updatedUser = { ...user, preferredSports: sports };
      setUser(updatedUser);
      sessionStorage.setItem('snwa_user', JSON.stringify(updatedUser));
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
