'use client';

import { createContext, useContext, useEffect, useState, ReactNode } from 'react';
import { CurrentUser, authApi } from './api';

interface AuthContextType {
  user: CurrentUser | null;
  loading: boolean;
  login: (token: string, user: CurrentUser) => void;
  logout: () => void;
  isManager: () => boolean;
  isItAdmin: () => boolean;
}

const AuthContext = createContext<AuthContextType | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<CurrentUser | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const token = localStorage.getItem('deskflow_token');
    const stored = localStorage.getItem('deskflow_user');
    if (token && stored) {
      setUser(JSON.parse(stored));
    }
    setLoading(false);
  }, []);

  const login = (token: string, userData: CurrentUser) => {
    localStorage.setItem('deskflow_token', token);
    localStorage.setItem('deskflow_user', JSON.stringify(userData));
    setUser(userData);
  };

  const logout = () => {
    localStorage.removeItem('deskflow_token');
    localStorage.removeItem('deskflow_user');
    setUser(null);
    window.location.href = '/login';
  };

  const isManager = () => user?.role === 'MANAGER' || user?.role === 'IT_ADMIN';
  const isItAdmin = () => user?.role === 'IT_ADMIN';

  return (
    <AuthContext.Provider value={{ user, loading, login, logout, isManager, isItAdmin }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
}
