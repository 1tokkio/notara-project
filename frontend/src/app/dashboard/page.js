'use client';
import { useState, useEffect } from 'react';
import {
  AreaChart, Area, XAxis, YAxis, CartesianGrid,
  Tooltip, ResponsiveContainer, BarChart, Bar
} from 'recharts';
import { progress as progressApi } from '@/lib/api';
import Navbar from '@/components/ui/Navbar';
import { useAuth } from '@/context/AuthContext';

// ─── Sub-componentes ──────────────────────────────────────────────────────────

function StatCard({ icon, value, label, color = 'text-white' }) {
  return (
    <div className="bg-brand-card rounded-xl p-5 border border-white/5 text-center">
      <p className="text-3xl mb-1">{icon}</p>
      <p className={`text-3xl font-bold ${color}`}>{value}</p>
      <p className="text-brand-text text-sm mt-1">{label}</p>
    </div>
  );
}

const CustomTooltip = ({ active, payload, label }) => {
  if (!active || !payload?.length) return null;
  return (
    <div className="bg-brand-hover border border-white/10 rounded-lg px-3 py-2 text-sm">
      <p className="text-brand-text mb-1">{label}</p>
      {payload.map((p, i) => (
        <p key={i} style={{ color: p.color }}>
          {p.name}: <strong>{p.value}</strong>
        </p>
      ))}
    </div>
  );
};

// ─── Página principal ─────────────────────────────────────────────────────────

export default function DashboardPage() {
  const { user } = useAuth();
  const [stats, setStats]     = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    progressApi.getStats()
      .then(setStats)
      .catch(() => setStats(null))
      .finally(() => setLoading(false));
  }, []);

  // Datos mock para los gráficos mientras no haya datos reales
  const weeklyData = stats?.weeklyWords || [
    { day: 'Lun', palabras: 0 },
    { day: 'Mar', palabras: 0 },
    { day: 'Mié', palabras: 0 },
    { day: 'Jue', palabras: 0 },
    { day: 'Vie', palabras: 0 },
    { day: 'Sáb', palabras: 0 },
    { day: 'Dom', palabras: 0 },
  ];

  const monthlyData = stats?.monthlyProgress || [
    { semana: 'S1', palabras: 0, canciones: 0 },
    { semana: 'S2', palabras: 0, canciones: 0 },
    { semana: 'S3', palabras: 0, canciones: 0 },
    { semana: 'S4', palabras: 0, canciones: 0 },
  ];

  const levelLabel = (words) => {
    if (!words || words < 20)  return 'Principiante';
    if (words < 50)   return 'Básico';
    if (words < 100)  return 'Intermedio';
    if (words < 200)  return 'Avanzado';
    return 'Experto';
  };

  return (
    <div className="min-h-screen bg-brand-dark">
      <Navbar />

      <main className="max-w-5xl mx-auto px-4 py-8 space-y-8">

        {/* Header */}
        <div>
          <h1 className="text-3xl font-bold text-white">Mi Progreso</h1>
          <p className="text-brand-text mt-1">
            Hola {user?.name} — aquí tienes tu historial de aprendizaje
          </p>
        </div>

        {loading ? (
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            {[...Array(4)].map((_, i) => (
              <div key={i} className="bg-brand-card rounded-xl p-5 h-28 animate-pulse" />
            ))}
          </div>
        ) : (
          <>
            {/* Stats cards */}
            <div className="grid grid-cols-2 md:grid-cols-4 gap-4 animate-fadeIn">
              <StatCard
                icon="🔥"
                value={stats?.streak ?? 0}
                label="Días de racha"
                color="text-orange-400"
              />
              <StatCard
                icon="📖"
                value={stats?.wordsTotal ?? 0}
                label="Palabras aprendidas"
                color="text-brand-green"
              />
              <StatCard
                icon="🎵"
                value={stats?.songsCompleted ?? 0}
                label="Canciones completadas"
                color="text-blue-400"
              />
              <StatCard
                icon="⭐"
                value={levelLabel(stats?.wordsTotal)}
                label="Nivel estimado"
                color="text-yellow-400"
              />
            </div>

            {/* Gráfico semanal */}
            <div className="bg-brand-card rounded-xl p-6 border border-white/5 animate-fadeIn">
              <h2 className="text-white font-semibold mb-1">Palabras por día — esta semana</h2>
              <p className="text-brand-text text-xs mb-4">Cada barra representa las palabras aprendidas ese día</p>
              <ResponsiveContainer width="100%" height={200}>
                <BarChart data={weeklyData} margin={{ top: 5, right: 10, left: -20, bottom: 5 }}>
                  <CartesianGrid strokeDasharray="3 3" stroke="#282828" />
                  <XAxis dataKey="day" tick={{ fill: '#B3B3B3', fontSize: 12 }} />
                  <YAxis tick={{ fill: '#B3B3B3', fontSize: 12 }} />
                  <Tooltip content={<CustomTooltip />} />
                  <Bar dataKey="palabras" fill="#1DB954" radius={[4, 4, 0, 0]} />
                </BarChart>
              </ResponsiveContainer>
            </div>

            {/* Gráfico mensual */}
            <div className="bg-brand-card rounded-xl p-6 border border-white/5 animate-fadeIn">
              <h2 className="text-white font-semibold mb-1">Progreso del mes</h2>
              <p className="text-brand-text text-xs mb-4">Palabras aprendidas y canciones completadas por semana</p>
              <ResponsiveContainer width="100%" height={220}>
                <AreaChart data={monthlyData} margin={{ top: 5, right: 10, left: -20, bottom: 5 }}>
                  <defs>
                    <linearGradient id="greenGrad" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="5%"  stopColor="#1DB954" stopOpacity={0.3} />
                      <stop offset="95%" stopColor="#1DB954" stopOpacity={0} />
                    </linearGradient>
                    <linearGradient id="blueGrad" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="5%"  stopColor="#60a5fa" stopOpacity={0.3} />
                      <stop offset="95%" stopColor="#60a5fa" stopOpacity={0} />
                    </linearGradient>
                  </defs>
                  <CartesianGrid strokeDasharray="3 3" stroke="#282828" />
                  <XAxis dataKey="semana" tick={{ fill: '#B3B3B3', fontSize: 12 }} />
                  <YAxis tick={{ fill: '#B3B3B3', fontSize: 12 }} />
                  <Tooltip content={<CustomTooltip />} />
                  <Area type="monotone" dataKey="palabras"  stroke="#1DB954" fill="url(#greenGrad)" name="Palabras" />
                  <Area type="monotone" dataKey="canciones" stroke="#60a5fa" fill="url(#blueGrad)"  name="Canciones" />
                </AreaChart>
              </ResponsiveContainer>
            </div>

            {/* Racha visual */}
            {stats?.streak > 0 && (
              <div className="bg-gradient-to-r from-orange-900/30 to-brand-card rounded-xl p-6 border border-orange-500/20 animate-fadeIn">
                <div className="flex items-center gap-4">
                  <span className="text-5xl animate-pulse-green">🔥</span>
                  <div>
                    <p className="text-white font-bold text-xl">
                      ¡{stats.streak} día{stats.streak !== 1 ? 's' : ''} de racha!
                    </p>
                    <p className="text-brand-text text-sm mt-1">
                      Sigue así — vuelve mañana para no perder tu racha
                    </p>
                  </div>
                </div>
              </div>
            )}

            {/* Vacío */}
            {!stats?.wordsTotal && (
              <div className="text-center py-8 animate-fadeIn">
                <p className="text-4xl mb-4">🎵</p>
                <p className="text-white font-medium">¡Empieza tu primera lección!</p>
                <p className="text-brand-text text-sm mt-1">
                  Busca una canción y haz clic en una línea de la letra para comenzar.
                </p>
                <a href="/search" className="inline-block mt-4 bg-brand-green text-black font-semibold px-6 py-2 rounded-full hover:bg-green-400 transition-colors text-sm">
                  Buscar canciones
                </a>
              </div>
            )}
          </>
        )}
      </main>
    </div>
  );
}
