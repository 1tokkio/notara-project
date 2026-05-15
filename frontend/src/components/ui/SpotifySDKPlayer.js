'use client';
import { useEffect, useRef, useState } from 'react';

/**
 * SpotifySDKPlayer — reproduce la pista completa usando el Web Playback SDK.
 * Requiere Spotify Premium. Carga el script del SDK, inicializa el player,
 * transfiere la reproducción al dispositivo "Notara Player" y emite el tiempo
 * actual cada segundo vía onTimeUpdate para sincronizar el highlight de letra.
 *
 * @param {string}   spotifyId    — ID de la pista en Spotify
 * @param {string}   token        — access_token de Spotify (OAuth)
 * @param {function} onTimeUpdate — callback(seconds) para sincronizar letra
 */
export default function SpotifySDKPlayer({ spotifyId, token, onTimeUpdate }) {
  const playerRef    = useRef(null);
  const deviceIdRef  = useRef(null);
  const pollRef      = useRef(null);

  const [isReady,  setIsReady]  = useState(false);
  const [isPaused, setIsPaused] = useState(true);
  const [error,    setError]    = useState(null);

  useEffect(() => {
    if (!token || !spotifyId) return;

    // Carga el SDK solo una vez
    const loadSDK = () =>
      new Promise((resolve) => {
        if (window.Spotify) { resolve(); return; }
        window.onSpotifyWebPlaybackSDKReady = resolve;
        if (!document.getElementById('spotify-sdk-script')) {
          const script = document.createElement('script');
          script.id    = 'spotify-sdk-script';
          script.src   = 'https://sdk.scdn.co/spotify-player.js';
          script.async = true;
          document.body.appendChild(script);
        }
      });

    const init = async () => {
      await loadSDK();

      const player = new window.Spotify.Player({
        name: 'Notara Player',
        getOAuthToken: (cb) => cb(token),
        volume: 0.8,
      });
      playerRef.current = player;

      player.addListener('ready', async ({ device_id }) => {
        deviceIdRef.current = device_id;
        setIsReady(true);
        setError(null);

        // Transferir reproducción al SDK player y empezar la pista
        try {
          await fetch(
            `https://api.spotify.com/v1/me/player/play?device_id=${device_id}`,
            {
              method:  'PUT',
              headers: {
                Authorization:  `Bearer ${token}`,
                'Content-Type': 'application/json',
              },
              body: JSON.stringify({ uris: [`spotify:track:${spotifyId}`] }),
            }
          );
          setIsPaused(false);
        } catch {
          setError('No se pudo iniciar la reproducción. Intentá de nuevo.');
        }
      });

      player.addListener('not_ready', () => setIsReady(false));

      player.addListener('player_state_changed', (state) => {
        if (!state) return;
        setIsPaused(state.paused);
        onTimeUpdate?.(state.position / 1000);
      });

      player.addListener('authentication_error', () => {
        setError('Token de Spotify expirado. Reconectá tu cuenta.');
        localStorage.removeItem('spotify_token');
        localStorage.removeItem('spotify_refresh');
      });

      player.addListener('account_error', () => {
        setError('Se requiere Spotify Premium para reproducción completa.');
      });

      player.connect();

      // Poll cada segundo para actualizar la posición suavemente
      pollRef.current = setInterval(async () => {
        const state = await player.getCurrentState().catch(() => null);
        if (state && !state.paused) {
          onTimeUpdate?.(state.position / 1000);
        }
      }, 1000);
    };

    init();

    return () => {
      clearInterval(pollRef.current);
      playerRef.current?.disconnect();
    };
  }, [token, spotifyId]); // eslint-disable-line react-hooks/exhaustive-deps

  const togglePlay = () => playerRef.current?.togglePlay();

  if (error) {
    return (
      <div className="w-full rounded-xl bg-red-900/20 border border-red-500/30 p-4 text-center">
        <p className="text-red-400 text-sm">{error}</p>
        <button
          onClick={() => {
            localStorage.removeItem('spotify_token');
            localStorage.removeItem('spotify_refresh');
            window.location.reload();
          }}
          className="mt-2 text-xs text-red-300 underline"
        >
          Reconectar cuenta
        </button>
      </div>
    );
  }

  if (!isReady) {
    return (
      <div className="w-full rounded-xl bg-brand-card border border-white/5 p-6 flex items-center justify-center gap-3">
        <div className="w-5 h-5 rounded-full border-2 border-brand-green border-t-transparent animate-spin flex-shrink-0" />
        <p className="text-brand-text text-sm">Conectando con Spotify...</p>
      </div>
    );
  }

  return (
    <div className="w-full rounded-xl bg-brand-card border border-white/5 p-4 flex items-center gap-4">
      {/* Botón play/pause */}
      <button
        onClick={togglePlay}
        aria-label={isPaused ? 'Reproducir' : 'Pausar'}
        className="w-12 h-12 rounded-full bg-brand-green flex items-center justify-center flex-shrink-0 hover:scale-105 active:scale-95 transition-transform"
      >
        {isPaused ? (
          <svg className="w-5 h-5 text-black ml-0.5" fill="currentColor" viewBox="0 0 24 24">
            <path d="M8 5v14l11-7z" />
          </svg>
        ) : (
          <svg className="w-5 h-5 text-black" fill="currentColor" viewBox="0 0 24 24">
            <path d="M6 19h4V5H6v14zm8-14v14h4V5h-4z" />
          </svg>
        )}
      </button>

      {/* Estado */}
      <div className="flex-1 min-w-0">
        <p className="text-white text-sm font-medium truncate">Reproduciendo en Notara</p>
        <p className="text-brand-green text-xs mt-0.5 flex items-center gap-1.5">
          <span className="w-1.5 h-1.5 rounded-full bg-brand-green animate-pulse inline-block" />
          Spotify Premium conectado
        </p>
      </div>

      {/* Desconectar */}
      <button
        onClick={() => {
          localStorage.removeItem('spotify_token');
          localStorage.removeItem('spotify_refresh');
          window.location.reload();
        }}
        title="Desconectar Spotify"
        className="text-brand-text hover:text-white text-xs flex-shrink-0 transition-colors"
      >
        Desconectar
      </button>
    </div>
  );
}
