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

| Patrón | Ubicación | Descripción |
|--------|-----------|-------------|
| **Factory Method** | `ms-canciones/src/patterns/LessonFactory.js` | Genera el tipo de lección según el género musical |
| **Circuit Breaker** | `ms-canciones/src/patterns/CircuitBreaker.js` | Protege las llamadas a Spotify y LyricsAPI |
| **Repository** | `ms-canciones/src/repositories/SongRepository.js` | Abstrae el acceso a MongoDB |
| **BFF** | `bff/` | Backend for Frontend para el panel de IA |
| **API Gateway** | `api-gateway/` | Punto de entrada único que enruta a los microservicios |

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
