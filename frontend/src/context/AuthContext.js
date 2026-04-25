<<<<<<< HEAD
'use client';
import { createContext, useContext, useState, useEffect } from 'react';
import { auth } from '@/lib/api';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser]       = useState(null);
  const [loading, setLoading] = useState(true);

  // Al montar, verificar si hay sesión activa
  useEffect(() => {
    const stored = localStorage.getItem('user');
    const token  = localStorage.getItem('access_token');

    if (stored && token) {
      setUser(JSON.parse(stored));
    }
    setLoading(false);
  }, []);

  const login = async (email, password) => {
    const data = await auth.login(email, password);
    setUser(data.user);
    return data;
  };

  const register = async (name, email, password) => {
    const data = await auth.register(name, email, password);
    // Después del registro, hacer login automático
    return login(email, password);
  };

  const logout = () => {
    auth.logout();
=======
"use client";

import { createContext, useContext, useState } from "react";

const AuthContext = createContext(null);

const DEMO_USER = {
  id: "demouser",
  name: "demo user",
  email: "demo@linguaflow.com",
  password: "123456",
  avatar: "🎵",
  plan: "free",
};

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(false);

  const login = async (email, password) => {
    setLoading(true);
    await new Promise((r) => setTimeout(r, 800)); // simula delay de red

    try {
      if (email === DEMO_USER.email && password === DEMO_USER.password) {
        const { password: _, ...safeUser } = DEMO_USER;
        setUser(safeUser);
        return safeUser;
      }
      throw new Error("Credenciales incorrectas");
    } finally {
      setLoading(false);
    }
  };

  const logout = () => {
>>>>>>> origin/panxo
    setUser(null);
  };

  return (
<<<<<<< HEAD
    <AuthContext.Provider value={{ user, loading, login, register, logout }}>
=======
    <AuthContext.Provider value={{ user, setUser, loading, login, logout }}>
>>>>>>> origin/panxo
      {children}
    </AuthContext.Provider>
  );
}

<<<<<<< HEAD
export const useAuth = () => {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth debe usarse dentro de <AuthProvider>');
  return ctx;
};
=======
export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) throw new Error("useAuth must be used within an AuthProvider");
  return context;
}
>>>>>>> origin/panxo
