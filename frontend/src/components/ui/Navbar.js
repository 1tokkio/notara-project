'use client';
import Link from 'next/link';
import { useRouter, usePathname } from 'next/navigation';
import { useAuth } from '@/context/AuthContext';

export default function Navbar() {
  const { user, logout } = useAuth();
  const router   = useRouter();
  const pathname = usePathname();

  const handleLogout = () => {
    logout();
    router.push('/login');
  };

  const navLinks = [
    { href: '/search',    label: '🔍 Buscar' },
    { href: '/dashboard', label: '📊 Mi progreso' },
  ];

  return (
    <nav className="sticky top-0 z-50 bg-black/80 backdrop-blur-md border-b border-white/5">
      <div className="max-w-6xl mx-auto px-4 h-16 flex items-center justify-between">

        {/* Logo */}
        <Link href="/search" className="flex items-center gap-2 font-bold text-lg">
          <span className="text-brand-green text-2xl">🎵</span>
          <span className="text-white">LinguaFlow</span>
        </Link>

        {/* Links */}
        <div className="hidden md:flex items-center gap-1">
          {navLinks.map((link) => (
            <Link
              key={link.href}
              href={link.href}
              className={`px-4 py-2 rounded-lg text-sm font-medium transition-colors ${
                pathname === link.href
                  ? 'bg-brand-green text-black'
                  : 'text-brand-text hover:text-white hover:bg-brand-hover'
              }`}
            >
              {link.label}
            </Link>
          ))}
        </div>

        {/* Usuario */}
        {user && (
          <div className="flex items-center gap-3">
            <span className="text-brand-text text-sm hidden md:block">
              {user.name}
            </span>
            <button
              onClick={handleLogout}
              className="text-brand-text hover:text-white text-sm transition-colors px-3 py-2 rounded-lg hover:bg-brand-hover"
            >
              Salir
            </button>
          </div>
        )}
      </div>
    </nav>
  );
}
