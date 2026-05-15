'use client';
import { useState, useEffect, useCallback, useRef } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { songs as songsApi, progress as progressApi, ia } from '../../../lib/api';
import Navbar from '../../../components/ui/Navbar';
import AudioPlayer from '../../../components/ui/AudioPlayer';
import ExercisePanel from '../../../components/lesson/ExercisePanel';
import { LYRICS_STRATEGIES, getLyricsStrategy } from '../../../patterns/LyricsDisplayStrategy';

const EXERCISE_CARDS = [
  { badge: 'Vocabulario', color: 'green',  title: 'Completa los espacios',  desc: 'Rellena las palabras que faltan en la letra.' },
  { badge: 'Escucha',     color: 'orange', title: 'Dictado de letra',        desc: 'Escucha y escribe lo que oyes.' },
  { badge: 'Gramática',   color: 'purple', title: 'Tiempo verbal',           desc: 'Identifica verbos en presente perfecto.' },
  { badge: 'Vocabulario', color: 'green',  title: 'Tarjetas de memoria',     desc: 'Practica con flashcards interactivas.' },
];

const BADGE_COLORS = {
  green:  'bg-brand-green/20 text-brand-green',
  orange: 'bg-brand-orange/20 text-brand-orange',
  purple: 'bg-brand-purple/20 text-brand-purple',
};

const DOT_COLORS = {
  known:    'bg-brand-green',
  learning: 'bg-brand-orange',
  new:      'bg-brand-text',
};

// ─── Helpers ──────────────────────────────────────────────────────────────────

function parseLRC(lrc) {
  if (!lrc) return [];
  return lrc
    .split('\n')
    .map((line) => {
      const match = line.match(/\[(\d+):(\d+\.\d+)\](.*)/);
      if (!match) return null;
      return {
        time: parseInt(match[1]) * 60 + parseFloat(match[2]),
        text: match[3].trim(),
      };
    })
    .filter(Boolean);
}

function capitalize(str = '') {
  return str.charAt(0).toUpperCase() + str.slice(1);
}

// ─── Sub-componentes ──────────────────────────────────────────────────────────

function SectionLabel({ children }) {
  return (
    <p className="text-brand-text text-[10px] font-semibold uppercase tracking-widest mb-3">
      {children}
    </p>
  );
}

function ExerciseCard({ badge, color, title, desc, onClick }) {
  return (
    <button
      onClick={onClick}
      className="w-full text-left p-3 rounded-xl bg-brand-hover hover:bg-white/5 border border-white/5 transition-colors group"
    >
      <span className={`text-[10px] font-semibold px-2 py-0.5 rounded-full ${BADGE_COLORS[color]}`}>
        {badge}
      </span>
      <p className="text-white text-sm font-medium mt-2 group-hover:text-brand-green transition-colors">
        {title}
      </p>
      <p className="text-brand-text text-xs mt-0.5">{desc}</p>
    </button>
  );
}

function ProgressBar({ label, value, max, color = 'bg-brand-green' }) {
  const pct = max > 0 ? Math.min((value / max) * 100, 100) : 0;
  return (
    <div className="space-y-1">
      <div className="flex justify-between text-xs">
        <span className="text-brand-text">{label}</span>
        <span className="text-white font-medium">{value}{max ? `/${max}` : ''}</span>
      </div>
      <div className="h-1.5 bg-brand-hover rounded-full overflow-hidden">
        <div className={`h-full rounded-full transition-all ${color}`} style={{ width: `${pct}%` }} />
      </div>
    </div>
  );
}

// ─── Página principal ─────────────────────────────────────────────────────────

export default function LessonPage() {
  const { songId } = useParams();
  const router     = useRouter();

  // Datos de la canción
  const [song, setSong]           = useState(null);
  const [lyricsData, setLyricsData] = useState(null);
  const [parsedLines, setParsedLines] = useState([]);
  const [lessonInfo, setLessonInfo] = useState(null);
  const [stats, setStats]         = useState(null);
  const [relatedSongs, setRelatedSongs] = useState([]);
  const [loading, setLoading]     = useState(true);
  const [error, setError]         = useState('');

  // Reproducción
  const [currentTime, setCurrentTime] = useState(0);
  const [activeLineIdx, setActiveLineIdx] = useState(-1);
  const lyricsContainerRef = useRef(null);

  // Patrón Strategy: estrategia activa de visualización de letra
  const [lyricsMode, setLyricsMode] = useState('en-only');
  const [translations, setTranslations] = useState({});
  const [keywords, setKeywords]     = useState([]);

  // Chat embebido
  const [chatMessages, setChatMessages] = useState([
    { role: 'ai', content: 'Hola! Pregúntame sobre cualquier palabra de la canción' },
  ]);
  const [chatInput, setChatInput]   = useState('');
  const [chatLoading, setChatLoading] = useState(false);
  const chatEndRef = useRef(null);

  // Ejercicio activo — reemplaza la columna central cuando está abierto
  const [activeExercise, setActiveExercise] = useState(null);
  const [lastClickedPhrase, setLastClickedPhrase] = useState('');

  const addXP = useCallback((amount) => {
    try {
      const stored = JSON.parse(localStorage.getItem('notara_progress') || '{}');
      stored.xp = (stored.xp || 0) + amount;
      localStorage.setItem('notara_progress', JSON.stringify(stored));
    } catch {}
  }, []);

  // Carga inicial
  useEffect(() => {
    if (!songId) return;

    const load = async () => {
      setLoading(true);
      try {
        const [songRes, lyricsRes, lessonRes, statsRes] = await Promise.allSettled([
          songsApi.getById(songId),
          songsApi.getLyrics(songId),
          songsApi.getLessonType(songId),
          progressApi.getStats(),
        ]);

        const songData = songRes.status === 'fulfilled' ? songRes.value?.song : null;
        if (songData) {
          setSong(songData);
          // Buscar canciones relacionadas del mismo artista
          songsApi.search(songData.artist, 4)
            .then((d) => setRelatedSongs((d.results || []).filter(s => s.spotifyId !== songId).slice(0, 3)))
            .catch(() => {});
        }

        if (lyricsRes.status === 'fulfilled') {
          const ld = lyricsRes.value;
          setLyricsData(ld);
          if (ld.synced) setParsedLines(parseLRC(ld.lyrics));
        }

        if (lessonRes.status === 'fulfilled') {
          const lesson = lessonRes.value?.lesson;
          setLessonInfo(lesson);
          // Extraer palabras clave de los ejercicios del LLM si están disponibles
          if (lesson?.exercises?.length) {
            const kw = lesson.exercises
              .filter(e => e.targetWord)
              .slice(0, 6)
              .map((e, i) => ({
                word:        e.targetWord,
                translation: e.translation || '—',
                level:       i < 2 ? 'known' : i < 4 ? 'learning' : 'new',
              }));
            if (kw.length) setKeywords(kw);
          }
        }

        if (statsRes.status === 'fulfilled') setStats(statsRes.value);
      } catch {
        setError('Error al cargar la canción');
      } finally {
        setLoading(false);
      }
    };

    load();
  }, [songId]);

  // Sincronización de letra con el player
  useEffect(() => {
    if (!parsedLines.length) return;

    let idx = -1;
    for (let i = 0; i < parsedLines.length; i++) {
      if (parsedLines[i].time <= currentTime) idx = i;
      else break;
    }

    if (idx !== activeLineIdx) {
      setActiveLineIdx(idx);
      const el = lyricsContainerRef.current?.querySelector(`[data-line="${idx}"]`);
      el?.scrollIntoView({ behavior: 'smooth', block: 'center' });
    }
  }, [currentTime, parsedLines, activeLineIdx]);

  // Auto-scroll del chat
  useEffect(() => {
    chatEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [chatMessages]);

  const handleTimeUpdate = useCallback((seconds) => setCurrentTime(seconds), []);

  // Clic en una línea de letra → traduce y abre chat contextual
  const handleLineClick = async (line, idx) => {
    if (!line.trim()) return;
    setLastClickedPhrase(line);

    if (translations[idx]) {
      setChatMessages(prev => [...prev, { role: 'user', content: `¿Qué significa "${line}"?` }]);
      setChatMessages(prev => [...prev, { role: 'ai', content: translations[idx] }]);
      return;
    }

    // Obtener explicación del backend
    setChatMessages(prev => [...prev, { role: 'user', content: `¿Qué significa "${line}"?` }]);
    setChatLoading(true);

    try {
      const data = await ia.explain(songId, line);
      const reply = data.significado
        ? `${data.significado}${data.gramatica ? `\n\n${data.gramatica}` : ''}${data.ejemplo ? `\n\nEjemplo: ${data.ejemplo}` : ''}`
        : data.explanation || 'No se pudo obtener la explicación.';
      setTranslations(prev => ({ ...prev, [idx]: data.significado || reply }));
      setChatMessages(prev => [...prev, { role: 'ai', content: reply }]);
    } catch {
      setChatMessages(prev => [...prev, { role: 'ai', content: 'No se pudo obtener la explicación en este momento.' }]);
    } finally {
      setChatLoading(false);
    }
  };

  // Enviar mensaje de chat libre
  const handleChatSend = async () => {
    if (!chatInput.trim() || chatLoading) return;
    const message = chatInput.trim();
    setChatInput('');
    setChatMessages(prev => [...prev, { role: 'user', content: message }]);
    setChatLoading(true);

    try {
      const data = await ia.chat(songId, message, chatMessages.filter(m => m.role !== 'ai' || m.content !== chatMessages[0].content));
      setChatMessages(prev => [...prev, { role: 'ai', content: data.response || data.reply || 'Sin respuesta.' }]);
    } catch {
      setChatMessages(prev => [...prev, { role: 'ai', content: 'Error al responder. Intenta de nuevo.' }]);
    } finally {
      setChatLoading(false);
    }
  };

  const displayLines = parsedLines.length > 0
    ? parsedLines
    : (lyricsData?.lyrics && !lyricsData.synced)
      ? lyricsData.lyrics.split('\n').filter(l => l.trim()).map(text => ({ text, time: null }))
      : [];

  // ─── Estados de carga / error ─────────────────────────────────────────────

  if (loading) {
    return (
      <div className="h-screen bg-brand-dark flex items-center justify-center">
        <div className="text-center">
          <div className="w-10 h-10 rounded-full border-2 border-brand-green border-t-transparent animate-spin mx-auto" />
          <p className="text-brand-text mt-4 text-sm">Cargando lección...</p>
        </div>
      </div>
    );
  }

  if (error || !song) {
    return (
      <div className="h-screen bg-brand-dark flex flex-col">
        <Navbar />
        <div className="flex-1 flex items-center justify-center text-center">
          <div>
            <p className="text-white text-lg mb-4">{error || 'Canción no encontrada'}</p>
            <button onClick={() => router.push('/search')} className="text-brand-green hover:underline text-sm">
              ← Volver a buscar
            </button>
          </div>
        </div>
      </div>
    );
  }

  // ─── Layout principal ─────────────────────────────────────────────────────

  return (
    <div className="h-screen flex flex-col bg-brand-dark overflow-hidden">
      <Navbar lessonBadge={lessonInfo?.type ? capitalize(lessonInfo.type) : undefined} />

      <div className="flex flex-1 overflow-hidden">

        {/* ═══════════════ COLUMNA IZQUIERDA ═══════════════ */}
        <aside className="w-60 flex-shrink-0 flex flex-col border-r border-white/5 overflow-y-auto p-4 space-y-5">

          {/* Player */}
          <div>
            <SectionLabel>Reproduciendo</SectionLabel>
            {song.imageUrl && (
              <img
                src={song.imageUrl}
                alt={song.album || song.title}
                className="w-full aspect-square rounded-xl object-cover mb-3"
              />
            )}
            <p className="text-white font-semibold text-sm truncate">{song.title}</p>
            <p className="text-brand-text text-xs truncate">{song.artist}</p>
            {song.album && (
              <p className="text-brand-text text-xs truncate opacity-60 mt-0.5">{song.album}</p>
            )}
            <div className="mt-3">
              <AudioPlayer
                previewUrl={song.previewUrl}
                spotifyId={songId}
                songTitle={song.title}
              />
            </div>
          </div>

          {/* Tipo de lección */}
          {lessonInfo && (
            <div>
              <SectionLabel>Tipo de lección</SectionLabel>
              <span className="inline-block bg-brand-purple/20 text-brand-purple text-xs font-semibold px-3 py-1 rounded-full">
                {capitalize(lessonInfo.type || 'General')}
              </span>
              {lessonInfo.focus && (
                <p className="text-brand-text text-xs mt-2">{lessonInfo.focus}</p>
              )}
            </div>
          )}

          {/* Palabras clave */}
          {keywords.length > 0 && (
            <div className="flex-1">
              <SectionLabel>Palabras clave</SectionLabel>
              <div className="space-y-2">
                {keywords.map((kw, i) => (
                  <div key={i} className="flex items-center justify-between gap-2">
                    <div className="min-w-0">
                      <p className="text-white text-xs font-medium truncate">{kw.word}</p>
                      <p className="text-brand-text text-[10px] truncate">{kw.translation}</p>
                    </div>
                    <span className={`w-2 h-2 rounded-full flex-shrink-0 ${DOT_COLORS[kw.level]}`} />
                  </div>
                ))}
              </div>
            </div>
          )}

        </aside>

        {/* ═══════════════ COLUMNA CENTRAL ═══════════════ */}
        <main className="flex-1 flex flex-col overflow-hidden border-r border-white/5">

          {activeExercise ? (
            <ExercisePanel
              songId={songId}
              phrase={lastClickedPhrase}
              exerciseType={activeExercise}
              onClose={() => setActiveExercise(null)}
              onXP={addXP}
            />
          ) : (
            <>
              {/* Toggles — Patrón Strategy */}
              <div className="flex-shrink-0 flex items-center gap-1 px-4 py-3 border-b border-white/5">
                {LYRICS_STRATEGIES.map((strategy) => (
                  <button
                    key={strategy.id}
                    onClick={() => setLyricsMode(strategy.id)}
                    className={`px-3 py-1.5 rounded-lg text-xs font-medium transition-colors ${
                      lyricsMode === strategy.id
                        ? 'bg-brand-green/20 text-brand-green font-semibold border border-brand-green/30'
                        : 'text-brand-text hover:text-white hover:bg-brand-hover'
                    }`}
                  >
                    {strategy.label}
                  </button>
                ))}
                {lyricsData?.synced && (
                  <span className="ml-auto text-brand-green text-[10px] font-medium">Sincronizada</span>
                )}
              </div>

              {/* Letra */}
              <div ref={lyricsContainerRef} className="flex-1 overflow-y-auto px-4 py-3">
                {displayLines.length > 0 ? (
                  getLyricsStrategy(lyricsMode).render({
                    lines:       displayLines,
                    translations,
                    activeIdx:   activeLineIdx,
                    onLineClick: handleLineClick,
                  })
                ) : (
                  <p className="text-brand-text text-sm text-center py-12">
                    No encontramos la letra de esta canción.
                  </p>
                )}
              </div>

              {/* Chat embebido */}
              <div className="flex-shrink-0 border-t border-white/5 flex flex-col max-h-56">
                <div className="flex-1 overflow-y-auto px-4 py-3 space-y-2">
                  {chatMessages.map((msg, i) => (
                    <div key={i} className={`flex ${msg.role === 'user' ? 'justify-end' : 'justify-start'}`}>
                      {msg.role === 'ai' ? (
                        <p className="text-brand-text text-sm max-w-[85%]">{msg.content}</p>
                      ) : (
                        <span className="bg-brand-purple text-white text-sm px-3 py-1.5 rounded-xl max-w-[85%]">
                          {msg.content}
                        </span>
                      )}
                    </div>
                  ))}
                  {chatLoading && (
                    <div className="flex gap-1 px-1">
                      {[0, 1, 2].map(i => (
                        <div key={i} className="w-1.5 h-1.5 bg-brand-text rounded-full animate-bounce" style={{ animationDelay: `${i * 0.15}s` }} />
                      ))}
                    </div>
                  )}
                  <div ref={chatEndRef} />
                </div>
                <div className="flex gap-2 px-4 pb-3">
                  <input
                    type="text"
                    value={chatInput}
                    onChange={(e) => setChatInput(e.target.value)}
                    onKeyDown={(e) => e.key === 'Enter' && handleChatSend()}
                    placeholder="Pregúntale a la IA sobre la letra..."
                    className="flex-1 bg-brand-hover border border-white/5 rounded-xl px-4 py-2 text-white text-sm placeholder-brand-text focus:outline-none focus:border-brand-purple/50 transition-colors"
                  />
                  <button
                    onClick={handleChatSend}
                    disabled={!chatInput.trim() || chatLoading}
                    className="w-9 h-9 rounded-xl bg-brand-purple hover:bg-violet-500 disabled:opacity-40 transition-colors flex items-center justify-center text-white text-sm"
                  >
                    &#10148;
                  </button>
                </div>
              </div>
            </>
          )}

        </main>

        {/* ═══════════════ COLUMNA DERECHA ═══════════════ */}
        <aside className="w-72 flex-shrink-0 overflow-y-auto p-4 space-y-6">

          {/* Ejercicios */}
          <div>
            <SectionLabel>Ejercicios</SectionLabel>
            <div className="space-y-2">
              {EXERCISE_CARDS.map((ex, i) => (
                <ExerciseCard
                  key={i}
                  {...ex}
                  onClick={() => setActiveExercise(ex.title)}
                />
              ))}
            </div>
          </div>

          {/* Tu progreso */}
          {stats && (
            <div>
              <SectionLabel>Tu progreso</SectionLabel>
              <div className="bg-brand-card rounded-xl p-4 border border-white/5 space-y-4">
                <div className="flex items-center gap-3">
                  <div className="w-9 h-9 rounded-xl bg-brand-orange/15 flex items-center justify-center">
                    <span className="text-brand-orange font-black text-sm">{stats.streak || 0}</span>
                  </div>
                  <div>
                    <p className="text-white text-sm font-medium">Racha actual</p>
                    <p className="text-brand-text text-xs">{stats.streak || 0} día{stats.streak !== 1 ? 's' : ''} seguido{stats.streak !== 1 ? 's' : ''}</p>
                  </div>
                </div>

                <div className="space-y-3">
                  <ProgressBar
                    label="Vocabulario aprendido"
                    value={stats.wordsTotal || 0}
                    max={100}
                    color="bg-brand-green"
                  />
                  <ProgressBar
                    label="Lecciones completadas"
                    value={stats.songsCompleted || 0}
                    max={12}
                    color="bg-brand-green"
                  />
                  <ProgressBar
                    label="Ejercicios hoy"
                    value={stats.exercisesToday || 0}
                    max={5}
                    color="bg-brand-orange"
                  />
                </div>
              </div>
            </div>
          )}

          {/* Canciones relacionadas */}
          {relatedSongs.length > 0 && (
            <div>
              <SectionLabel>Canciones relacionadas</SectionLabel>
              <div className="space-y-2">
                {relatedSongs.map((s) => (
                  <button
                    key={s.spotifyId}
                    onClick={() => router.push(`/lesson/${s.spotifyId}`)}
                    className="w-full flex items-center gap-3 p-2 rounded-xl hover:bg-brand-hover transition-colors group text-left"
                  >
                    {s.imageUrl ? (
                      <img src={s.imageUrl} alt={s.title} className="w-10 h-10 rounded-lg object-cover flex-shrink-0" />
                    ) : (
                      <div className="w-10 h-10 rounded-lg bg-brand-hover flex-shrink-0" />
                    )}
                    <div className="min-w-0">
                      <p className="text-white text-xs font-medium truncate group-hover:text-brand-green transition-colors">{s.title}</p>
                      <p className="text-brand-text text-[10px] truncate">{s.artist}</p>
                    </div>
                    {s.lessonType && (
                      <span className={`ml-auto text-[10px] font-semibold px-1.5 py-0.5 rounded flex-shrink-0 ${BADGE_COLORS[s.lessonType === 'vocabulary' ? 'green' : s.lessonType === 'grammar' ? 'purple' : 'orange']}`}>
                        {capitalize(s.lessonType)}
                      </span>
                    )}
                  </button>
                ))}
              </div>
            </div>
          )}

        </aside>

      </div>
    </div>
  );
}
