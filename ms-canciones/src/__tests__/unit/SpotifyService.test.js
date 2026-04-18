const { mockSpotifyTrack, mockSpotifySearchResponse, mockTokenResponse } = require('../fixtures/songs.fixtures');

let SpotifyService;
let mockAxios;

beforeEach(() => {
  jest.resetModules();
  mockAxios = { get: jest.fn(), post: jest.fn() };
  jest.doMock('axios', () => mockAxios);
  process.env.SPOTIFY_CLIENT_ID = 'test_client_id';
  process.env.SPOTIFY_CLIENT_SECRET = 'test_client_secret';
  SpotifyService = require('../../services/SpotifyService');
});

describe('SpotifyService', () => {
  describe('searchSongs', () => {
    test('retorna lista de canciones mapeadas desde Spotify', async () => {
      mockAxios.post.mockResolvedValueOnce({ data: mockTokenResponse });
      mockAxios.get.mockResolvedValueOnce({ data: mockSpotifySearchResponse });

      const results = await SpotifyService.searchSongs('test song', 5);

      expect(mockAxios.get).toHaveBeenCalledWith(
        'https://api.spotify.com/v1/search',
        expect.objectContaining({ params: expect.objectContaining({ q: 'test song', limit: 5 }) })
      );
      expect(results).toHaveLength(1);
      expect(results[0]).toMatchObject({
        spotifyId: 'track_abc123',
        title: 'Test Song',
        artist: 'Test Artist',
      });
    });

    test('retorna fallback cuando Spotify falla repetidamente', async () => {
      mockAxios.post.mockResolvedValue({ data: mockTokenResponse });
      mockAxios.get.mockRejectedValue(new Error('Network error'));

      await SpotifyService.searchSongs('q', 5);
      await SpotifyService.searchSongs('q', 5);
      const result = await SpotifyService.searchSongs('q', 5);

      expect(result).toMatchObject({ error: expect.any(String) });
    });
  });

  describe('getTrackById', () => {
    test('retorna metadatos mapeados de una canción', async () => {
      mockAxios.post.mockResolvedValueOnce({ data: mockTokenResponse });
      mockAxios.get.mockResolvedValueOnce({ data: mockSpotifyTrack });

      const result = await SpotifyService.getTrackById('track_abc123');

      expect(result).toMatchObject({
        spotifyId: 'track_abc123',
        title: 'Test Song',
        artist: 'Test Artist',
        album: 'Test Album',
      });
    });

    test('retorna null como fallback cuando el servicio falla', async () => {
      mockAxios.post.mockResolvedValue({ data: mockTokenResponse });
      mockAxios.get.mockRejectedValue(new Error('Not found'));

      await SpotifyService.getTrackById('x');
      await SpotifyService.getTrackById('x');
      const result = await SpotifyService.getTrackById('x');

      expect(result).toBeNull();
    });
  });

  describe('getArtistGenre', () => {
    test('retorna el primer género del artista', async () => {
      mockAxios.post.mockResolvedValueOnce({ data: mockTokenResponse });
      mockAxios.get.mockResolvedValueOnce({ data: { genres: ['pop', 'indie'] } });

      const genre = await SpotifyService.getArtistGenre('artist_xyz');

      expect(genre).toBe('pop');
    });

    test('retorna string vacío cuando el artista no tiene géneros', async () => {
      mockAxios.post.mockResolvedValueOnce({ data: mockTokenResponse });
      mockAxios.get.mockResolvedValueOnce({ data: { genres: [] } });

      const genre = await SpotifyService.getArtistGenre('artist_xyz');

      expect(genre).toBe('');
    });
  });

  describe('getCircuitState', () => {
    test('retorna el estado actual del CircuitBreaker', () => {
      const state = SpotifyService.getCircuitState();

      expect(state).toMatchObject({
        name: 'Spotify',
        state: 'CLOSED',
        failureCount: 0,
      });
    });
  });
});
