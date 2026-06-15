const mockSong = {
  spotifyId: 'track_abc123',
  title: 'Test Song',
  artist: 'Test Artist',
  artistId: 'artist_xyz',
  album: 'Test Album',
  duration: 210000,
  imageUrl: 'https://example.com/image.jpg',
  previewUrl: null,
  lyrics: null,
  lyricsUpdatedAt: null,
};

const mockSpotifyTrack = {
  id: 'track_abc123',
  name: 'Test Song',
  artists: [{ name: 'Test Artist', id: 'artist_xyz' }],
  album: {
    name: 'Test Album',
    images: [{ url: 'https://example.com/image.jpg' }],
  },
  duration_ms: 210000,
  preview_url: null,
  external_urls: { spotify: 'https://open.spotify.com/track/track_abc123' },
};

const mockSpotifySearchResponse = {
  tracks: {
    items: [mockSpotifyTrack],
  },
};

const mockTokenResponse = {
  access_token: 'mock_access_token',
  expires_in: 3600,
};

const mockLyricsResult = {
  lyrics: '[00:10.00] Hello world\n[00:15.00] This is a test',
  synced: true,
  source: 'lrclib',
};

module.exports = {
  mockSong,
  mockSpotifyTrack,
  mockSpotifySearchResponse,
  mockTokenResponse,
  mockLyricsResult,
};
