<<<<<<< HEAD
/**
 * Rutas de canciones — ms-canciones
 *
 * GET /songs/search?q=         → Búsqueda en Spotify
 * GET /songs/:id               → Metadatos de una canción
 * GET /songs/:id/lyrics        → Letra desde LRCLIB + caché Redis
 * GET /songs/:id/lesson-type   → Tipo de lección (usa LessonFactory)
 * GET /songs/status            → Estado de los Circuit Breakers
 */

=======
>>>>>>> origin/panxo
const SpotifyService = require('../services/SpotifyService');
const LyricsService = require('../services/LyricsService');
const SongRepository = require('../repositories/SongRepository');
const { LessonFactory } = require('../patterns/LessonFactory');
<<<<<<< HEAD

async function songRoutes(fastify, options) {
  // ──────────────────────────────────────────────────────────────────────────
  // GET /songs/status — Estado de los Circuit Breakers (útil para monitoreo)
  // ──────────────────────────────────────────────────────────────────────────
=======
const SongNotFoundError = require('../exceptions/SongNotFoundError');
const ServiceUnavailableError = require('../exceptions/ServiceUnavailableError');
const ValidationError = require('../exceptions/ValidationError');

async function songRoutes(fastify, options) {
>>>>>>> origin/panxo
  fastify.get('/status', async (request, reply) => {
    return {
      service: 'ms-canciones',
      circuitBreakers: {
        spotify: SpotifyService.getCircuitState(),
        lyrics: LyricsService.getCircuitState(),
      },
    };
  });

<<<<<<< HEAD
  // ──────────────────────────────────────────────────────────────────────────
  // GET /songs/search?q=query&limit=10
  // Busca canciones en Spotify y guarda metadatos en MongoDB
  // ──────────────────────────────────────────────────────────────────────────
=======
>>>>>>> origin/panxo
  fastify.get('/search', async (request, reply) => {
    const { q, limit = 10 } = request.query;

    if (!q || q.trim() === '') {
<<<<<<< HEAD
      return reply.status(400).send({ error: 'El parámetro q es requerido' });
=======
      throw new ValidationError('El parámetro q es requerido');
>>>>>>> origin/panxo
    }

    try {
      const results = await SpotifyService.searchSongs(q.trim(), parseInt(limit));

<<<<<<< HEAD
      // Guardar en MongoDB en background (no bloqueamos la respuesta)
=======
>>>>>>> origin/panxo
      if (Array.isArray(results)) {
        results.forEach((song) => {
          SongRepository.upsert(song).catch((err) =>
            fastify.log.warn({ err }, 'Error guardando canción en MongoDB')
          );
        });
      }

      return { query: q, results: results || [] };
    } catch (err) {
<<<<<<< HEAD
      fastify.log.error({ err }, 'Error en búsqueda de canciones');
      return reply.status(503).send({
        error: 'Servicio de búsqueda no disponible temporalmente',
        message: err.message,
      });
    }
  });

  // ──────────────────────────────────────────────────────────────────────────
  // GET /songs/:id — Metadatos de una canción
  // Primero busca en MongoDB (caché), si no va a Spotify
  // ──────────────────────────────────────────────────────────────────────────
=======
      if (err.statusCode) throw err;
      fastify.log.error({ err }, 'Error en búsqueda de canciones');
      throw new ServiceUnavailableError('Spotify');
    }
  });

>>>>>>> origin/panxo
  fastify.get('/:id', async (request, reply) => {
    const { id } = request.params;

    try {
<<<<<<< HEAD
      // 1. Intentar desde MongoDB
      let song = await SongRepository.findBySpotifyId(id);

      if (!song) {
        // 2. Si no está en BD, buscar en Spotify
        const spotifyData = await SpotifyService.getTrackById(id);

        if (!spotifyData) {
          return reply.status(404).send({ error: 'Canción no encontrada' });
        }

        // Guardar en MongoDB para futuras consultas
=======
      let song = await SongRepository.findBySpotifyId(id);

      if (!song) {
        const spotifyData = await SpotifyService.getTrackById(id);
        if (!spotifyData) throw new SongNotFoundError(id);
>>>>>>> origin/panxo
        song = await SongRepository.upsert(spotifyData);
      }

      return { song };
    } catch (err) {
<<<<<<< HEAD
      fastify.log.error({ err }, `Error obteniendo canción ${id}`);
      return reply.status(503).send({
        error: 'Servicio no disponible temporalmente',
        message: err.message,
      });
    }
  });

  // ──────────────────────────────────────────────────────────────────────────
  // GET /songs/:id/lyrics — Letra de la canción
  // Usa caché Redis → LRCLIB → devuelve null si no hay
  // ──────────────────────────────────────────────────────────────────────────
=======
      if (err.statusCode) throw err;
      fastify.log.error({ err }, `Error obteniendo canción ${id}`);
      throw new ServiceUnavailableError('ms-canciones');
    }
  });

>>>>>>> origin/panxo
  fastify.get('/:id/lyrics', async (request, reply) => {
    const { id } = request.params;

    try {
<<<<<<< HEAD
      // Obtener metadatos de la canción para la búsqueda de letras
=======
>>>>>>> origin/panxo
      let song = await SongRepository.findBySpotifyId(id);

      if (!song) {
        const spotifyData = await SpotifyService.getTrackById(id);
<<<<<<< HEAD
        if (!spotifyData) {
          return reply.status(404).send({ error: 'Canción no encontrada' });
        }
        song = await SongRepository.upsert(spotifyData);
      }

      const { lyrics, synced, source } = await LyricsService.getLyrics(
        id,
        song.title,
        song.artist
      );

      // Si se encontraron letras nuevas, actualizar en MongoDB
=======
        if (!spotifyData) throw new SongNotFoundError(id);
        song = await SongRepository.upsert(spotifyData);
      }

      const { lyrics, synced, source } = await LyricsService.getLyrics(id, song.title, song.artist);

>>>>>>> origin/panxo
      if (lyrics && source === 'lrclib') {
        SongRepository.updateLyrics(id, lyrics).catch((err) =>
          fastify.log.warn({ err }, 'Error actualizando lyrics en MongoDB')
        );
      }

<<<<<<< HEAD
      return {
        spotifyId: id,
        title: song.title,
        artist: song.artist,
        lyrics,
        synced,
        source,
      };
    } catch (err) {
      fastify.log.error({ err }, `Error obteniendo letras de ${id}`);
      return reply.status(503).send({
        error: 'Servicio de letras no disponible temporalmente',
        message: err.message,
      });
    }
  });

  // ──────────────────────────────────────────────────────────────────────────
  // GET /songs/:id/lesson-type — Tipo de lección (LessonFactory)
  // Determina qué tipo de lección corresponde a la canción según su género
  // ──────────────────────────────────────────────────────────────────────────
=======
      return { spotifyId: id, title: song.title, artist: song.artist, lyrics, synced, source };
    } catch (err) {
      if (err.statusCode) throw err;
      fastify.log.error({ err }, `Error obteniendo letras de ${id}`);
      throw new ServiceUnavailableError('LyricsAPI');
    }
  });

>>>>>>> origin/panxo
  fastify.get('/:id/lesson-type', async (request, reply) => {
    const { id } = request.params;

    try {
<<<<<<< HEAD
      // Obtener datos de la canción
=======
>>>>>>> origin/panxo
      let song = await SongRepository.findBySpotifyId(id);

      if (!song) {
        const spotifyData = await SpotifyService.getTrackById(id);
<<<<<<< HEAD
        if (!spotifyData) {
          return reply.status(404).send({ error: 'Canción no encontrada' });
        }
        song = await SongRepository.upsert(spotifyData);
      }

      // Obtener el género del artista desde Spotify para determinar el tipo de lección
=======
        if (!spotifyData) throw new SongNotFoundError(id);
        song = await SongRepository.upsert(spotifyData);
      }

>>>>>>> origin/panxo
      let genre = '';
      if (song.artistId) {
        genre = await SpotifyService.getArtistGenre(song.artistId);
      }

<<<<<<< HEAD
      // Usar el Factory Method para crear la lección apropiada
=======
>>>>>>> origin/panxo
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
<<<<<<< HEAD
      fastify.log.error({ err }, `Error determinando tipo de lección para ${id}`);
      return reply.status(503).send({
        error: 'No se pudo determinar el tipo de lección',
        message: err.message,
      });
=======
      if (err.statusCode) throw err;
      fastify.log.error({ err }, `Error determinando tipo de lección para ${id}`);
      throw new ServiceUnavailableError('ms-canciones');
>>>>>>> origin/panxo
    }
  });
}

module.exports = songRoutes;
