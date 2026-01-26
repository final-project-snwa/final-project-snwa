import { createContext, useContext, useState, ReactNode, useEffect } from 'react';

interface User {
  email: string;
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

  // Load user from localStorage on mount
  useEffect(() => {
    const savedUser = localStorage.getItem('snwa_user');
    if (savedUser) {
      setUser(JSON.parse(savedUser));
    }
  }, []);

  const login = async (email: string, password: string): Promise<boolean> => {
    // Mock login - check if user exists in localStorage
    const users = JSON.parse(localStorage.getItem('snwa_users') || '{}');
    
    if (users[email] && users[email].password === password) {
      const userData = {
        email,
        preferredSports: users[email].preferredSports || [],
      };
      setUser(userData);
      localStorage.setItem('snwa_user', JSON.stringify(userData));
      return true;
    }
    return false;
  };

  const signup = async (email: string, password: string): Promise<boolean> => {
    // Mock signup
    const users = JSON.parse(localStorage.getItem('snwa_users') || '{}');
    
    if (users[email]) {
      return false; // User already exists
    }
    
    users[email] = { password, preferredSports: [] };
    localStorage.setItem('snwa_users', JSON.stringify(users));
    
    const userData = {
      email,
      preferredSports: [],
    };
    setUser(userData);
    localStorage.setItem('snwa_user', JSON.stringify(userData));
    return true;
  };

  const logout = () => {
    setUser(null);
    localStorage.removeItem('snwa_user');
  };

  const updatePreferences = (sports: string[]) => {
    if (user) {
      const updatedUser = { ...user, preferredSports: sports };
      setUser(updatedUser);
      localStorage.setItem('snwa_user', JSON.stringify(updatedUser));
      
      // Update in users database
      const users = JSON.parse(localStorage.getItem('snwa_users') || '{}');
      if (users[user.email]) {
        users[user.email].preferredSports = sports;
        localStorage.setItem('snwa_users', JSON.stringify(users));
      }
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
