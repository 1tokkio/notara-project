'use client';
import Link from 'next/link';
import { useRouter, usePathname } from 'next/navigation';
import { useAuth } from '../../context/AuthContext';

const styles = {
  nav:          'sticky top-0 z-50 bg-black/80 backdrop-blur-md border-b border-white/5',
  inner:        'max-w-6xl mx-auto px-4 h-16 flex items-center justify-between',
  logo:         'flex items-center gap-2 font-bold text-lg',
  logoMark:     'text-brand-green font-black text-xl tracking-tight',
  logoText:     'text-white',
  navLinks:     'hidden md:flex items-center gap-1',
  linkActive:   'px-4 py-2 rounded-lg text-sm font-medium transition-colors bg-brand-green text-black',
  linkInactive: 'px-4 py-2 rounded-lg text-sm font-medium transition-colors text-brand-text hover:text-white hover:bg-brand-hover',
  userArea:     'flex items-center gap-3',
  userName:     'text-brand-text text-sm hidden md:block',
  logoutBtn:    'text-brand-text hover:text-white text-sm transition-colors px-3 py-2 rounded-lg hover:bg-brand-hover',
};

const navLinks = [
  { href: '/search',    label: 'Buscar' },
  { href: '/dashboard', label: 'Progreso' },
];

export default function Navbar() {
  const { user, logout } = useAuth();
  const router   = useRouter();
  const pathname = usePathname();

  const handleLogout = () => {
    logout();
    router.push('/login');
  };

  return (
    <nav className={styles.nav}>
      <div className={styles.inner}>

        <Link href="/search" className={styles.logo}>
          <span className={styles.logoMark}>N</span>
          <span className={styles.logoText}>Notara</span>
        </Link>

        <div className={styles.navLinks}>
          {navLinks.map((link) => (
            <Link
              key={link.href}
              href={link.href}
              className={pathname === link.href ? styles.linkActive : styles.linkInactive}
            >
              {link.label}
            </Link>
          ))}
        </div>

        {user && (
          <div className={styles.userArea}>
            <span className={styles.userName}>{user.name}</span>
            <button onClick={handleLogout} className={styles.logoutBtn}>
              Salir
            </button>
          </div>
        )}
      </div>
    </nav>
  );
}
