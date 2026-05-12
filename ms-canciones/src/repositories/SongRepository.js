const Song = require('./SongModel');

class SongRepository {
  async findBySpotifyId(spotifyId) {
    try {
      return await Song.findOne({ spotifyId });
    } catch {
      return null;
    }
  }

  async upsert(songData) {
    try {
      const { spotifyId, ...rest } = songData;
      return await Song.findOneAndUpdate(
        { spotifyId },
        { spotifyId, ...rest },
        { upsert: true, new: true, setDefaultsOnInsert: true }
      );
    } catch {
      return songData;
    }
  }

  async updateLyrics(spotifyId, lyrics) {
    try {
      return await Song.findOneAndUpdate(
        { spotifyId },
        { lyrics, lyricsUpdatedAt: new Date() },
        { new: true }
      );
    } catch {
      return null;
    }
  }

  async findAll() {
    try {
      return await Song.find().sort({ createdAt: -1 }).limit(50);
    } catch {
      return [];
    }
  }
}

module.exports = new SongRepository();
