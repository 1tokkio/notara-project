'use client';
import { useState, useEffect, useCallback, useRef } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { songs as songsApi, progress as progressApi } from '../../../lib/api';
import Navbar from '../../../components/ui/Navbar';
import SpotifyEmbedPlayer from '../../../components/ui/SpotifyEmbedPlayer';
import IAPanel from '../../../components/lesson/IAPanel';
import ProgressWidget from '../../../components/lesson/ProgressWidget';

/**
 * Parsea el formato LRC (con timestamps) a un array de líneas.
 * Formato LRC: [mm:ss.xx] Letra de la línea
 */
function parseLRC(lrc) {
  if (!lrc) return [];
  return lrc
    .split('\n')
    .map((line) => {
      const match = line.match(/\[(\d+):(\d+\.\d+)\](.*)/);
      if (!match) return null;
      const minutes = parseInt(match[1]);
      const seconds = parseFloat(match[2]);
      return {
        time: minutes * 60 + seconds,
        text: match[3].trim(),
      };
    })
    .filter(Boolean);
}

export default function LessonPage() {
  const { songId }  = useParams();
  const router      = useRouter();

  const [song, setSong]         = useState(null);
  const [lyricsData, setLyricsData] = useState(null);
  const [parsedLines, setParsedLines] = useState([]);
  const [lessonInfo, setLessonInfo] = useState(null);
  const [stats, setStats]       = useState(null);
  const [loading, setLoading]   = useState(true);
  const [error, setError]       = useState('');

  // Estado de reproducción y sincronización
  const [currentTime, setCurrentTime] = useState(0);
  const [activeLineIdx, setActiveLineIdx] = useState(-1);
  const lyricsRef = useRef(null);

  // Estado del panel IA
  const [selectedPhrase, setSelectedPhrase] = useState(null);
  const [panelOpen, setPanelOpen] = useState(false);

  // Cargar datos de la canción
  useEffect(() => {
    if (!songId) return;

    const load = async () => {
      setLoading(true);
      try {
        const [songData, lyricsRes, lessonRes, statsRes] = await Promise.allSettled([
          songsApi.getById(songId),
          songsApi.getLyrics(songId),
          songsApi.getLessonType(songId),
          progressApi.getStats(),
        ]);

        if (songData.status === 'fulfilled') setSong(songData.value?.song);
        if (lyricsRes.status === 'fulfilled') {
          const ld = lyricsRes.value;
          setLyricsData(ld);
          if (ld.synced) {
            setParsedLines(parseLRC(ld.lyrics));
          }
        }
        if (lessonRes.status === 'fulfilled') setLessonInfo(lessonRes.value?.lesson);
        if (statsRes.status === 'fulfilled') setStats(statsRes.value);
      } catch (err) {
        setError('Error al cargar la canción');
      } finally {
        setLoading(false);
      }
    };

    load();
  }, [songId]);

  // Sincronizar highlight con el tiempo del player
  useEffect(() => {
    if (parsedLines.length === 0) return;

    let idx = -1;
    for (let i = 0; i < parsedLines.length; i++) {
      if (parsedLines[i].time <= currentTime) idx = i;
      else break;
    }

    if (idx !== activeLineIdx) {
      setActiveLineIdx(idx);
      // Auto-scroll a la línea activa
      const lineEl = lyricsRef.current?.querySelector(`[data-line="${idx}"]`);
      lineEl?.scrollIntoView({ behavior: 'smooth', block: 'center' });
    }
  }, [currentTime, parsedLines, activeLineIdx]);

  const handleTimeUpdate = useCallback((seconds) => {
    setCurrentTime(seconds);
  }, []);

  const handlePhraseSelect = (text) => {
    if (!text.trim()) return;
    setSelectedPhrase(text.trim());
    setPanelOpen(true);
  };

  // Dividir letra plana en líneas para PhraseSelector
  const plainLines = lyricsData?.lyrics && !lyricsData.synced
    ? lyricsData.lyrics.split('\n').filter(l => l.trim())
    : [];

  if (loading) {
    return (
      <div className="min-h-screen bg-brand-dark flex items-center justify-center">
        <div className="text-center">
          <div className="w-12 h-12 rounded-full border-4 border-brand-green border-t-transparent animate-spin mx-auto" />
          <p className="text-brand-text mt-4">Cargando lección...</p>
        </div>
      </div>
    );
  }

  if (error || !song) {
    return (
      <div className="min-h-screen bg-brand-dark flex items-center justify-center">
        <div className="text-center">
          <p className="text-white text-xl mb-4">⚠️ {error || 'Canción no encontrada'}</p>
          <button onClick={() => router.push('/search')} className="text-brand-green hover:underline">
            ← Volver a buscar
          </button>
        </div>
      </div>
    );
  }

  const displayLines = parsedLines.length > 0
    ? parsedLines
    : plainLines.map((text) => ({ text, time: null }));

  return (
    <div className="min-h-screen bg-brand-dark flex flex-col">
      <Navbar />

      <div className="flex flex-1 overflow-hidden">

        {/* ── Columna principal ── */}
        <div className={`flex flex-col flex-1 overflow-y-auto transition-all duration-300 ${panelOpen ? 'w-[60%]' : 'w-full'}`}>
          <div className="max-w-2xl mx-auto w-full px-4 py-6 space-y-6">

            {/* Info de la canción */}
            <div className="flex items-center gap-4">
              {song.imageUrl && (
                <img src={song.imageUrl} alt={song.album} className="w-16 h-16 rounded-xl object-cover flex-shrink-0" />
              )}
              <div className="min-w-0">
                <h1 className="text-white font-bold text-xl truncate">{song.title}</h1>
                <p className="text-brand-text truncate">{song.artist}</p>
              </div>
              <button onClick={() => router.push('/search')} className="ml-auto text-brand-text hover:text-white text-sm flex-shrink-0">
                ← Volver
              </button>
            </div>

            {/* Tipo de lección */}
            {lessonInfo && (
              <div className="bg-brand-card rounded-xl px-4 py-3 border border-white/5 animate-fadeIn">
                <div className="flex items-center gap-2">
                  <span className="text-brand-green text-lg">
                    {lessonInfo.type === 'vocabulary' ? '📚' : lessonInfo.type === 'grammar' ? '✏️' : '🎤'}
                  </span>
                  <div>
                    <p className="text-white text-sm font-medium">{lessonInfo.description}</p>
                    <p className="text-brand-text text-xs mt-0.5">{lessonInfo.focus}</p>
                  </div>
                </div>
              </div>
            )}

            {/* Player de Spotify */}
            <SpotifyEmbedPlayer spotifyId={songId} onTimeUpdate={handleTimeUpdate} />

            {/* Progress Widget */}
            {stats && <ProgressWidget stats={stats} />}

            {/* Letra */}
            <div className="bg-brand-card rounded-xl p-6 border border-white/5">
              <div className="flex items-center justify-between mb-4">
                <h2 className="text-white font-semibold">Letra</h2>
                <div className="flex items-center gap-2">
                  {lyricsData?.synced && (
                    <span className="text-brand-green text-xs bg-brand-green/20 px-2 py-1 rounded-full">
                      ⚡ Sincronizada
                    </span>
                  )}
                  <span className="text-brand-text text-xs">Clic en una línea para analizarla</span>
                </div>
              </div>

              {displayLines.length > 0 ? (
                <div ref={lyricsRef} className="space-y-1">
                  {displayLines.map((line, i) => (
                    <p
                      key={i}
                      data-line={i}
                      onClick={() => handlePhraseSelect(line.text)}
                      className={`px-3 py-2 rounded-lg cursor-pointer text-base leading-relaxed transition-all duration-300 ${
                        i === activeLineIdx
                          ? 'bg-brand-green/20 text-brand-green font-semibold scale-[1.01]'
                          : 'text-white/70 hover:text-white hover:bg-brand-hover'
                      }`}
                    >
                      {line.text || '\u00A0'}
                    </p>
                  ))}
                </div>
              ) : (
                <div className="text-center py-8">
                  <p className="text-brand-text">No encontramos la letra de esta canción.</p>
                  <p className="text-brand-text text-sm mt-1">Intenta con otra canción.</p>
                </div>
              )}
            </div>

          </div>
        </div>

        {/* ── Panel IA (lateral) ── */}
        {panelOpen && selectedPhrase && (
          <div className="w-[40%] min-w-[320px] max-w-[480px] flex-shrink-0 border-l border-white/10 animate-fadeIn overflow-hidden">
            <IAPanel
              songId={songId}
              phrase={selectedPhrase}
              onClose={() => setPanelOpen(false)}
            />
          </div>
        )}

      </div>
    </div>
  );
}
