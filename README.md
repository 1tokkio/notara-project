# Notara

Plataforma web para aprender inglés a través de canciones. El usuario busca una canción en Spotify, selecciona frases de la letra y recibe explicaciones, notas gramaticales y ejercicios generados por inteligencia artificial.

---

## Arquitectura

```
┌─────────────┐     ┌───────────────┐     ┌──────────────────┐
│   Frontend  │────▶│  API Gateway  │────▶│  ms-usuarios     │
│  (Next.js)  │     │  (Express)    │     │  (Spring Boot)   │
└─────────────┘     │   :3000       │     │  Auth + JWT       │
                    │               │────▶│  :3001           │
                    └───────────────┘     └──────────────────┘
                                          ┌──────────────────┐
                                     ────▶│  ms-canciones    │
                                          │  (Fastify)       │
                                          │  Spotify + Lyrics│
                                          │  :3002           │
                                          └──────────────────┘
                                          ┌──────────────────┐
                                     ────▶│  ms-notas-metas  │
                                          │  (Spring Boot)   │
                                          │  Progreso + IA   │
                                          │  :3003           │
                                          └──────────────────┘
```

---

## Patrones de diseño implementados

### Backend — ms-canciones

| Patrón | Archivo | Problema que resuelve |
|--------|---------|-----------------------|
| **Factory Method** | `ms-canciones/src/patterns/LessonFactory.js` | La creación de un objeto `Lesson` varía según el género musical (pop, rock, reggaeton…). Sin el patrón, la ruta HTTP contendría un bloque `switch` acoplado a los detalles de construcción. La fábrica encapsula esa lógica: la ruta solo llama `LessonFactory.create(genre)` y recibe el objeto correcto. Agregar un género nuevo implica solo añadir un caso en la fábrica, sin tocar la ruta. |
| **Circuit Breaker** | `ms-canciones/src/patterns/CircuitBreaker.js` | Spotify y LRCLib son servicios externos que pueden fallar. Sin protección, un timeout de 30 s en Spotify bloquearía cada petición del usuario durante ese tiempo y agotaría el pool de conexiones. El Circuit Breaker detecta fallos consecutivos, abre el circuito y devuelve un error inmediato hasta que el servicio se recupera, protegiendo la disponibilidad del microservicio. |
| **Repository** | `ms-canciones/src/repositories/SongRepository.js` | Las rutas necesitan persistir y consultar canciones en MongoDB, pero no deberían conocer el esquema Mongoose ni las queries. El Repository expone métodos de dominio (`findById`, `upsert`) y oculta todos los detalles de la base de datos. Si se migra de MongoDB a otra BD, solo cambia el Repository, no las rutas. |

### Frontend — Next.js

| Patrón | Archivo | Problema que resuelve |
|--------|---------|-----------------------|
| **Strategy** | `frontend/src/patterns/LyricsDisplayStrategy.js` | La letra puede mostrarse en cuatro modos: solo inglés, solo español, bilingüe y sincronizada. Sin el patrón, `LessonPage` tendría bloques `if/else` anidados mezclando lógica de presentación con lógica de negocio. Cada modo se define como una estrategia con interfaz `{ id, label, render(props) }`. El componente solo llama `getLyricsStrategy(id).render(props)` sin conocer los detalles de ningún modo. |
| **Observer / Context** | `frontend/src/context/AuthContext.js` | El estado de autenticación (usuario, token) es necesario en componentes distantes del árbol (Navbar, páginas protegidas, llamadas a la API). Sin Context, habría que pasar el usuario por props en cada nivel. `AuthContext` implementa el patrón Observer: los componentes se suscriben con `useAuth()` y se re-renderizan automáticamente cuando el estado cambia. |
| **Facade** | `frontend/src/lib/api.js` | El frontend realiza docenas de llamadas HTTP distintas (auth, búsqueda, letra, progreso…). Sin una fachada, cada componente construiría su propia URL, gestionaría headers de autorización y manejaría errores de red de forma independiente. `api.js` expone funciones de alto nivel (`searchSongs`, `getLyrics`, `saveProgress`) y oculta todos los detalles de `fetch` y tokens JWT. |

### Arquitectura

| Patrón | Ubicación | Problema que resuelve |
|--------|-----------|-----------------------|
| **API Gateway** | `api-gateway/` | Sin un punto de entrada único, el frontend necesitaría conocer la URL de cada microservicio, gestionar CORS por separado y replicar la validación de tokens en cada uno. El Gateway centraliza el enrutamiento, la autenticación y la traducción de errores. |
| **BFF (Backend for Frontend)** | `bff/` | El panel de IA requiere orquestar múltiples llamadas (letra + contexto del usuario + modelo de IA) y transformar la respuesta a un formato específico del cliente. Colocar esa lógica en el frontend lo haría frágil y difícil de cachear. El BFF actúa como intermediario especializado para ese caso de uso. |

---

## Stack tecnológico

| Capa | Tecnología |
|------|------------|
| Frontend | Next.js 14, React, Tailwind CSS |
| API Gateway | Node.js, Express |
| Microservicio canciones | Node.js, Fastify, Mongoose |
| Microservicio usuarios | Java 17, Spring Boot, Spring Security, JWT |
| Microservicio notas/metas | Java 17, Spring Boot |
| BFF | Java 17, Spring Boot |
| Base de datos | MongoDB (canciones), PostgreSQL (usuarios) |
| Caché | Redis |
| Descubrimiento de servicios | Netflix Eureka |
| APIs externas | Spotify Web API, LRCLib (letras sincronizadas) |

---

## Requisitos

- Node.js 18+
- Java 17+
- Docker y Docker Compose
- Credenciales de la [Spotify Developer API](https://developer.spotify.com/dashboard)

---

## Configuración

Crea un archivo `.env` en la raíz del proyecto con las siguientes variables:

```env
# Spotify
SPOTIFY_CLIENT_ID=tu_client_id
SPOTIFY_CLIENT_SECRET=tu_client_secret

# JWT
JWT_SECRET=una_clave_secreta_larga
JWT_REFRESH_SECRET=otra_clave_secreta_larga
```

---

## Ejecución con Docker

```bash
docker-compose up --build
```

Servicios disponibles tras el arranque:

| Servicio | URL |
|----------|-----|
| Frontend | http://localhost:3005 |
| API Gateway | http://localhost:3000 |
| ms-canciones | http://localhost:3002 |
| ms-usuarios | http://localhost:3001 |
| Eureka Dashboard | http://localhost:8761 |

---

## Ejecución en desarrollo (sin Docker)

**ms-canciones**
```bash
cd ms-canciones && npm install && npm run dev
```

**api-gateway**
```bash
cd api-gateway && npm install && npm start
```

**Frontend**
```bash
cd frontend && npm install && npm run dev
# http://localhost:3005
```

---

## Endpoints del API Gateway

### Autenticación (`/auth`)
| Método | Ruta | Descripción |
|--------|------|-------------|
| POST | `/auth/register` | Registro de usuario |
| POST | `/auth/login` | Login, devuelve access + refresh token |
| POST | `/auth/refresh` | Renueva el access token |

### Canciones (`/songs`)
| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/songs/search?q=query` | Búsqueda en Spotify |
| GET | `/songs/:id` | Metadatos de una canción |
| GET | `/songs/:id/lyrics` | Letra (sincronizada si existe) |
| GET | `/songs/:id/lesson-type` | Tipo de lección según género |

---

## Tests

```bash
cd ms-canciones

# Todos los tests con cobertura
npm test

# Solo tests unitarios (patrones de diseño)
npm run test:unit

# Solo tests de integración (rutas HTTP)
npm run test:integration
```

---

## Estructura del repositorio

```
notara-project/
├── api-gateway/          # Gateway Express — enruta /auth, /users, /songs
├── frontend/             # App Next.js — interfaz de usuario
├── ms-canciones/         # Microservicio Node.js/Fastify — Spotify + letras
├── ms-usuarios/          # Microservicio Spring Boot — autenticación
├── ms-notas-metas/       # Microservicio Spring Boot — progreso del usuario
├── bff/                  # Backend for Frontend — integración con IA
├── eureka-server/        # Servidor de descubrimiento de servicios
└── docker-compose.yml    # Orquestación completa del sistema
```
