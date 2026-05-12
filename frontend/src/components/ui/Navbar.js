'use client';
import { useState, useEffect } from 'react';
import Link from 'next/link';
import { useRouter, usePathname } from 'next/navigation';
import { useAuth } from '../../context/AuthContext';
import notaraLogo from '../../../assets/notara-logo.png';

const styles = {
  nav:          'sticky top-0 z-50 bg-brand-dark/90 backdrop-blur-md border-b border-white/5',
  inner:        'max-w-none px-6 h-16 flex items-center gap-5',
  logo:         'flex items-center gap-2 font-semibold text-sm mr-3 shrink-0',
  dot:          'w-2 h-2 rounded-full bg-brand-green',
  logoText:     'text-white',
  navLinks:     'flex items-center gap-1',
  linkActive:   'px-3 py-1.5 text-sm font-medium text-white bg-brand-card rounded-lg',
  linkInactive: 'px-3 py-1.5 text-sm font-medium text-brand-text hover:text-white hover:bg-brand-card/50 rounded-lg transition-colors',
  spacer:       'flex-1',
  rightArea:    'flex items-center gap-3 shrink-0',
  xpPill:       'flex items-center gap-2 px-3 py-1.5 rounded-lg bg-brand-card border border-white/5',
  xpDot:        'w-1.5 h-1.5 rounded-full bg-brand-green',
  xpText:       'text-xs font-semibold text-white',
  xpSub:        'text-xs text-brand-text',
  badge:        'bg-brand-green text-black text-xs font-semibold px-3 py-1 rounded-full',
  avatar:       'w-8 h-8 rounded-full bg-brand-purple flex items-center justify-center text-white text-xs font-bold',
  logoutBtn:    'text-brand-text hover:text-white text-sm transition-colors ml-1',
};

const LEVEL_THRESHOLDS = [0, 100, 300, 700, 1500, 3000];
function getLevel(xp) {
  return LEVEL_THRESHOLDS.filter(t => xp >= t).length;
}

const navLinks = [
  { href: '/search',       label: 'Buscar',      match: '/search' },
  { href: '/dashboard',    label: 'Mi progreso', match: '/dashboard' },
  { href: '/achievements', label: 'Logros',      match: '/achievements' },
];

function getInitials(name = '') {
  return name.split(' ').slice(0, 2).map(w => w[0]).join('').toUpperCase() || 'U';
}

export default function Navbar({ lessonBadge }) {
  const { user, logout } = useAuth();
  const router   = useRouter();
  const pathname = usePathname();
  const [xp, setXp] = useState(0);

  useEffect(() => {
    try {
      const stored = JSON.parse(localStorage.getItem('notara_progress') || '{}');
      setXp(stored.xp || 0);
    } catch {}
  }, []);

  const level = getLevel(xp);

  const handleLogout = () => {
    logout();
    router.push('/login');
  };

  const isOnLesson = pathname?.startsWith('/lesson');

  return (
    <nav className={styles.nav}>
      <div className={styles.inner}>

        <Link href="/search" className={styles.logo}>
          <Image src={notaraLogo} alt="Notara Logo" width={72} height={72} className="rounded-sm" />
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
              className={pathname?.startsWith(link.match) ? styles.linkActive : styles.linkInactive}
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
              <div className={styles.xpPill}>
                <span className={styles.xpDot} />
                <span className={styles.xpText}>Nv. {level}</span>
                <span className={styles.xpSub}>{xp} XP</span>
              </div>
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
