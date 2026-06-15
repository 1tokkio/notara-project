const { mockLyricsResult } = require('../fixtures/songs.fixtures');

let LyricsService;
let mockAxios;
let mockGetRedis;
const mockRedis = { get: jest.fn(), setEx: jest.fn() };

beforeEach(() => {
  jest.resetModules();
  mockAxios = { get: jest.fn() };
  mockGetRedis = jest.fn().mockReturnValue(mockRedis);
  jest.doMock('axios', () => mockAxios);
  jest.doMock('../../database/redis', () => ({ getRedis: mockGetRedis }));
  mockRedis.get.mockReset();
  mockRedis.setEx.mockReset();
  LyricsService = require('../../services/LyricsService');
});

describe('LyricsService', () => {
  describe('getLyrics', () => {
    test('retorna letra desde caché de Redis si existe', async () => {
      mockRedis.get.mockResolvedValue(JSON.stringify(mockLyricsResult));

      const result = await LyricsService.getLyrics('track_abc123', 'Test Song', 'Test Artist');

      expect(mockRedis.get).toHaveBeenCalledWith('lyrics:track_abc123');
      expect(mockAxios.get).not.toHaveBeenCalled();
      expect(result).toEqual(mockLyricsResult);
    });

    test('consulta LRCLIB cuando no hay caché y guarda resultado', async () => {
      mockRedis.get.mockResolvedValue(null);
      mockRedis.setEx.mockResolvedValue('OK');
      mockAxios.get.mockResolvedValueOnce({
        data: { syncedLyrics: '[00:10.00] Hello world', plainLyrics: null },
      });

      const result = await LyricsService.getLyrics('track_abc123', 'Test Song', 'Test Artist');

      expect(mockAxios.get).toHaveBeenCalledWith(
        'https://lrclib.net/api/get',
        expect.objectContaining({ params: { track_name: 'Test Song', artist_name: 'Test Artist' } })
      );
      expect(result.synced).toBe(true);
      expect(result.source).toBe('lrclib');
      expect(mockRedis.setEx).toHaveBeenCalled();
    });

    test('usa plainLyrics cuando no hay letra sincronizada', async () => {
      mockRedis.get.mockResolvedValue(null);
      mockRedis.setEx.mockResolvedValue('OK');
      mockAxios.get.mockResolvedValueOnce({
        data: { syncedLyrics: null, plainLyrics: 'Hello world\nThis is a test' },
      });

      const result = await LyricsService.getLyrics('track_abc123', 'Test Song', 'Test Artist');

      expect(result.synced).toBe(false);
      expect(result.source).toBe('lrclib');
      expect(result.lyrics).toBe('Hello world\nThis is a test');
    });

    test('retorna not_found cuando LRCLIB no tiene la letra', async () => {
      mockRedis.get.mockResolvedValue(null);
      mockAxios.get.mockResolvedValueOnce({
        data: { syncedLyrics: null, plainLyrics: null },
      });

      const result = await LyricsService.getLyrics('track_abc123', 'Test Song', 'Test Artist');

      expect(result).toEqual({ lyrics: null, synced: false, source: 'not_found' });
      expect(mockRedis.setEx).not.toHaveBeenCalled();
    });

    test('continúa sin caché cuando Redis no está disponible', async () => {
      mockGetRedis.mockImplementation(() => { throw new Error('Redis no disponible'); });
      mockAxios.get.mockResolvedValueOnce({
        data: { syncedLyrics: null, plainLyrics: 'Hello world' },
      });

      const result = await LyricsService.getLyrics('track_abc123', 'Test Song', 'Test Artist');

      expect(result.source).toBe('lrclib');
      expect(result.lyrics).toBe('Hello world');
    });

    test('retorna circuit_open cuando el CircuitBreaker está abierto', async () => {
      mockRedis.get.mockResolvedValue(null);
      mockAxios.get.mockRejectedValue(new Error('LRCLIB no disponible'));

      await LyricsService.getLyrics('a', 'T1', 'A1');
      await LyricsService.getLyrics('b', 'T2', 'A2');
      const result = await LyricsService.getLyrics('c', 'T3', 'A3');

      expect(result).toEqual({ lyrics: null, synced: false, source: 'circuit_open' });
    });
  });

  describe('getCircuitState', () => {
    test('retorna el estado del CircuitBreaker de letras', () => {
      const state = LyricsService.getCircuitState();

      expect(state).toMatchObject({
        name: 'LyricsAPI',
        state: 'CLOSED',
      });
    });
  });
});
