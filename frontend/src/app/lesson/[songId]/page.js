'use client';
import { useState, useEffect, useCallback, useRef } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { songs as songsApi, progress as progressApi, ia } from '../../../lib/api';
import { addXP, recordWordLearned, recordSongCompleted, addRecentSong, getProgress, mergeFromBackend } from '../../../lib/progressStore';
import Navbar from '../../../components/ui/Navbar';
import SpotifyEmbedPlayer from '../../../components/ui/SpotifyEmbedPlayer';
import SpotifySDKPlayer   from '../../../components/ui/SpotifySDKPlayer';
import { LYRICS_STRATEGIES, getLyricsStrategy } from '../../../patterns/LyricsDisplayStrategy';

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

function ExerciseCard({ badge, color, title, desc, onClick, disabled }) {
  return (
    <button
      onClick={onClick}
      disabled={disabled}
      className="w-full text-left p-3 rounded-xl bg-brand-hover hover:bg-white/5 border border-white/5 transition-colors group disabled:opacity-50 disabled:cursor-not-allowed"
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

function ExercisePanel({ exercises, onClose, onXP }) {
  const [idx, setIdx]       = useState(0);
  const [answer, setAnswer] = useState('');
  const [result, setResult] = useState(null);
  const [score, setScore]   = useState(0);
  const [done, setDone]     = useState(false);

  const exercise = exercises[idx];

  const handleOptionClick = (option) => {
    if (result) return;
    const correct = option === exercise.answer;
    setResult({ correct, explanation: exercise.explanation });
    if (correct) { setScore(s => s + 1); onXP(10); }
  };

  const handleTextSubmit = () => {
    if (!answer.trim() || result) return;
    const correct = answer.toLowerCase().trim() === (exercise.answer || '').toLowerCase().trim();
    setResult({ correct, explanation: exercise.explanation });
    if (correct) { setScore(s => s + 1); onXP(10); }
  };

  const handleNext = () => {
    if (idx + 1 >= exercises.length) {
      setDone(true);
    } else {
      setIdx(i => i + 1);
      setAnswer('');
      setResult(null);
    }
  };

  if (done) {
    return (
      <div className="p-4 rounded-xl border border-white/10 bg-brand-card text-center space-y-3">
        <div className="w-10 h-10 rounded-full bg-brand-green/20 flex items-center justify-center mx-auto">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" className="w-5 h-5 text-brand-green">
            <path d="M20 6 9 17l-5-5" strokeLinecap="round" strokeLinejoin="round" />
          </svg>
        </div>
        <p className="text-white font-semibold text-sm">Ejercicios completados</p>
        <p className="text-brand-text text-xs">{score}/{exercises.length} correctas · +{score * 10} XP</p>
        <button
          onClick={onClose}
          className="w-full py-2 rounded-lg bg-brand-hover border border-white/10 text-white text-sm hover:bg-brand-card transition-colors"
        >
          Volver a ejercicios
        </button>
      </div>
    );
  }

  return (
    <div className="p-4 rounded-xl border border-white/10 bg-brand-card space-y-3">
      <div className="flex items-center justify-between">
        <p className="text-[10px] font-semibold text-brand-text uppercase tracking-widest">
          Ejercicio {idx + 1}/{exercises.length}
        </p>
        <button onClick={onClose} className="text-brand-text hover:text-white text-xs transition-colors">✕</button>
      </div>

      <span className={`inline-block text-[10px] font-semibold px-2 py-0.5 rounded-full ${
        exercise.type === 'multiple-choice' ? BADGE_COLORS.green :
        exercise.type === 'fill-blank'      ? BADGE_COLORS.purple :
        BADGE_COLORS.orange
      }`}>
        {exercise.type === 'multiple-choice' ? 'Opción múltiple' :
         exercise.type === 'fill-blank'      ? 'Completar' : 'Traducción'}
      </span>

      <p className="text-white text-sm font-medium leading-snug">{exercise.question}</p>

      {exercise.type === 'multiple-choice' && (
        <div className="space-y-2">
          {(exercise.options || []).map((opt, i) => {
            let cls = 'w-full text-left px-3 py-2 rounded-lg border text-sm transition-colors ';
            if (!result)
              cls += 'border-white/10 text-brand-text hover:border-brand-green/50 hover:text-white cursor-pointer';
            else if (opt === exercise.answer)
              cls += 'border-brand-green bg-brand-green/10 text-brand-green';
            else
              cls += 'border-white/5 text-brand-text opacity-40 cursor-default';
            return (
              <button key={i} className={cls} onClick={() => handleOptionClick(opt)}>
                {opt}
              </button>
            );
          })}
        </div>
      )}

      {(exercise.type === 'fill-blank' || exercise.type === 'translation') && (
        <div className="space-y-2">
          <input
            type="text"
            value={answer}
            onChange={e => setAnswer(e.target.value)}
            onKeyDown={e => e.key === 'Enter' && handleTextSubmit()}
            disabled={!!result}
            placeholder="Tu respuesta..."
            className="w-full bg-brand-hover border border-white/5 rounded-lg px-3 py-2 text-white text-sm placeholder-brand-text focus:outline-none focus:border-brand-purple/50 transition-colors disabled:opacity-60"
          />
          {!result && (
            <button
              onClick={handleTextSubmit}
              disabled={!answer.trim()}
              className="w-full py-2 rounded-lg bg-brand-purple text-white text-sm hover:bg-violet-500 transition-colors disabled:opacity-40"
            >
              Verificar
            </button>
          )}
        </div>
      )}

      {result && (
        <div className={`p-3 rounded-lg border text-sm ${result.correct
          ? 'bg-brand-green/10 border-brand-green/20 text-brand-green'
          : 'bg-red-500/10 border-red-500/20 text-red-400'}`}
        >
          <p className="font-semibold">{result.correct ? '¡Correcto! +10 XP' : 'Incorrecto'}</p>
          {result.explanation && (
            <p className="text-brand-text text-xs mt-1 font-normal">{result.explanation}</p>
          )}
        </div>
      )}

      {result && (
        <button
          onClick={handleNext}
          className="w-full py-2 rounded-lg bg-brand-hover border border-white/10 text-white text-sm hover:bg-brand-card transition-colors"
        >
          {idx + 1 >= exercises.length ? 'Ver resultado' : 'Siguiente →'}
        </button>
      )}
    </div>
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

  // ── Token de Spotify Premium ──────────────────────────────────────────────
  const [spotifyToken, setSpotifyToken] = useState(null);

  useEffect(() => {
    // 1) Leer token desde URL params (vuelta del callback OAuth)
    const urlParams    = new URLSearchParams(window.location.search);
    const tokenFromUrl = urlParams.get('spotify_token');
    const refresh      = urlParams.get('spotify_refresh');

    if (tokenFromUrl) {
      localStorage.setItem('spotify_token', tokenFromUrl);
      if (refresh) localStorage.setItem('spotify_refresh', refresh);
      window.history.replaceState({}, '', window.location.pathname);
      setSpotifyToken(tokenFromUrl);
      return;
    }

    // 2) Leer token desde localStorage (sesión previa)
    const saved = localStorage.getItem('spotify_token');
    if (saved) setSpotifyToken(saved);
  }, []);
  // ─────────────────────────────────────────────────────────────────────────

  const [song, setSong]             = useState(null);
  const [lyricsData, setLyricsData] = useState(null);
  const [parsedLines, setParsedLines] = useState([]);
  const [lessonInfo, setLessonInfo] = useState(null);
  const [stats, setStats]           = useState(null);
  const [relatedSongs, setRelatedSongs] = useState([]);
  const [loading, setLoading]       = useState(true);
  const [error, setError]           = useState('');

  const [currentTime, setCurrentTime] = useState(0);
  const [activeLineIdx, setActiveLineIdx] = useState(-1);
  const lyricsContainerRef = useRef(null);

  const [lyricsMode, setLyricsMode] = useState('en-only');
  const [translations, setTranslations] = useState({});
  const [keywords, setKeywords]     = useState([]);
  const [lessonDone, setLessonDone] = useState(false);

  const [lastPhrase, setLastPhrase]     = useState('');
  const [exerciseSet, setExerciseSet]   = useState(null);
  const [exerciseLoading, setExerciseLoading] = useState(false);
  const [showExercises, setShowExercises] = useState(false);

  const [chatMessages, setChatMessages] = useState([]);
  const [chatInput, setChatInput]   = useState('');
  const [chatLoading, setChatLoading] = useState(false);

  useEffect(() => {
    setTranslations({});
    setKeywords([]);
    setActiveLineIdx(-1);
    setLessonDone(false);
    setLastPhrase('');
    setExerciseSet(null);
    setShowExercises(false);
  }, [songId]);

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
          addRecentSong(songData);
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

        if (statsRes.status === 'fulfilled' && statsRes.value) {
          mergeFromBackend(statsRes.value);
        }
        const localProgress = getProgress();
        setStats(statsRes.status === 'fulfilled' && statsRes.value
          ? { ...localProgress, ...statsRes.value }
          : localProgress
        );
      } catch {
        setError('Error al cargar la canción');
      } finally {
        setLoading(false);
      }
    };

    load();
  }, [songId]);

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

  const handleTimeUpdate = useCallback((seconds) => setCurrentTime(seconds), []);

  const handleLineClick = async (line, idx) => {
    if (!line.trim()) return;
    setLastPhrase(line);
  };

  const handleChatSend = async () => {
    if (!chatInput.trim() || chatLoading) return;
    const message = chatInput.trim();
    setChatInput('');
    setChatMessages(prev => [...prev, { role: 'user', content: message }]);
    setChatLoading(true);

    try {
      const data = await ia.chat(songId, message, chatMessages);
      setChatMessages(prev => [...prev, { role: 'ai', content: data.reply }]);
      addXP(2);
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

  return (
    <div className="h-screen flex flex-col bg-brand-dark overflow-hidden">
      <Navbar lessonBadge={lessonInfo?.type ? capitalize(lessonInfo.type) : undefined} />

      <div className="flex flex-1 overflow-hidden">

        {/* ═══════════════ COLUMNA IZQUIERDA ═══════════════ */}
        <aside className="w-60 flex-shrink-0 flex flex-col border-r border-white/5 overflow-y-auto p-4 space-y-5">

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

            {/* Player — SDK completo si tiene Premium, embed si no */}
            <div className="mt-3">
              {spotifyToken ? (
                <SpotifySDKPlayer
                  spotifyId={songId}
                  token={spotifyToken}
                  onTimeUpdate={handleTimeUpdate}
                />
              ) : (
                <div className="space-y-2">
                  <SpotifyEmbedPlayer spotifyId={songId} onTimeUpdate={handleTimeUpdate} />
                  <a
                    href={`${process.env.NEXT_PUBLIC_API_URL || 'http://localhost:3000'}/auth/spotify?songId=${songId}`}
                    className="flex items-center justify-center gap-1.5 w-full py-2 px-3 rounded-xl bg-[#1DB954] hover:bg-[#1ed760] text-black font-semibold text-xs transition-colors"
                  >
                    <svg className="w-3.5 h-3.5 flex-shrink-0" fill="currentColor" viewBox="0 0 24 24">
                      <path d="M12 0C5.373 0 0 5.373 0 12s5.373 12 12 12 12-5.373 12-12S18.627 0 12 0zm5.516 17.302a.748.748 0 0 1-1.03.25c-2.819-1.723-6.365-2.113-10.542-1.157a.748.748 0 0 1-.332-1.459c4.571-1.044 8.492-.595 11.655 1.337a.748.748 0 0 1 .249 1.029zm1.473-3.275a.937.937 0 0 1-1.288.308c-3.226-1.983-8.144-2.558-11.96-1.4a.937.937 0 1 1-.543-1.794c4.358-1.323 9.776-.681 13.483 1.596a.937.937 0 0 1 .308 1.29zm.127-3.409C15.496 8.412 9.439 8.209 5.87 9.309a1.124 1.124 0 1 1-.653-2.151c4.118-1.25 10.963-1.008 15.295 1.6a1.124 1.124 0 0 1-1.396 1.76z" />
                    </svg>
                    Conectar Premium
                  </a>
                </div>
              )}
            </div>
          </div>

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

          <div className="flex-shrink-0 flex items-center gap-1 px-4 py-3 border-b border-white/5">
            {LYRICS_STRATEGIES.map((strategy) => (
              <button
                key={strategy.id}
                onClick={() => setLyricsMode(strategy.id)}
                className={`px-3 py-1.5 rounded-lg text-xs font-medium transition-colors ${
                  lyricsMode === strategy.id
                    ? 'bg-brand-green/15 text-brand-green font-semibold ring-1 ring-brand-green/30'
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

          {/* Ejercicios desplegables */}
          {showExercises && (
            <div className="flex-shrink-0 border-t border-white/5 max-h-64 overflow-y-auto p-4">
              {exerciseSet ? (
                <ExercisePanel
                  exercises={exerciseSet}
                  onClose={() => { setExerciseSet(null); setShowExercises(false); }}
                  onXP={(amount) => { addXP(amount); setStats(getProgress()); }}
                />
              ) : (
                <div className="text-center py-4">
                  <div className="w-6 h-6 rounded-full border-2 border-brand-green border-t-transparent animate-spin mx-auto mb-2" />
                  <p className="text-brand-text text-xs">Generando ejercicios...</p>
                </div>
              )}
            </div>
          )}

        </main>

        {/* ═══════════════ COLUMNA DERECHA ═══════════════ */}
        <aside className="w-72 flex-shrink-0 overflow-y-auto p-4 space-y-6">

          <div>
            {lessonDone ? (
              <div className="flex items-center gap-3 p-3 rounded-xl bg-brand-green/10 border border-brand-green/20">
                <div className="w-8 h-8 rounded-full bg-brand-green/20 flex items-center justify-center flex-shrink-0">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" className="w-4 h-4 text-brand-green">
                    <path d="M20 6 9 17l-5-5" strokeLinecap="round" strokeLinejoin="round" />
                  </svg>
                </div>
                <div>
                  <p className="text-brand-green text-sm font-semibold">Lección completada</p>
                  <p className="text-brand-text text-xs">+50 XP ganados</p>
                </div>
              </div>
            ) : (
              <button
                onClick={() => {
                  const p = recordSongCompleted(song);
                  setStats(s => ({ ...s, ...p }));
                  setLessonDone(true);
                }}
                className="w-full py-2.5 rounded-xl bg-brand-green text-black font-semibold text-sm hover:bg-brand-green/90 transition-colors shadow-[0_0_16px_rgba(34,197,94,0.25)]"
              >
                Completar lección +50 XP
              </button>
            )}
          </div>

          <div>
            <SectionLabel>Ejercicios</SectionLabel>
            <button
              onClick={() => {
                if (!lastPhrase) {
                  setShowExercises(false);
                  return;
                }
                if (!showExercises) {
                  setShowExercises(true);
                  if (!exerciseSet) {
                    setExerciseLoading(true);
                    ia.getExercises(songId, lastPhrase)
                      .then(data => setExerciseSet(data.exercises || []))
                      .catch(() => {})
                      .finally(() => setExerciseLoading(false));
                  }
                } else {
                  setShowExercises(false);
                }
              }}
              disabled={exerciseLoading || !lastPhrase}
              className={`w-full py-2.5 px-4 rounded-xl font-semibold text-sm transition-colors ${
                showExercises
                  ? 'bg-brand-purple text-white hover:bg-violet-500'
                  : 'bg-brand-hover text-white hover:bg-white/10 border border-white/10'
              } disabled:opacity-50 disabled:cursor-not-allowed`}
            >
              {showExercises ? '✕ Cerrar ejercicios' : '▼ Activar ejercicios'}
            </button>
            {!lastPhrase && (
              <p className="text-brand-text text-[10px] text-center pt-2 opacity-70">
                Tocá una línea de la letra
              </p>
            )}
          </div>

          <div>
            <SectionLabel>Chat - Preguntas</SectionLabel>
            <div className="bg-brand-card rounded-xl border border-white/10 p-3 space-y-2 max-h-64 flex flex-col">
              <div className="flex-1 overflow-y-auto space-y-2">
                {chatMessages.slice(0, 8).map((msg, i) => (
                  <div key={i} className={`flex ${msg.role === 'user' ? 'justify-end' : 'justify-start'}`}>
                    {msg.role === 'ai' ? (
                      <p className="text-brand-text text-xs max-w-[85%] leading-relaxed">{msg.content}</p>
                    ) : (
                      <span className="bg-brand-purple text-white text-xs px-2 py-1 rounded-lg max-w-[85%]">
                        {msg.content}
                      </span>
                    )}
                  </div>
                ))}
                {chatLoading && (
                  <div className="flex gap-1 px-1">
                    {[0, 1, 2].map(i => (
                      <div key={i} className="w-1 h-1 bg-brand-text rounded-full animate-bounce" style={{ animationDelay: `${i * 0.15}s` }} />
                    ))}
                  </div>
                )}
              </div>
              <div className="flex gap-1.5 pt-2 border-t border-white/5">
                <input
                  type="text"
                  value={chatInput}
                  onChange={(e) => setChatInput(e.target.value)}
                  onKeyDown={(e) => e.key === 'Enter' && handleChatSend()}
                  placeholder="Pregunta algo..."
                  className="flex-1 bg-brand-hover border border-white/5 rounded-lg px-2 py-1.5 text-white text-xs placeholder-brand-text focus:outline-none focus:border-brand-purple/50 transition-colors"
                />
                <button
                  onClick={handleChatSend}
                  disabled={!chatInput.trim() || chatLoading}
                  className="px-2 py-1.5 rounded-lg bg-brand-purple hover:bg-violet-500 disabled:opacity-40 transition-colors flex items-center justify-center text-white text-xs"
                >
                  →
                </button>
              </div>
            </div>
          </div>

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
