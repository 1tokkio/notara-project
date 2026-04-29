'use client';
import { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useAuth } from '../context/AuthContext';

export default function Home() {
  const { user, loading } = useAuth();
  const router = useRouter();

  useEffect(() => {
    if (!loading) {
      router.replace(user ? '/search' : '/login');
    }
  }, [user, loading, router]);

  return (
    <div className="min-h-screen bg-brand-dark flex items-center justify-center">
      <div className="flex flex-col items-center gap-4">
        <div className="w-12 h-12 rounded-full border-4 border-brand-green border-t-transparent animate-spin" />
        <p className="text-brand-text text-sm">Cargando LinguaFlow...</p>
      </div>
    </div>
  );
}
