'use client';

/**
 * ProgressWidget — muestra racha, palabras aprendidas y nivel del usuario.
 */
export default function ProgressWidget({ stats }) {
  if (!stats) return null;

  const levelLabel = (level) => {
    if (level < 20)  return { label: 'Principiante', color: 'text-blue-400' };
    if (level < 50)  return { label: 'Básico',       color: 'text-green-400' };
    if (level < 100) return { label: 'Intermedio',   color: 'text-yellow-400' };
    if (level < 200) return { label: 'Avanzado',     color: 'text-orange-400' };
    return              { label: 'Experto',       color: 'text-purple-400' };
  };

  const { label, color } = levelLabel(stats.wordsTotal || 0);

  return (
    <div className="bg-brand-card rounded-xl p-4 border border-white/5">
      <h3 className="text-white font-semibold text-sm mb-3">Tu progreso</h3>
      <div className="grid grid-cols-3 gap-3">

        {/* Racha */}
        <div className="text-center">
          <p className="text-2xl font-bold text-brand-green">
            {stats.streak || 0}
          </p>
          <p className="text-brand-text text-xs mt-1">🔥 Racha</p>
        </div>

        {/* Palabras */}
        <div className="text-center">
          <p className="text-2xl font-bold text-white">
            {stats.wordsTotal || 0}
          </p>
          <p className="text-brand-text text-xs mt-1">📖 Palabras</p>
        </div>

        {/* Nivel */}
        <div className="text-center">
          <p className={`text-sm font-bold ${color}`}>{label}</p>
          <p className="text-brand-text text-xs mt-1">⭐ Nivel</p>
        </div>
      </div>

      {/* Canciones completadas */}
      {stats.songsCompleted > 0 && (
        <div className="mt-3 pt-3 border-t border-white/5 text-center">
          <span className="text-brand-text text-xs">
            🎵 {stats.songsCompleted} canción{stats.songsCompleted !== 1 ? 'es' : ''} completada{stats.songsCompleted !== 1 ? 's' : ''}
          </span>
        </div>
      )}
    </div>
  );
}
