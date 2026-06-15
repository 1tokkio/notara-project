const axios = require('axios');
const CircuitBreaker = require('../patterns/CircuitBreaker');
const config = require('../config/config');

const spotifyCircuit = new CircuitBreaker('Spotify', config.circuitBreaker.spotify);

let accessToken = null;
let tokenExpiresAt = 0;

const getAccessToken = async () => {
  if (accessToken && Date.now() < tokenExpiresAt) return accessToken;

  const clientId = config.spotify.clientId;
  const clientSecret = config.spotify.clientSecret;

  if (!clientId || !clientSecret) {
    throw new Error('Spotify credentials no configuradas en .env');
  }

  const credentials = Buffer.from(`${clientId}:${clientSecret}`).toString('base64');

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
    accessToken = response.data.access_token;
    tokenExpiresAt = Date.now() + response.data.expires_in * 1000 - 60000;
    return accessToken;
  } catch (err) {
    console.error('Error token:', err.response?.data || err.message);
    throw err;
  }
};

const searchSongs = async (query, limit = 10) => {
  return spotifyCircuit.execute(
    async () => {
      const token = await getAccessToken();
      const response = await axios.get('https://api.spotify.com/v1/search', {
        headers: { Authorization: `Bearer ${token}` },
        params: { q: query, type: 'track', limit, market: 'CL' },
      });
      return response.data.tracks.items.map(mapTrack);
    },
    () => ({ error: 'Spotify no disponible temporalmente', items: [] })
  );
};

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
