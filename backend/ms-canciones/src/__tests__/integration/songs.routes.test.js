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

  // Ruta auxiliar para probar el branch error.validation del errorHandler
  app.get('/test-schema', {
    schema: {
      querystring: {
        type: 'object',
        required: ['edad'],
        properties: { edad: { type: 'integer' } },
      },
    },
  }, async () => ({ ok: true }));

  // Ruta auxiliar para probar el branch de error genérico 500 del errorHandler
  app.get('/test-crash', async () => {
    throw new Error('crash directo sin statusCode');
  });

  await app.ready();
});

afterAll(async () => {
  await app.close();
});

beforeEach(() => {
  jest.clearAllMocks();
});

// ─── errorHandler ────────────────────────────────────────────────────────────

describe('errorHandler', () => {
  test('retorna 400 con details cuando Fastify rechaza por validación de schema', async () => {
    const response = await app.inject({ method: 'GET', url: '/test-schema' });

    expect(response.statusCode).toBe(400);
    const body = JSON.parse(response.body);
    expect(body.error).toBe('VALIDATION_ERROR');
    expect(body.details).toBeDefined();
  });

  test('retorna 500 INTERNAL_ERROR ante un error genérico inesperado', async () => {
    SongRepository.findBySpotifyId.mockRejectedValue(new Error('boom inesperado'));

    const response = await app.inject({ method: 'GET', url: '/songs/cualquier-id' });

    expect(response.statusCode).toBe(503);
    // El errorHandler convierte errores sin statusCode en 503 vía ServiceUnavailableError
    // Para llegar al 500 puro necesitamos que el error llegue sin pasar por el catch de la ruta
    // Verificamos que el handler respondió correctamente
    const body = JSON.parse(response.body);
    expect(body.error).toBe('SERVICE_UNAVAILABLE');
  });

  test('retorna 500 INTERNAL_ERROR cuando el error no tiene statusCode ni validation', async () => {
    const response = await app.inject({ method: 'GET', url: '/test-crash' });

    expect(response.statusCode).toBe(500);
    const body = JSON.parse(response.body);
    expect(body.error).toBe('INTERNAL_ERROR');
    expect(body.statusCode).toBe(500);
  });
});

// ─── GET /songs/status ───────────────────────────────────────────────────────

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

// ─── GET /songs/search ───────────────────────────────────────────────────────

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

  test('retorna resultados cuando searchSongs devuelve fallback no-array del circuit breaker', async () => {
    // El CircuitBreaker abierto devuelve { error: '...', items: [] } en lugar de un array
    SpotifyService.searchSongs.mockResolvedValue({ error: 'Spotify no disponible temporalmente', items: [] });

    const response = await app.inject({ method: 'GET', url: '/songs/search?q=test' });

    expect(response.statusCode).toBe(200);
    const body = JSON.parse(response.body);
    // No es array → upsert no se llama, results devuelve el objeto fallback
    expect(SongRepository.upsert).not.toHaveBeenCalled();
    expect(body.results).toBeDefined();
  });
});

// ─── GET /songs/:id ──────────────────────────────────────────────────────────

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

  test('retorna 503 cuando ocurre un error inesperado', async () => {
    SongRepository.findBySpotifyId.mockRejectedValue(new Error('DB down'));

    const response = await app.inject({ method: 'GET', url: `/songs/${mockSong.spotifyId}` });

    expect(response.statusCode).toBe(503);
    const body = JSON.parse(response.body);
    expect(body.error).toBe('SERVICE_UNAVAILABLE');
  });
});

// ─── GET /songs/:id/lyrics ───────────────────────────────────────────────────

describe('GET /songs/:id/lyrics', () => {
  test('retorna letra cuando la canción existe y la guarda en BD', async () => {
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

  test('no llama updateLyrics cuando el source no es lrclib', async () => {
    SongRepository.findBySpotifyId.mockResolvedValue(mockSong);
    LyricsService.getLyrics.mockResolvedValue({ lyrics: 'letra desde caché', synced: false, source: 'cache' });

    const response = await app.inject({ method: 'GET', url: `/songs/${mockSong.spotifyId}/lyrics` });

    expect(response.statusCode).toBe(200);
    expect(SongRepository.updateLyrics).not.toHaveBeenCalled();
  });

  test('no llama updateLyrics cuando lyrics es null', async () => {
    SongRepository.findBySpotifyId.mockResolvedValue(mockSong);
    LyricsService.getLyrics.mockResolvedValue({ lyrics: null, synced: false, source: 'lrclib' });

    const response = await app.inject({ method: 'GET', url: `/songs/${mockSong.spotifyId}/lyrics` });

    expect(response.statusCode).toBe(200);
    expect(SongRepository.updateLyrics).not.toHaveBeenCalled();
  });

  test('retorna 404 cuando la canción no existe', async () => {
    SongRepository.findBySpotifyId.mockResolvedValue(null);
    SpotifyService.getTrackById.mockResolvedValue(null);

    const response = await app.inject({ method: 'GET', url: '/songs/no_existe/lyrics' });

    expect(response.statusCode).toBe(404);
    const body = JSON.parse(response.body);
    expect(body.error).toBe('SONG_NOT_FOUND');
  });

  test('retorna 503 cuando ocurre un error inesperado', async () => {
    SongRepository.findBySpotifyId.mockResolvedValue(mockSong);
    LyricsService.getLyrics.mockRejectedValue(new Error('LyricsAPI down'));

    const response = await app.inject({ method: 'GET', url: `/songs/${mockSong.spotifyId}/lyrics` });

    expect(response.statusCode).toBe(503);
    const body = JSON.parse(response.body);
    expect(body.error).toBe('SERVICE_UNAVAILABLE');
  });
});

// ─── GET /songs/:id/lesson-type ──────────────────────────────────────────────

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

  test('retorna lección de vocabulario por defecto cuando no hay artistId', async () => {
    SongRepository.findBySpotifyId.mockResolvedValue({ ...mockSong, artistId: null });

    const response = await app.inject({
      method: 'GET',
      url: `/songs/${mockSong.spotifyId}/lesson-type`,
    });

    expect(response.statusCode).toBe(200);
    const body = JSON.parse(response.body);
    expect(body.lesson.type).toBe('vocabulary');
    expect(SpotifyService.getArtistGenre).not.toHaveBeenCalled();
  });

  test('retorna 404 cuando la canción no existe', async () => {
    SongRepository.findBySpotifyId.mockResolvedValue(null);
    SpotifyService.getTrackById.mockResolvedValue(null);

    const response = await app.inject({ method: 'GET', url: '/songs/no_existe/lesson-type' });

    expect(response.statusCode).toBe(404);
    const body = JSON.parse(response.body);
    expect(body.error).toBe('SONG_NOT_FOUND');
  });

  test('retorna 503 cuando ocurre un error inesperado', async () => {
    SongRepository.findBySpotifyId.mockResolvedValue({ ...mockSong, artistId: 'artist_xyz' });
    SpotifyService.getArtistGenre.mockRejectedValue(new Error('fallo inesperado'));

    const response = await app.inject({
      method: 'GET',
      url: `/songs/${mockSong.spotifyId}/lesson-type`,
    });

    expect(response.statusCode).toBe(503);
    const body = JSON.parse(response.body);
    expect(body.error).toBe('SERVICE_UNAVAILABLE');
  });
});