/**
 * SongRepository — Repository Pattern
 *
 * Desacopla la lógica de negocio del motor de base de datos (MongoDB).
 * Si en el futuro se migra a otro motor, solo se modifica esta clase.
 */
const Song = require('./SongModel');

class SongRepository {
  /**
   * Busca una canción por su Spotify ID.
   * @param {string} spotifyId
   * @returns {Promise<Song|null>}
   */
  async findBySpotifyId(spotifyId) {
    try {
      return await Song.findOne({ spotifyId });
    } catch (e) {
      return null;
    }
  }

  /**
   * Guarda o actualiza una canción en la base de datos.
   * Usa upsert para evitar duplicados.
   * @param {object} songData
   * @returns {Promise<Song>}
   */
  async upsert(songData) {
    try {
      const { spotifyId, ...rest } = songData;
      return await Song.findOneAndUpdate(
        { spotifyId },
        { spotifyId, ...rest },
        { upsert: true, new: true, setDefaultsOnInsert: true }
      );
    } catch (e) {
      return songData;
    }
  }

  /**
   * Actualiza la letra de una canción existente.
   * @param {string} spotifyId
   * @param {string} lyrics
   * @returns {Promise<Song>}
   */
  async updateLyrics(spotifyId, lyrics) {
    try {
      return await Song.findOneAndUpdate(
        { spotifyId },
        { lyrics, lyricsUpdatedAt: new Date() },
        { new: true }
      );
    } catch (e) {
      return null;
    }
  }

  /**
   * Lista todas las canciones (útil para debugging/admin).
   * @returns {Promise<Song[]>}
   */
  async findAll() {
    try {
      return await Song.find().sort({ createdAt: -1 }).limit(50);
    } catch (e) {
      return [];
    }
  }
}

module.exports = new SongRepository();
