# ms-canciones

Microservicio Node.js/Fastify para búsqueda de canciones, letras y tipo de lección.

## Stack

- **Fastify** — servidor HTTP
- **MongoDB** — persistencia de canciones
- **Redis** — caché de letras
- **Spotify API** — búsqueda y metadatos
- **LRCLib** — letras sincronizadas (LRC)

## Puertos

| Servicio | Puerto |
|----------|--------|
| ms-canciones | 3002 |
| MongoDB  | 27017 |
| Redis    | 6379  |

## Endpoints

```
GET /search?q=query&limit=10    Buscar canciones en Spotify
GET /:id                         Metadatos de una canción
GET /:id/lyrics                  Letra de la canción (con caché en Redis)
GET /:id/lesson-type             Tipo de lección según género
GET /status                      Estado de los circuit breakers
GET /health                      Health check
```

## Patrones implementados

- **Factory Method** (`patterns/LessonFactory.js`) — determina el tipo de lección según el género musical
- **Circuit Breaker** (`patterns/CircuitBreaker.js`) — protege las llamadas a Spotify y LRCLib
- **Repository** (`repositories/SongRepository.js`) — abstrae el acceso a MongoDB

## Variables de entorno

```env
SPOTIFY_CLIENT_ID=
SPOTIFY_CLIENT_SECRET=
MONGO_URI=mongodb://localhost:27017/linguaflow
REDIS_URL=redis://localhost:6379
PORT=3002
```

## Tests

```bash
npm test              # todos los tests
npm run test:unit     # solo unitarios
npm run test:integration  # solo integración
```

Los tests cubren: CircuitBreaker, LessonFactory, SpotifyService, SongRepository, LyricsService y rutas HTTP.
