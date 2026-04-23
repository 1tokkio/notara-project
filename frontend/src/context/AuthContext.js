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
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, setUser, loading, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) throw new Error("useAuth must be used within an AuthProvider");
  return context;
}
