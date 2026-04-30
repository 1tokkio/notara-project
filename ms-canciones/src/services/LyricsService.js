const axios = require('axios');
const CircuitBreaker = require('../patterns/CircuitBreaker');
const { getRedis } = require('../database/redis');
const config = require('../config/config');

const lyricsCircuit = new CircuitBreaker('LyricsAPI', config.circuitBreaker.lyrics);

const CACHE_TTL = config.cache.lyricsTtlSeconds;
const CACHE_PREFIX = 'lyrics:';

const getLyrics = async (spotifyId, title, artist) => {
  const cacheKey = `${CACHE_PREFIX}${spotifyId}`;

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

      if (data.syncedLyrics) {
        return { lyrics: data.syncedLyrics, synced: true, source: 'lrclib' };
      } else if (data.plainLyrics) {
        return { lyrics: data.plainLyrics, synced: false, source: 'lrclib' };
      }

      return { lyrics: null, synced: false, source: 'not_found' };
    },
    () => ({ lyrics: null, synced: false, source: 'circuit_open' })
  );

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
