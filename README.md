# Notara

Plataforma web para aprender inglés a través de canciones. El usuario busca una canción en Spotify, escucha la letra sincronizada, responde un quiz generado por IA y puede chatear libremente sobre el contenido de la canción.

---

## Arquitectura

```
┌──────────────────────────────────────────────────────┐
│                     Frontend                          │
│              Next.js 14  —  :3001                    │
└───────────────────────┬──────────────────────────────┘
                        │ HTTP
┌───────────────────────▼──────────────────────────────┐
│                   API Gateway                         │
│              Node.js / Express  —  :3000             │
└──┬──────────┬────────┬──────────┬──────────┬─────────┘
   │          │        │          │          │
┌──▼───┐  ┌──▼───┐  ┌─▼────┐  ┌─▼────┐  ┌──▼────┐
│ ms-  │  │ ms-  │  │ ms-  │  │ ms-  │  │  ms-  │
│usua- │  │can-  │  │notas-│  │vocab-│  │ pagos │
│rios  │  │ciones│  │metas │  │ulario│  │ subs  │
│:8081 │  │:3002 │  │:8083 │  │:8086 │  │ :8084 │
└──┬───┘  └──┬───┘  └──┬───┘  └──┬───┘  └───┬───┘
   │         │          │         │           │
┌──▼──┐   ┌──▼──┐   ┌──▼──┐   ┌──▼──┐       │    ┌─────────────┐
│ PG  │   │Mongo│   │ PG  │   │ PG  │       │    │ms-notifica- │
│5432 │   │27017│   │5433 │   │5435 │       │    │ciones :8085 │
└─────┘   └─────┘   └─────┘   └─────┘       │    └──────┬──────┘
                                             │           │
                                          ┌──▼───────────▼──┐
                                          │    RabbitMQ      │
                                          │     :5672        │
                                          └─────────────────┘

Redis :6379  (caché — ms-usuarios, ms-canciones)
Eureka Server :8761  (descubrimiento de servicios)
```

---

## Estructura del repositorio

```
notara-project/
├── frontend/                     # App Next.js — interfaz de usuario
│   ├── src/
│   │   ├── app/                  # Rutas Next.js App Router
│   │   ├── components/           # Componentes React
│   │   ├── context/              # Estado global (AuthContext)
│   │   ├── lib/                  # API client, utilidades
│   │   └── patterns/             # Patrones de diseño
│   └── docker-compose.yml
│
├── backend/                      # Todos los microservicios
│   ├── api-gateway/              # Express — enruta /auth, /songs, /ia, /progress
│   ├── eureka-server/            # Servidor de descubrimiento Netflix Eureka
│   ├── ms-usuarios/              # Spring Boot — autenticación, JWT, usuarios
│   ├── ms-canciones/             # Node.js/Fastify — Spotify, letras, lecciones
│   ├── ms-notas-metas/           # Spring Boot — notas y metas del usuario
│   ├── ms-vocabulario/           # Spring Boot — juego de vocabulario y ranking
│   ├── ms-pagos-subscripciones/  # Spring Boot — suscripciones + RabbitMQ publisher
│   ├── ms-notificaciones/        # Spring Boot — emails + RabbitMQ consumer
│   └── docker-compose.yml
│
├── data/                         # Configuración de bases de datos
│   ├── postgres/                 # Scripts de inicialización PostgreSQL
│   ├── mongodb/                  # Scripts de inicialización MongoDB
│   ├── redis/                    # Configuración Redis
│   ├── rabbitmq/                 # Configuración RabbitMQ
│   └── docker-compose.yml
│
├── docker-compose.yml            # Orquestación completa (alternativa)
└── .env                          # Variables de entorno (no subir al repo)
```

---

## Patrones de diseño implementados

### Backend — ms-canciones

| Patrón | Archivo | Descripción |
|--------|---------|-------------|
| **Factory Method** | `backend/ms-canciones/src/patterns/LessonFactory.js` | Crea el tipo de lección (`vocabulary`, `grammar`, `pronunciation`) según el género musical de la canción. |
| **Circuit Breaker** | `backend/ms-canciones/src/patterns/CircuitBreaker.js` | Protege las llamadas a Spotify y LRCLib: si acumulan fallos, el circuito se abre y devuelve error inmediato. |
| **Repository** | `backend/ms-canciones/src/repositories/SongRepository.js` | Abstrae el acceso a MongoDB con métodos de dominio (`findById`, `upsert`). |

### Frontend — Next.js

| Patrón | Archivo | Descripción |
|--------|---------|-------------|
| **Strategy** | `frontend/src/patterns/LyricsDisplayStrategy.js` | Cuatro modos de visualización de letra (solo EN, solo ES, bilingüe, sincronizada), cada uno como estrategia intercambiable. |
| **Observer / Context** | `frontend/src/context/AuthContext.js` | Estado de autenticación global. Componentes se suscriben con `useAuth()` sin prop drilling. |
| **Facade** | `frontend/src/lib/api.js` | Centraliza todas las llamadas HTTP: maneja JWT, refresh automático y redirección por sesión expirada. |

### Arquitectura

| Patrón | Ubicación | Descripción |
|--------|-----------|-------------|
| **API Gateway** | `backend/api-gateway/` | Punto de entrada único. Centraliza enrutamiento, autenticación y CORS. |
| **Microservicios** | `backend/ms-*/` | Cada dominio de negocio como servicio independiente con su propia base de datos. |
| **Publish/Subscribe** | RabbitMQ | `ms-pagos-subscripciones` publica eventos; `ms-notificaciones` los consume para enviar emails. |

---

## Stack tecnológico

| Capa | Tecnología |
|------|------------|
| Frontend | Next.js 14, React 18, Tailwind CSS 4 |
| API Gateway | Node.js, Express 4 |
| ms-canciones | Node.js, Fastify 4, Mongoose 8, Redis 4 |
| ms-usuarios | Java 17, Spring Boot 3.3, Spring Security, JJWT |
| ms-notas-metas | Java 17, Spring Boot 3.3, OpenFeign |
| ms-vocabulario | Java 17, Spring Boot 3.3, JPA |
| ms-pagos-subscripciones | Java 17, Spring Boot 3.3, AMQP |
| ms-notificaciones | Java 17, Spring Boot 3.3, Spring Mail, AMQP |
| Bases de datos | PostgreSQL 16 ×4, MongoDB 7, Redis 7 |
| Mensajería | RabbitMQ 3.13 |
| Descubrimiento | Netflix Eureka |
| IA | Anthropic Claude (Haiku) |
| Audio | Spotify Web API, Spotify Web Playback SDK |
| Letras | LRCLib (letras sincronizadas) |
| Cobertura | JaCoCo (Java, mínimo 85%), Jest (Node.js/React) |

---

## Requisitos previos

- [Docker](https://www.docker.com/get-started) y Docker Compose
- O bien: Node.js 20+ y Java 17+ para correr sin Docker
- Credenciales de [Spotify Developer](https://developer.spotify.com/dashboard)
- API Key de [Anthropic](https://console.anthropic.com)

---

## Configuración

Crear un archivo `.env` en la raíz del proyecto:

```env
SPOTIFY_CLIENT_ID=tu_spotify_client_id
SPOTIFY_CLIENT_SECRET=tu_spotify_client_secret
ANTHROPIC_API_KEY=tu_anthropic_api_key
MAIL_USERNAME=tu_email@gmail.com
MAIL_PASSWORD=tu_app_password
MAIL_FROM=noreply@notara.cl
```

> Los secrets de JWT están en los `docker-compose.yml` con valores por defecto para desarrollo. Cambiarlos en producción.

---

## Ejecución con Docker

El proyecto tiene tres `docker-compose.yml` independientes. **Deben levantarse en este orden:**

### 1. Bases de datos (crea la red compartida `notara-network`)

```bash
cd data
docker compose up -d --build
```

### 2. Backend (microservicios)

```bash
cd backend
docker compose up -d --build
```

### 3. Frontend

```bash
cd frontend
docker compose up -d --build
```

> El primer build tarda ~5-10 minutos (Spring Boot compila Java).

### Alternativa: levantar todo desde la raíz

```bash
docker compose up -d --build
```

### Detener servicios

```bash
# Desde cada carpeta:
docker compose down

# O desde la raíz:
docker compose down
```

### Ver logs de un servicio

```bash
docker compose logs -f api-gateway
docker compose logs -f ms-canciones
docker compose logs -f ms-usuarios
```

---

## URLs tras el arranque

| Servicio | URL |
|----------|-----|
| **Frontend** | http://localhost:3001 |
| **API Gateway** | http://localhost:3000 |
| ms-canciones | http://localhost:3002 |
| ms-usuarios | http://localhost:8081 |
| ms-notas-metas | http://localhost:8083 |
| ms-pagos-subscripciones | http://localhost:8084 |
| ms-notificaciones | http://localhost:8085 |
| ms-vocabulario | http://localhost:8086 |
| Eureka Dashboard | http://localhost:8761 |
| RabbitMQ Management | http://localhost:15672 (guest / guest) |

---

## Ejecución en desarrollo (sin Docker)

Requiere MongoDB, PostgreSQL ×4, Redis y RabbitMQ corriendo localmente.

**ms-canciones**
```bash
cd backend/ms-canciones
npm install
npm run dev
```

**api-gateway**
```bash
cd backend/api-gateway
npm install
npm run dev
```

**Frontend**
```bash
cd frontend
npm install
npm run dev
```

**Servicios Java (Spring Boot)**
```bash
cd backend/eureka-server
mvn spring-boot:run

cd backend/ms-usuarios
mvn spring-boot:run

cd backend/ms-notas-metas
mvn spring-boot:run

cd backend/ms-vocabulario
mvn spring-boot:run

cd backend/ms-pagos-subscripciones
mvn spring-boot:run

cd backend/ms-notificaciones
mvn spring-boot:run
```

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
| GET | `/songs/:id/lyrics` | Letra sincronizada (LRC) |
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

### Frontend (Jest + Testing Library)
```bash
cd frontend
npm test                  # todos los tests
npm run test:coverage     # con reporte de cobertura (mínimo 85%)
```

### ms-canciones (Jest)
```bash
cd backend/ms-canciones
npm test                  # todos los tests con cobertura
npm run test:unit         # solo unitarios (CircuitBreaker, LessonFactory)
npm run test:integration  # solo integración (rutas HTTP)
```

### Microservicios Java (JUnit 5 + Mockito — cobertura mínima 85%)
```bash
cd backend/ms-usuarios
mvn test

cd backend/ms-notas-metas
mvn test

cd backend/ms-vocabulario
mvn test

cd backend/ms-pagos-subscripciones
mvn test

cd backend/ms-notificaciones
mvn test
```
