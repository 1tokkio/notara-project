jest.mock('../../repositories/SongModel');

const Song = require('../../repositories/SongModel');
const SongRepository = require('../../repositories/SongRepository');
const { mockSong } = require('../fixtures/songs.fixtures');

describe('SongRepository', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('findBySpotifyId', () => {
    test('retorna la canción cuando existe en la base de datos', async () => {
      Song.findOne.mockResolvedValue(mockSong);

      const result = await SongRepository.findBySpotifyId('track_abc123');

      expect(Song.findOne).toHaveBeenCalledWith({ spotifyId: 'track_abc123' });
      expect(result).toEqual(mockSong);
    });

    test('retorna null cuando la canción no existe', async () => {
      Song.findOne.mockResolvedValue(null);

      const result = await SongRepository.findBySpotifyId('no_existe');

      expect(result).toBeNull();
    });

    test('propaga errores de la base de datos', async () => {
      Song.findOne.mockRejectedValue(new Error('DB error'));

      await expect(SongRepository.findBySpotifyId('track_abc123')).rejects.toThrow('DB error');
    });
  });

  describe('upsert', () => {
    test('crea o actualiza una canción con upsert', async () => {
      Song.findOneAndUpdate.mockResolvedValue(mockSong);

      const result = await SongRepository.upsert(mockSong);

      expect(Song.findOneAndUpdate).toHaveBeenCalledWith(
        { spotifyId: mockSong.spotifyId },
        expect.objectContaining({ spotifyId: mockSong.spotifyId }),
        { upsert: true, new: true, setDefaultsOnInsert: true }
      );
      expect(result).toEqual(mockSong);
    });

    test('separa spotifyId del resto de los datos', async () => {
      Song.findOneAndUpdate.mockResolvedValue(mockSong);

      await SongRepository.upsert({ spotifyId: 'track_abc123', title: 'Test', artist: 'Artist' });

      const callArgs = Song.findOneAndUpdate.mock.calls[0];
      expect(callArgs[0]).toEqual({ spotifyId: 'track_abc123' });
      expect(callArgs[1]).toMatchObject({ spotifyId: 'track_abc123', title: 'Test' });
    });
  });

  describe('updateLyrics', () => {
    test('actualiza la letra y la fecha de actualización', async () => {
      const updatedSong = { ...mockSong, lyrics: 'Hello world', lyricsUpdatedAt: new Date() };
      Song.findOneAndUpdate.mockResolvedValue(updatedSong);

      const result = await SongRepository.updateLyrics('track_abc123', 'Hello world');

      expect(Song.findOneAndUpdate).toHaveBeenCalledWith(
        { spotifyId: 'track_abc123' },
        expect.objectContaining({ lyrics: 'Hello world', lyricsUpdatedAt: expect.any(Date) }),
        { new: true }
      );
      expect(result).toEqual(updatedSong);
    });
  });

  describe('findAll', () => {
    test('retorna lista de canciones ordenadas por fecha', async () => {
      const mockFind = { sort: jest.fn().mockReturnThis(), limit: jest.fn().mockResolvedValue([mockSong]) };
      Song.find.mockReturnValue(mockFind);

      const result = await SongRepository.findAll();

      expect(Song.find).toHaveBeenCalled();
      expect(mockFind.sort).toHaveBeenCalledWith({ createdAt: -1 });
      expect(mockFind.limit).toHaveBeenCalledWith(50);
      expect(result).toEqual([mockSong]);
    });
  });
});
