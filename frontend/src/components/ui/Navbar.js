'use client';
import Link from 'next/link';
import { useRouter, usePathname } from 'next/navigation';
import { useAuth } from '../../context/AuthContext';

const styles = {
  nav:          'sticky top-0 z-50 bg-brand-dark/90 backdrop-blur-md border-b border-white/5',
  inner:        'max-w-none px-5 h-14 flex items-center gap-6',
  logo:         'flex items-center gap-2 font-bold text-sm mr-2',
  dot:          'w-2 h-2 rounded-full bg-brand-green',
  logoText:     'text-white font-semibold',
  navLinks:     'flex items-center gap-1',
  linkActive:   'px-3 py-1.5 text-sm font-medium text-white',
  linkInactive: 'px-3 py-1.5 text-sm font-medium text-brand-text hover:text-white transition-colors',
  spacer:       'flex-1',
  rightArea:    'flex items-center gap-3',
  badge:        'bg-brand-green text-black text-xs font-semibold px-3 py-1 rounded-full',
  avatar:       'w-8 h-8 rounded-full bg-brand-purple flex items-center justify-center text-white text-xs font-bold',
  logoutBtn:    'text-brand-text hover:text-white text-sm transition-colors',
};

const navLinks = [
  { href: '/search',    label: 'Buscar',      match: '/search' },
  { href: '/dashboard', label: 'Mi progreso', match: '/dashboard' },
];

function getInitials(name = '') {
  return name.split(' ').slice(0, 2).map(w => w[0]).join('').toUpperCase() || 'U';
}

export default function Navbar({ lessonBadge }) {
  const { user, logout } = useAuth();
  const router   = useRouter();
  const pathname = usePathname();

  const handleLogout = () => {
    logout();
    router.push('/login');
  };

  const isOnLesson = pathname?.startsWith('/lesson');

  return (
    <nav className={styles.nav}>
      <div className={styles.inner}>

        <Link href="/search" className={styles.logo}>
          <span className={styles.dot} />
          <span className={styles.logoText}>Notara</span>
        </Link>

        <div className={styles.navLinks}>
          {isOnLesson && (
            <span className={styles.linkActive}>Lección</span>
          )}
          {navLinks.map((link) => (
            <Link
              key={link.href}
              href={link.href}
              className={pathname === link.match ? styles.linkActive : styles.linkInactive}
            >
              {link.label}
            </Link>
          ))}
        </div>

        <div className={styles.spacer} />

        <div className={styles.rightArea}>
          {lessonBadge && (
            <span className={styles.badge}>{lessonBadge}</span>
          )}
          {user && (
            <>
              <div className={styles.avatar} title={user.name}>
                {getInitials(user.name)}
              </div>
              <button onClick={handleLogout} className={styles.logoutBtn}>
                Salir
              </button>
            </>
          )}
        </div>

      </div>
    </nav>
  );
}
