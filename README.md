# Notara

Plataforma web para aprender inglés a través de canciones. El usuario busca una canción en Spotify, escucha la letra sincronizada, responde un quiz generado por IA y puede chatear libremente sobre el contenido de la canción.

---

## Arquitectura

```
┌─────────────────┐
│    Frontend      │  Next.js 14 — :3001
└────────┬────────┘
         │ HTTP
┌────────▼────────┐
│   API Gateway   │  Node.js / Express — :3000
└────────┬────────┘
         │ proxy
    ┌────┴──────────────────┐
    │                       │
┌───▼──────────┐   ┌───────▼──────────┐
│ ms-usuarios  │   │  ms-canciones    │
│ Spring Boot  │   │  Node.js/Fastify │
│ Auth + JWT   │   │  Spotify + LRC   │
│    :8081     │   │     :3002        │
└───┬──────────┘   └───────┬──────────┘
    │                      │
┌───▼──────┐   ┌───────────▼┐   ┌────────┐
│PostgreSQL│   │  MongoDB   │   │ Redis  │
│ :5432    │   │  :27017    │   │ :6379  │
└──────────┘   └────────────┘   └────────┘

Eureka Server (descubrimiento de servicios) — :8761
ms-notas-metas (Spring Boot) — :8083
```

---

## Patrones de diseño implementados

### Backend — ms-canciones

| Patrón | Archivo | Descripción |
|--------|---------|-------------|
| **Factory Method** | `ms-canciones/src/patterns/LessonFactory.js` | Crea el tipo de lección (`vocabulary`, `grammar`, `pronunciation`) según el género musical de la canción, sin que la ruta conozca los detalles de construcción. |
| **Circuit Breaker** | `ms-canciones/src/patterns/CircuitBreaker.js` | Protege las llamadas a Spotify y LRCLib: si acumulan fallos consecutivos, el circuito se abre y devuelve error inmediato en vez de esperar timeouts. |
| **Repository** | `ms-canciones/src/repositories/SongRepository.js` | Abstrae el acceso a MongoDB. Las rutas usan métodos de dominio (`findById`, `upsert`) sin conocer Mongoose ni las queries. |

### Frontend — Next.js

| Patrón | Archivo | Descripción |
|--------|---------|-------------|
| **Strategy** | `frontend/src/patterns/LyricsDisplayStrategy.js` | Cuatro modos de visualización de letra (solo EN, solo ES, bilingüe, sincronizada), cada uno encapsulado en una estrategia intercambiable con interfaz `{ id, label, render(props) }`. |
| **Observer / Context** | `frontend/src/context/AuthContext.js` | Estado de autenticación global. Los componentes se suscriben con `useAuth()` y se re-renderizan automáticamente ante cambios sin necesidad de prop drilling. |
| **Facade** | `frontend/src/lib/api.js` | Centraliza todas las llamadas HTTP al backend: maneja el token JWT, el refresh automático y la redirección en caso de sesión expirada. |

### Arquitectura

| Patrón | Ubicación | Descripción |
|--------|-----------|-------------|
| **API Gateway** | `api-gateway/` | Punto de entrada único. Centraliza enrutamiento, autenticación y CORS. El frontend no conoce las URLs internas de los microservicios. |
| **BFF (Backend for Frontend)** | `bff/` | Orquesta llamadas a múltiples servicios para las respuestas de IA, adaptando el formato al cliente. |

---

## Stack tecnológico

| Capa | Tecnología |
|------|------------|
| Frontend | Next.js 14, React, Tailwind CSS |
| API Gateway | Node.js, Express |
| ms-canciones | Node.js, Fastify, Mongoose, Redis |
| ms-usuarios | Java 17, Spring Boot 3, Spring Security, JJWT |
| ms-notas-metas | Java 17, Spring Boot 3 |
| Bases de datos | MongoDB (canciones), PostgreSQL x2 (usuarios, notas) |
| Caché | Redis 7 |
| Descubrimiento | Netflix Eureka |
| IA | Anthropic Claude (Haiku) |
| Audio | Spotify Web API, Spotify Web Playback SDK |
| Letras | LRCLib (letras sincronizadas) |

---

## Requisitos previos

- [Docker](https://www.docker.com/get-started) y Docker Compose (recomendado)
- O bien: Node.js 20+ y Java 17+ para correr sin Docker
- Credenciales de [Spotify Developer](https://developer.spotify.com/dashboard)
- API Key de [Anthropic](https://console.anthropic.com) (para las funciones de IA)

---

## Configuración

Crear un archivo `.env` en la raíz del proyecto (`notara-project/.env`):

```env
SPOTIFY_CLIENT_ID=tu_spotify_client_id
SPOTIFY_CLIENT_SECRET=tu_spotify_client_secret
ANTHROPIC_API_KEY=tu_anthropic_api_key
```

> Los secrets de JWT ya están configurados en el `docker-compose.yml` con valores por defecto para desarrollo. Cambiarlos en producción.

---

## Ejecución con Docker (recomendado)

### Primera vez

```bash
# Clonar el repositorio
git clone https://github.com/1tokkio/notara-project.git
cd notara-project

# Crear el archivo de variables de entorno
cp .env.example .env   # o crear .env manualmente con las variables de arriba

# Construir imágenes y levantar todos los servicios
docker-compose up --build
```

El primer build tarda ~5-10 minutos (Spring Boot compila Java).

### Ejecuciones siguientes

```bash
docker-compose up
```

### Detener los servicios

```bash
docker-compose down
```

### Ver logs de un servicio específico

```bash
docker-compose logs -f api-gateway
docker-compose logs -f ms-canciones
docker-compose logs -f ms-usuarios
```

---

## URLs tras el arranque

| Servicio | URL |
|---------|-----|
| **Frontend** | http://localhost:3001 |
| **API Gateway** | http://localhost:3000 |
| ms-canciones | http://localhost:3002 |
| ms-usuarios | http://localhost:8081 |
| ms-notas-metas | http://localhost:8083 |
| Eureka Dashboard | http://localhost:8761 |

---

## Ejecución en desarrollo (sin Docker)

Requiere tener MongoDB, PostgreSQL y Redis corriendo localmente o vía Docker.

**ms-canciones**
```bash
cd ms-canciones
npm install
npm run dev        # nodemon, recarga automática
```

**api-gateway**
```bash
cd api-gateway
npm install
npm run dev
```

**Frontend**
```bash
cd frontend
npm install
npm run dev        # http://localhost:3000 (dev) o 3001 (build)
```

**ms-usuarios / ms-notas-metas / eureka-server**
```bash
cd ms-usuarios     # o ms-notas-metas, eureka-server
mvn spring-boot:run
```

---

## Demo presencial con ngrok

Para exponer el backend local y acceder desde otros dispositivos:

```bash
# 1. Levantar todos los servicios
docker-compose up -d

# 2. Abrir túnel al API Gateway (en otra terminal)
ngrok http 3000
```

Copiar la URL que muestra ngrok (ej. `https://abc123.ngrok-free.app`) y usarla como valor de `NEXT_PUBLIC_API_URL` en el frontend o en Vercel.

> ngrok requiere una cuenta gratuita en ngrok.com para obtener el authtoken: `ngrok config add-authtoken <token>`

---

## Endpoints del API Gateway

### Autenticación
| Método | Ruta | Descripción |
|--------|------|-------------|
| POST | `/auth/register` | Registro de nuevo usuario |
| POST | `/auth/login` | Login — devuelve `accessToken` + `refreshToken` |
| POST | `/auth/refresh` | Renueva el access token |
| GET | `/auth/spotify` | Inicia OAuth con Spotify Premium |
| GET | `/auth/spotify/callback` | Callback de Spotify OAuth |

### Canciones
| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/songs/search?q=query&limit=10` | Búsqueda en Spotify |
| GET | `/songs/:id` | Metadatos de una canción |
| GET | `/songs/:id/lyrics` | Letra (sincronizada LRC si existe) |
| GET | `/songs/:id/lesson-type` | Tipo de lección según género musical |

### IA (requiere autenticación)
| Método | Ruta | Descripción |
|--------|------|-------------|
| POST | `/ia/explain` | Explica una frase de la letra |
| POST | `/ia/exercises` | Genera quiz sobre la canción |
| POST | `/ia/chat` | Chat libre sobre la canción |

### Progreso (requiere autenticación)
| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/progress/stats` | Estadísticas del usuario (XP, racha, palabras) |
| POST | `/progress/word` | Guarda una palabra aprendida |
| POST | `/progress/lesson-complete` | Registra lección completada (+50 XP) |

---

## Tests

### ms-canciones (Jest)
```bash
cd ms-canciones

npm test                  # todos los tests con cobertura
npm run test:unit         # solo unitarios (CircuitBreaker, LessonFactory)
npm run test:integration  # solo integración (rutas HTTP)
```

### ms-usuarios (JUnit 5 + Mockito)
```bash
cd ms-usuarios
mvn test
```

---

## Estructura del repositorio

```
notara-project/
├── api-gateway/          # Gateway Express — enruta /auth, /songs, /ia, /progress
├── frontend/             # App Next.js — interfaz de usuario
├── ms-canciones/         # Node.js/Fastify — Spotify, letras, tipo de lección
├── ms-usuarios/          # Spring Boot — autenticación, JWT, usuarios
├── ms-notas-metas/       # Spring Boot — notas y metas del usuario
├── bff/                  # Backend for Frontend — orquestación de IA
├── eureka-server/        # Servidor de descubrimiento de servicios
├── docker-compose.yml    # Orquestación completa del sistema
└── .env                  # Variables de entorno (no subir al repo)
```
