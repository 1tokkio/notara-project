jest.mock('../../services/SpotifyService');
jest.mock('../../services/LyricsService');
jest.mock('../../repositories/SongRepository');

const Fastify = require('fastify');
const songRoutes = require('../../routes/songs');
const registerErrorHandler = require('../../middleware/errorHandler');
const SpotifyService = require('../../services/SpotifyService');
const LyricsService = require('../../services/LyricsService');
const SongRepository = require('../../repositories/SongRepository');
const { mockSong, mockLyricsResult } = require('../fixtures/songs.fixtures');

let app;

beforeAll(async () => {
  app = Fastify({ logger: false });
  registerErrorHandler(app);
  await app.register(songRoutes, { prefix: '/songs' });
  await app.ready();
});

afterAll(async () => {
  await app.close();
});

beforeEach(() => {
  jest.clearAllMocks();
});

describe('GET /songs/status', () => {
  test('retorna el estado de los Circuit Breakers', async () => {
    SpotifyService.getCircuitState.mockReturnValue({ name: 'Spotify', state: 'CLOSED', failureCount: 0 });
    LyricsService.getCircuitState.mockReturnValue({ name: 'LyricsAPI', state: 'CLOSED', failureCount: 0 });

    const response = await app.inject({ method: 'GET', url: '/songs/status' });

    expect(response.statusCode).toBe(200);
    const body = JSON.parse(response.body);
    expect(body.service).toBe('ms-canciones');
    expect(body.circuitBreakers.spotify.state).toBe('CLOSED');
    expect(body.circuitBreakers.lyrics.state).toBe('CLOSED');
  });
});

describe('GET /songs/search', () => {
  test('retorna resultados de búsqueda para una query válida', async () => {
    SpotifyService.searchSongs.mockResolvedValue([mockSong]);
    SongRepository.upsert.mockResolvedValue(mockSong);

    const response = await app.inject({ method: 'GET', url: '/songs/search?q=test+song' });

    expect(response.statusCode).toBe(200);
    const body = JSON.parse(response.body);
    expect(body.query).toBe('test song');
    expect(body.results).toHaveLength(1);
    expect(body.results[0].spotifyId).toBe(mockSong.spotifyId);
  });

  test('retorna 400 cuando falta el parámetro q', async () => {
    const response = await app.inject({ method: 'GET', url: '/songs/search' });

    expect(response.statusCode).toBe(400);
    const body = JSON.parse(response.body);
    expect(body.error).toBe('VALIDATION_ERROR');
  });

  test('retorna 400 cuando q está vacío', async () => {
    const response = await app.inject({ method: 'GET', url: '/songs/search?q=' });

    expect(response.statusCode).toBe(400);
  });

  test('retorna 503 cuando Spotify no está disponible', async () => {
    SpotifyService.searchSongs.mockRejectedValue(new Error('Spotify down'));

    const response = await app.inject({ method: 'GET', url: '/songs/search?q=test' });

    expect(response.statusCode).toBe(503);
  });
});

describe('GET /songs/:id', () => {
  test('retorna canción desde la base de datos si ya existe', async () => {
    SongRepository.findBySpotifyId.mockResolvedValue(mockSong);

    const response = await app.inject({ method: 'GET', url: `/songs/${mockSong.spotifyId}` });

    expect(response.statusCode).toBe(200);
    const body = JSON.parse(response.body);
    expect(body.song.spotifyId).toBe(mockSong.spotifyId);
    expect(SpotifyService.getTrackById).not.toHaveBeenCalled();
  });

  test('consulta Spotify y guarda si la canción no está en BD', async () => {
    SongRepository.findBySpotifyId.mockResolvedValue(null);
    SpotifyService.getTrackById.mockResolvedValue(mockSong);
    SongRepository.upsert.mockResolvedValue(mockSong);

    const response = await app.inject({ method: 'GET', url: `/songs/${mockSong.spotifyId}` });

    expect(response.statusCode).toBe(200);
    expect(SpotifyService.getTrackById).toHaveBeenCalledWith(mockSong.spotifyId);
    expect(SongRepository.upsert).toHaveBeenCalled();
  });

  test('retorna 404 cuando la canción no existe en ningún lado', async () => {
    SongRepository.findBySpotifyId.mockResolvedValue(null);
    SpotifyService.getTrackById.mockResolvedValue(null);

    const response = await app.inject({ method: 'GET', url: '/songs/no_existe' });

    expect(response.statusCode).toBe(404);
    const body = JSON.parse(response.body);
    expect(body.error).toBe('SONG_NOT_FOUND');
  });

  test('retorna 503 cuando el servicio falla inesperadamente', async () => {
    SongRepository.findBySpotifyId.mockRejectedValue(new Error('DB down'));

    const response = await app.inject({ method: 'GET', url: `/songs/${mockSong.spotifyId}` });

    expect(response.statusCode).toBe(503);
  });
});

describe('GET /songs/:id/lyrics', () => {
  test('retorna letra cuando la canción existe', async () => {
    SongRepository.findBySpotifyId.mockResolvedValue(mockSong);
    LyricsService.getLyrics.mockResolvedValue(mockLyricsResult);
    SongRepository.updateLyrics.mockResolvedValue(mockSong);

    const response = await app.inject({ method: 'GET', url: `/songs/${mockSong.spotifyId}/lyrics` });

    expect(response.statusCode).toBe(200);
    const body = JSON.parse(response.body);
    expect(body.spotifyId).toBe(mockSong.spotifyId);
    expect(body.lyrics).toBe(mockLyricsResult.lyrics);
    expect(body.synced).toBe(true);
  });

  test('retorna 404 cuando la canción no existe', async () => {
    SongRepository.findBySpotifyId.mockResolvedValue(null);
    SpotifyService.getTrackById.mockResolvedValue(null);

    const response = await app.inject({ method: 'GET', url: '/songs/no_existe/lyrics' });

    expect(response.statusCode).toBe(404);
    const body = JSON.parse(response.body);
    expect(body.error).toBe('SONG_NOT_FOUND');
  });
});

describe('GET /songs/:id/lesson-type', () => {
  test('retorna el tipo de lección según el género', async () => {
    SongRepository.findBySpotifyId.mockResolvedValue({ ...mockSong, artistId: 'artist_xyz' });
    SpotifyService.getArtistGenre.mockResolvedValue('hip-hop');

    const response = await app.inject({
      method: 'GET',
      url: `/songs/${mockSong.spotifyId}/lesson-type`,
    });

    expect(response.statusCode).toBe(200);
    const body = JSON.parse(response.body);
    expect(body.lesson.type).toBe('pronunciation');
    expect(body.genre).toBe('hip-hop');
  });

  test('retorna lección de vocabulario por defecto cuando no hay género', async () => {
    SongRepository.findBySpotifyId.mockResolvedValue({ ...mockSong, artistId: null });

    const response = await app.inject({
      method: 'GET',
      url: `/songs/${mockSong.spotifyId}/lesson-type`,
    });

    expect(response.statusCode).toBe(200);
    const body = JSON.parse(response.body);
    expect(body.lesson.type).toBe('vocabulary');
  });

  test('retorna 404 cuando la canción no existe', async () => {
    SongRepository.findBySpotifyId.mockResolvedValue(null);
    SpotifyService.getTrackById.mockResolvedValue(null);

    const response = await app.inject({ method: 'GET', url: '/songs/no_existe/lesson-type' });

    expect(response.statusCode).toBe(404);
    const body = JSON.parse(response.body);
    expect(body.error).toBe('SONG_NOT_FOUND');
  });
});
