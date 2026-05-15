'use client';
import { useState, useRef, useEffect } from 'react';

export default function AudioPlayer({ previewUrl, songTitle, spotifyId }) {
  const audioRef = useRef(null);
  const [playing, setPlaying]   = useState(false);
  const [current, setCurrent]   = useState(0);
  const [duration, setDuration] = useState(30);
  const [volume, setVolume]     = useState(0.8);

  useEffect(() => {
    const audio = audioRef.current;
    if (!audio) return;
    const onTime  = () => setCurrent(audio.currentTime);
    const onMeta  = () => setDuration(audio.duration || 30);
    const onEnded = () => setPlaying(false);
    audio.addEventListener('timeupdate', onTime);
    audio.addEventListener('loadedmetadata', onMeta);
    audio.addEventListener('ended', onEnded);
    return () => {
      audio.removeEventListener('timeupdate', onTime);
      audio.removeEventListener('loadedmetadata', onMeta);
      audio.removeEventListener('ended', onEnded);
    };
  }, [previewUrl]);

  const toggle = () => {
    const audio = audioRef.current;
    if (!audio) return;
    if (playing) { audio.pause(); setPlaying(false); }
    else         { audio.play();  setPlaying(true);  }
  };

  const seek = (e) => {
    const audio = audioRef.current;
    if (!audio) return;
    const rect = e.currentTarget.getBoundingClientRect();
    const pct  = (e.clientX - rect.left) / rect.width;
    audio.currentTime = pct * duration;
  };

  const changeVolume = (e) => {
    const v = parseFloat(e.target.value);
    setVolume(v);
    if (audioRef.current) audioRef.current.volume = v;
  };

  const fmt = (s) => `${Math.floor(s / 60)}:${String(Math.floor(s % 60)).padStart(2, '0')}`;
  const pct  = duration > 0 ? (current / duration) * 100 : 0;

  if (!previewUrl) {
    return (
      <div className="w-full rounded-xl bg-brand-hover border border-white/5 p-3 text-center">
        <p className="text-brand-text text-xs mb-2">Preview no disponible</p>
        <a
          href={`https://open.spotify.com/track/${spotifyId}`}
          target="_blank"
          rel="noopener noreferrer"
          className="text-brand-green text-xs hover:underline"
        >
          Abrir en Spotify
        </a>
      </div>
    );
  }

  return (
    <div className="w-full rounded-xl bg-brand-hover border border-white/5 p-3 space-y-2">
      <audio ref={audioRef} src={previewUrl} preload="metadata" />

      {/* Barra de progreso */}
      <div
        className="h-1.5 bg-brand-card rounded-full cursor-pointer group relative"
        onClick={seek}
      >
        <div
          className="h-full bg-brand-green rounded-full transition-all relative"
          style={{ width: `${pct}%` }}
        >
          <span className="absolute right-0 top-1/2 -translate-y-1/2 w-3 h-3 bg-white rounded-full opacity-0 group-hover:opacity-100 transition-opacity shadow" />
        </div>
      </div>

      {/* Controles */}
      <div className="flex items-center gap-3">
        <button
          onClick={toggle}
          className="w-8 h-8 rounded-full bg-brand-green hover:bg-green-400 text-black flex items-center justify-center transition-colors flex-shrink-0"
        >
          {playing ? (
            <svg width="10" height="12" viewBox="0 0 10 12" fill="currentColor">
              <rect x="0" y="0" width="3" height="12" rx="1"/>
              <rect x="7" y="0" width="3" height="12" rx="1"/>
            </svg>
          ) : (
            <svg width="10" height="12" viewBox="0 0 10 12" fill="currentColor">
              <path d="M0 0l10 6-10 6V0z"/>
            </svg>
          )}
        </button>

        <span className="text-brand-text text-[10px] tabular-nums">{fmt(current)}</span>
        <span className="text-brand-text text-[10px]">/</span>
        <span className="text-brand-text text-[10px] tabular-nums">{fmt(duration)}</span>

        <div className="flex-1" />

        {/* Volumen */}
        <input
          type="range" min="0" max="1" step="0.05"
          value={volume}
          onChange={changeVolume}
          className="w-16 h-1 accent-brand-green cursor-pointer"
        />
      </div>

      <p className="text-brand-text text-[10px] text-center opacity-50">
        Preview 30s · <a href={`https://open.spotify.com/track/${spotifyId}`} target="_blank" rel="noopener noreferrer" className="hover:text-brand-green transition-colors">Abrir en Spotify</a>
      </p>
    </div>
  );
}
