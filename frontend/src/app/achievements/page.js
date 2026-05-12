'use client';
import Navbar from '../../components/ui/Navbar';

const styles = {
  page:        'min-h-screen bg-brand-dark',
  main:        'max-w-4xl mx-auto px-6 py-10',
  header:      'mb-10',
  title:       'text-2xl font-bold text-white mb-1',
  subtitle:    'text-brand-text text-sm',
  section:     'mb-8',
  sectionTitle:'text-xs font-semibold text-brand-text uppercase tracking-widest mb-4',
  grid:        'grid grid-cols-2 md:grid-cols-3 gap-4',
  card:        'rounded-xl border p-5 flex flex-col gap-3 transition-all duration-200',
  cardUnlocked:'border-brand-green/20 bg-brand-green/5 hover:bg-brand-green/10',
  cardLocked:  'border-white/5 bg-brand-card opacity-50',
  iconBox:     'w-10 h-10 rounded-lg flex items-center justify-center text-base font-bold',
  iconUnlocked:'bg-brand-green/20 text-brand-green',
  iconLocked:  'bg-brand-hover text-brand-text',
  name:        'text-sm font-semibold text-white',
  desc:        'text-xs text-brand-text leading-relaxed',
  badge:       'text-[10px] font-semibold px-2 py-0.5 rounded-full w-fit',
  badgeOn:     'bg-brand-green/20 text-brand-green',
  badgeOff:    'bg-brand-hover text-brand-text',
  xpBar:       'mt-2',
  xpLabel:     'flex justify-between text-xs text-brand-text mb-1',
  xpTrack:     'h-1 bg-brand-hover rounded-full overflow-hidden',
  xpFill:      'h-full bg-brand-green rounded-full transition-all duration-500',
};

const ACHIEVEMENTS = [
  {
    id: 'first_lesson',
    icon: 'N',
    name: 'Primera lección',
    desc: 'Completaste tu primera canción en Notara.',
    unlocked: true,
  },
  {
    id: 'week_streak',
    icon: '7',
    name: 'Semana perfecta',
    desc: '7 días seguidos estudiando sin pausar.',
    unlocked: true,
  },
  {
    id: 'polyglot',
    icon: '3',
    name: 'Políglota',
    desc: 'Estudiaste canciones de 3 géneros distintos.',
    unlocked: false,
  },
  {
    id: 'explorer',
    icon: '10',
    name: 'Explorador',
    desc: 'Abriste 10 canciones distintas.',
    unlocked: false,
  },
  {
    id: 'vocabulary',
    icon: '50',
    name: 'Vocabulario',
    desc: 'Aprendiste 50 palabras nuevas en inglés.',
    unlocked: false,
  },
  {
    id: 'fire_streak',
    icon: '30',
    name: 'Racha de fuego',
    desc: '30 días consecutivos de estudio.',
    unlocked: false,
  },
];

const unlocked = ACHIEVEMENTS.filter(a => a.unlocked);
const locked   = ACHIEVEMENTS.filter(a => !a.unlocked);

export default function AchievementsPage() {
  return (
    <div className={styles.page}>
      <Navbar />
      <main className={styles.main}>

        <div className={styles.header}>
          <h1 className={styles.title}>Logros</h1>
          <p className={styles.subtitle}>{unlocked.length} de {ACHIEVEMENTS.length} desbloqueados</p>
        </div>

        <div className={styles.xpBar}>
          <div className={styles.xpLabel}>
            <span>Progreso total</span>
            <span>{unlocked.length}/{ACHIEVEMENTS.length}</span>
          </div>
          <div className={styles.xpTrack}>
            <div
              className={styles.xpFill}
              style={{ width: `${(unlocked.length / ACHIEVEMENTS.length) * 100}%` }}
            />
          </div>
        </div>

        <div className="mt-8">
          <p className={styles.sectionTitle}>Desbloqueados</p>
          <div className={styles.grid}>
            {unlocked.map(a => (
              <div key={a.id} className={`${styles.card} ${styles.cardUnlocked}`}>
                <div className={`${styles.iconBox} ${styles.iconUnlocked}`}>{a.icon}</div>
                <div>
                  <p className={styles.name}>{a.name}</p>
                  <p className={styles.desc}>{a.desc}</p>
                </div>
                <span className={`${styles.badge} ${styles.badgeOn}`}>Completado</span>
              </div>
            ))}
          </div>
        </div>

        <div className="mt-8">
          <p className={styles.sectionTitle}>Por desbloquear</p>
          <div className={styles.grid}>
            {locked.map(a => (
              <div key={a.id} className={`${styles.card} ${styles.cardLocked}`}>
                <div className={`${styles.iconBox} ${styles.iconLocked}`}>{a.icon}</div>
                <div>
                  <p className={styles.name}>{a.name}</p>
                  <p className={styles.desc}>{a.desc}</p>
                </div>
                <span className={`${styles.badge} ${styles.badgeOff}`}>Bloqueado</span>
              </div>
            ))}
          </div>
        </div>

      </main>
    </div>
  );
}
