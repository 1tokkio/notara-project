const SpotifyService = require('../services/SpotifyService');
const LyricsService = require('../services/LyricsService');
const SongRepository = require('../repositories/SongRepository');
const { LessonFactory } = require('../patterns/LessonFactory');
const SongNotFoundError = require('../exceptions/SongNotFoundError');
const ServiceUnavailableError = require('../exceptions/ServiceUnavailableError');
const ValidationError = require('../exceptions/ValidationError');

async function songRoutes(fastify, options) {
  fastify.get('/status', async (request, reply) => {
    return {
      service: 'ms-canciones',
      circuitBreakers: {
        spotify: SpotifyService.getCircuitState(),
        lyrics: LyricsService.getCircuitState(),
      },
    };
  });

  fastify.get('/search', async (request, reply) => {
    const { q, limit = 10 } = request.query;

    if (!q || q.trim() === '') {
      throw new ValidationError('El parámetro q es requerido');
    }

    try {
      const results = await SpotifyService.searchSongs(q.trim(), parseInt(limit));

      if (Array.isArray(results)) {
        results.forEach((song) => {
          SongRepository.upsert(song).catch((err) =>
            fastify.log.warn({ err }, 'Error guardando canción en MongoDB')
          );
        });
      }

      return { query: q, results: results || [] };
    } catch (err) {
      if (err.statusCode) throw err;
      fastify.log.error({ err }, 'Error en búsqueda de canciones');
      throw new ServiceUnavailableError('Spotify');
    }
  });

  fastify.get('/:id', async (request, reply) => {
    const { id } = request.params;

    try {
      let song = await SongRepository.findBySpotifyId(id);

      if (!song) {
        const spotifyData = await SpotifyService.getTrackById(id);
        if (!spotifyData) throw new SongNotFoundError(id);
        song = await SongRepository.upsert(spotifyData);
      }

      return { song };
    } catch (err) {
      if (err.statusCode) throw err;
      fastify.log.error({ err }, `Error obteniendo canción ${id}`);
      throw new ServiceUnavailableError('ms-canciones');
    }
  });

  fastify.get('/:id/lyrics', async (request, reply) => {
    const { id } = request.params;

    try {
      let song = await SongRepository.findBySpotifyId(id);

      if (!song) {
        const spotifyData = await SpotifyService.getTrackById(id);
        if (!spotifyData) throw new SongNotFoundError(id);
        song = await SongRepository.upsert(spotifyData);
      }

      const { lyrics, synced, source } = await LyricsService.getLyrics(id, song.title, song.artist);

      if (lyrics && source === 'lrclib') {
        SongRepository.updateLyrics(id, lyrics).catch((err) =>
          fastify.log.warn({ err }, 'Error actualizando lyrics en MongoDB')
        );
      }

      return { spotifyId: id, title: song.title, artist: song.artist, lyrics, synced, source };
    } catch (err) {
      if (err.statusCode) throw err;
      fastify.log.error({ err }, `Error obteniendo letras de ${id}`);
      throw new ServiceUnavailableError('LyricsAPI');
    }
  });

  fastify.get('/:id/lesson-type', async (request, reply) => {
    const { id } = request.params;

    try {
      let song = await SongRepository.findBySpotifyId(id);

      if (!song) {
        const spotifyData = await SpotifyService.getTrackById(id);
        if (!spotifyData) throw new SongNotFoundError(id);
        song = await SongRepository.upsert(spotifyData);
      }

      let genre = '';
      if (song.artistId) {
        genre = await SpotifyService.getArtistGenre(song.artistId);
      }

      const lesson = LessonFactory.create(genre, id, song.title, song.artist);

      return {
        spotifyId: id,
        title: song.title,
        artist: song.artist,
        genre,
        lesson: {
          type: lesson.type,
          focus: lesson.focus,
          description: lesson.describe(),
          exercises: lesson.exercises,
          createdAt: lesson.createdAt,
        },
      };
    } catch (err) {
      if (err.statusCode) throw err;
      fastify.log.error({ err }, `Error determinando tipo de lección para ${id}`);
      throw new ServiceUnavailableError('ms-canciones');
    }
  });
}

module.exports = songRoutes;
