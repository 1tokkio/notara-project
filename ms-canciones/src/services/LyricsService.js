<<<<<<< HEAD
/**
 * LyricsService
 *
 * Obtiene letras de canciones desde LRCLIB (API pública, sin autenticación).
 * Implementa caché con Redis para evitar llamadas repetidas.
 * Protegido por CircuitBreaker.
 *
 * LRCLIB API: https://lrclib.net/api
 */

const axios = require('axios');
const CircuitBreaker = require('../patterns/CircuitBreaker');
const { getRedis } = require('../database/redis');

const lyricsCircuit = new CircuitBreaker('LyricsAPI', {
  failureThreshold: 3,
  resetTimeout: 60_000, // 1 minuto (la API de letras es menos crítica)
});

const CACHE_TTL = 60 * 60 * 24 * 7; // 7 días en segundos
const CACHE_PREFIX = 'lyrics:';

/**
 * Obtiene la letra de una canción.
 * Primero revisa el caché de Redis; si no está, consulta LRCLIB.
 *
 * @param {string} spotifyId - ID de Spotify (usado como clave de caché)
 * @param {string} title     - Título de la canción
 * @param {string} artist    - Nombre del artista
 * @returns {Promise<{lyrics: string|null, synced: boolean, source: string}>}
 */
=======
const axios = require('axios');
const CircuitBreaker = require('../patterns/CircuitBreaker');
const { getRedis } = require('../database/redis');
const config = require('../config/config');

const lyricsCircuit = new CircuitBreaker('LyricsAPI', config.circuitBreaker.lyrics);

const CACHE_TTL = config.cache.lyricsTtlSeconds;
const CACHE_PREFIX = 'lyrics:';

>>>>>>> origin/panxo
const getLyrics = async (spotifyId, title, artist) => {
  const cacheKey = `${CACHE_PREFIX}${spotifyId}`;

  // 1. Intentar obtener del caché Redis
  try {
    const redis = getRedis();
    const cached = await redis.get(cacheKey);
    if (cached) {
      console.info(`[LyricsService] Cache HIT para ${spotifyId}`);
      return JSON.parse(cached);
    }
  } catch (err) {
    console.warn('[LyricsService] Redis no disponible, continuando sin caché:', err.message);
  }

  // 2. Consultar LRCLIB con protección del CircuitBreaker
  const result = await lyricsCircuit.execute(
    async () => {
      const response = await axios.get('https://lrclib.net/api/get', {
        params: {
          track_name: title,
          artist_name: artist,
        },
        timeout: 8000,
      });

      const data = response.data;

      // LRCLIB retorna syncedLyrics (con timestamps LRC) o plainLyrics
      if (data.syncedLyrics) {
        return { lyrics: data.syncedLyrics, synced: true, source: 'lrclib' };
      } else if (data.plainLyrics) {
        return { lyrics: data.plainLyrics, synced: false, source: 'lrclib' };
      }

      return { lyrics: null, synced: false, source: 'not_found' };
    },
    () => ({ lyrics: null, synced: false, source: 'circuit_open' })
  );

  // 3. Guardar en caché Redis si se encontraron letras
  if (result.lyrics) {
    try {
      const redis = getRedis();
      await redis.setEx(cacheKey, CACHE_TTL, JSON.stringify(result));
      console.info(`[LyricsService] Guardado en caché para ${spotifyId}`);
    } catch (err) {
      console.warn('[LyricsService] No se pudo guardar en caché:', err.message);
    }
  }

  return result;
};

const getCircuitState = () => lyricsCircuit.getState();

module.exports = { getLyrics, getCircuitState };
