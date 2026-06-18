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

## Caché Redis

| Clave | TTL | Datos cacheados | Por qué |
|-------|-----|----------------|---------|
| `lyrics:{spotifyId}` | **7 días** | Letra completa de la canción (LRC sincronizado o texto plano) | Las letras no cambian y la API externa (lrclib.net) tiene límite de requests |

El caché se gestiona en `src/services/LyricsService.js` usando `redis.setEx(key, CACHE_TTL, data)`.
El TTL de 7 días está configurado en `src/config/config.js` → `cache.lyricsTtlSeconds`.

## Swagger / OpenAPI

La documentación interactiva está disponible en:

```
http://localhost:3002/docs
```

## Docker

```bash
docker build -t notara-ms-canciones .
docker run -p 3002:3002 --env-file .env notara-ms-canciones
```
