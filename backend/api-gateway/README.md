# API Gateway — Notara

BFF (Backend For Frontend) que centraliza todas las peticiones del frontend y las enruta a los microservicios correspondientes. También gestiona la autenticación OAuth con Spotify y el chat con IA.

## Stack

- Node.js + Express
- http-proxy-middleware
- dotenv, cors, morgan

## Variables de entorno

Crea un archivo `.env` basado en `.env.example`:

```env
API_GATEWAY_PORT=3000
FRONTEND_URL=http://localhost:3001

MS_USUARIOS_URL=http://localhost:8081
MS_CANCIONES_URL=http://localhost:3002
MS_NOTAS_METAS_URL=http://localhost:8083
MS_PAGOS_URL=http://localhost:8084
MS_VOCABULARIO_URL=http://localhost:8086

SPOTIFY_CLIENT_ID=tu_client_id
SPOTIFY_CLIENT_SECRET=tu_client_secret
SPOTIFY_REDIRECT_URI=http://127.0.0.1:3000/auth/spotify/callback

ANTHROPIC_API_KEY=tu_api_key
```

## Instalación y ejecución

```bash
npm install
npm run dev      # desarrollo con nodemon
npm start        # producción
```

## Endpoints expuestos

| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/health` | Estado del gateway |
| POST | `/auth/register` | Registro de usuario |
| POST | `/auth/login` | Login con JWT |
| POST | `/auth/refresh` | Renovar access token |
| GET | `/auth/spotify` | Iniciar OAuth Spotify |
| GET | `/auth/spotify/callback` | Callback OAuth Spotify |
| GET | `/users/me` | Perfil del usuario autenticado |
| GET | `/songs/search` | Buscar canciones en Spotify |
| GET | `/songs/:id` | Metadatos de una canción |
| GET | `/songs/:id/lyrics` | Letra de la canción |
| GET | `/notas` | Listar notas del usuario |
| POST | `/notas` | Crear nota |
| PUT | `/notas/:id` | Actualizar nota |
| DELETE | `/notas/:id` | Eliminar nota |
| GET | `/metas` | Listar metas |
| POST | `/metas` | Crear meta |
| GET | `/suscripciones` | Listar suscripciones |
| POST | `/suscripciones` | Crear suscripción |
| POST | `/ia/chat` | Chat con tutor de IA (Claude) |

## Microservicios enrutados

| Prefijo | Microservicio | Puerto |
|---------|--------------|--------|
| `/auth`, `/users`, `/progress` | ms-usuarios | 8081 |
| `/songs` | ms-canciones | 3002 |
| `/notas`, `/metas` | ms-notas-metas | 8083 |
| `/suscripciones` | ms-pagos-subscripciones | 8084 |
| `/vocabulario` | ms-vocabulario | 8086 |

## Docker

```bash
docker build -t notara-api-gateway .
docker run -p 3000:3000 --env-file .env notara-api-gateway
```
