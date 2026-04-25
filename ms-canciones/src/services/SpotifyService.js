<<<<<<< HEAD
/**
 * SpotifyService
 *
 * Gestiona la autenticación Client Credentials con Spotify y provee
 * métodos para buscar canciones y obtener sus metadatos.
 * Todas las llamadas externas están protegidas por CircuitBreaker.
 */

const axios = require('axios');
const CircuitBreaker = require('../patterns/CircuitBreaker');

const spotifyCircuit = new CircuitBreaker('Spotify', {
  failureThreshold: 3,
  resetTimeout: 30_000,
});
=======
const axios = require('axios');
const CircuitBreaker = require('../patterns/CircuitBreaker');
const config = require('../config/config');

const spotifyCircuit = new CircuitBreaker('Spotify', config.circuitBreaker.spotify);
>>>>>>> origin/panxo

let accessToken = null;
let tokenExpiresAt = 0;

<<<<<<< HEAD
/**
 * Obtiene un token de acceso usando el flujo Client Credentials.
 * El token se cachea en memoria hasta que expire.
 */
const getAccessToken = async () => {
  if (accessToken && Date.now() < tokenExpiresAt) return accessToken;

  const clientId = process.env.SPOTIFY_CLIENT_ID;
  const clientSecret = process.env.SPOTIFY_CLIENT_SECRET;
=======
const getAccessToken = async () => {
  if (accessToken && Date.now() < tokenExpiresAt) return accessToken;

  const clientId = config.spotify.clientId;
  const clientSecret = config.spotify.clientSecret;

  console.log('CLIENT_ID:', clientId);
  console.log('CLIENT_SECRET:', clientSecret ? 'exists' : 'MISSING');
>>>>>>> origin/panxo

  if (!clientId || !clientSecret) {
    throw new Error('Spotify credentials no configuradas en .env');
  }

  const credentials = Buffer.from(`${clientId}:${clientSecret}`).toString('base64');

<<<<<<< HEAD
  const response = await axios.post(
    'https://accounts.spotify.com/api/token',
    'grant_type=client_credentials',
    {
      headers: {
        Authorization: `Basic ${credentials}`,
        'Content-Type': 'application/x-www-form-urlencoded',
      },
    }
  );

  accessToken = response.data.access_token;
  tokenExpiresAt = Date.now() + response.data.expires_in * 1000 - 60_000; // renovar 1 min antes
  return accessToken;
};

/**
 * Busca canciones en Spotify.
 * @param {string} query - Término de búsqueda
 * @param {number} limit - Resultados máximos (default 10)
 * @returns {Promise<Array>}
 */
=======
  try {
    const response = await axios.post(
      'https://accounts.spotify.com/api/token',
      'grant_type=client_credentials',
      {
        headers: {
          Authorization: `Basic ${credentials}`,
          'Content-Type': 'application/x-www-form-urlencoded',
        },
      }
    );
    console.log('Token obtenido OK');
    accessToken = response.data.access_token;
    tokenExpiresAt = Date.now() + response.data.expires_in * 1000 - 60000;
    return accessToken;
  } catch (err) {
    console.error('Error token:', err.response?.data || err.message);
    throw err;
  }
};

>>>>>>> origin/panxo
const searchSongs = async (query, limit = 10) => {
  return spotifyCircuit.execute(
    async () => {
      const token = await getAccessToken();
      const response = await axios.get('https://api.spotify.com/v1/search', {
        headers: { Authorization: `Bearer ${token}` },
        params: { q: query, type: 'track', limit, market: 'CL' },
      });
<<<<<<< HEAD

=======
>>>>>>> origin/panxo
      return response.data.tracks.items.map(mapTrack);
    },
    () => ({ error: 'Spotify no disponible temporalmente', items: [] })
  );
};

<<<<<<< HEAD
/**
 * Obtiene los metadatos de una canción por su ID de Spotify.
 * @param {string} spotifyId
 * @returns {Promise<object>}
 */
=======
>>>>>>> origin/panxo
const getTrackById = async (spotifyId) => {
  return spotifyCircuit.execute(
    async () => {
      const token = await getAccessToken();
      const response = await axios.get(`https://api.spotify.com/v1/tracks/${spotifyId}`, {
        headers: { Authorization: `Bearer ${token}` },
        params: { market: 'CL' },
      });
      return mapTrack(response.data);
    },
    () => null
  );
};

<<<<<<< HEAD
/**
 * Obtiene el género de un artista (necesario para LessonFactory).
 * @param {string} artistId
 * @returns {Promise<string>} Primer género o string vacío
 */
=======
>>>>>>> origin/panxo
const getArtistGenre = async (artistId) => {
  return spotifyCircuit.execute(
    async () => {
      const token = await getAccessToken();
      const response = await axios.get(`https://api.spotify.com/v1/artists/${artistId}`, {
        headers: { Authorization: `Bearer ${token}` },
      });
      return response.data.genres?.[0] || '';
    },
    () => ''
  );
};

<<<<<<< HEAD
/**
 * Mapea un track de Spotify a la estructura interna.
 */
=======
>>>>>>> origin/panxo
const mapTrack = (track) => ({
  spotifyId: track.id,
  title: track.name,
  artist: track.artists.map((a) => a.name).join(', '),
  artistId: track.artists[0]?.id,
  album: track.album?.name,
  duration: track.duration_ms,
  imageUrl: track.album?.images?.[0]?.url || null,
  previewUrl: track.preview_url || null,
  spotifyUrl: track.external_urls?.spotify || null,
});

const getCircuitState = () => spotifyCircuit.getState();

module.exports = { searchSongs, getTrackById, getArtistGenre, getCircuitState };
