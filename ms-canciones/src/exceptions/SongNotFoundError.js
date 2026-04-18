const AppError = require('./AppError');

class SongNotFoundError extends AppError {
  constructor(id) {
    super(`Canción con ID '${id}' no encontrada`, 404, 'SONG_NOT_FOUND');
  }
}

module.exports = SongNotFoundError;
