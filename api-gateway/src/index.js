require('dotenv').config();
const express = require('express');
const cors    = require('cors');
const morgan  = require('morgan');
const { createProxyMiddleware } = require('http-proxy-middleware');
const registerIaRoutes = require('./routes/ia');

const app = express();

const MS_USUARIOS_URL  = process.env.MS_USUARIOS_URL  || 'http://localhost:3001';
const MS_CANCIONES_URL = process.env.MS_CANCIONES_URL || 'http://localhost:3002';
const PORT             = process.env.API_GATEWAY_PORT  || 3000;

// ─── Credenciales Spotify ─────────────────────────────────────────────────────
const SPOTIFY_CLIENT_ID     = process.env.SPOTIFY_CLIENT_ID;
const SPOTIFY_CLIENT_SECRET = process.env.SPOTIFY_CLIENT_SECRET;
const SPOTIFY_REDIRECT_URI  = process.env.SPOTIFY_REDIRECT_URI || 'http://localhost:3000/auth/spotify/callback';
const FRONTEND_URL          = process.env.FRONTEND_URL || 'http://localhost:3001';

app.use(cors({ origin: '*', methods: ['GET', 'POST', 'PUT', 'DELETE', 'PATCH'] }));
app.use(morgan('[:date[clf]] :method :url :status :res[content-length] - :response-time ms'));
app.use(express.json());

app.get('/', (req, res) => {
  res.json({
    service: 'Notara API Gateway',
    version: '1.0.0',
    status: 'running',
    routes: {
      'POST /auth/register':         'Registro de usuario',
      'POST /auth/login':            'Login con JWT',
      'POST /auth/refresh':          'Renovar access token',
      'GET  /auth/spotify':          'Iniciar OAuth Spotify Premium (?songId=xxx)',
      'GET  /auth/spotify/callback': 'Callback OAuth Spotify',
      'GET  /users/me':              'Perfil del usuario autenticado',
      'GET  /songs/search':          'Buscar canciones en Spotify (?q=query)',
      'GET  /songs/:id':             'Metadatos de una canción',
      'GET  /songs/:id/lyrics':      'Letra de la canción',
      'GET  /songs/:id/lesson-type': 'Tipo de lección',
      'POST /ia/explain':            'Explicar frase seleccionada de la letra',
      'POST /ia/exercises':          'Generar ejercicios sobre una frase',
      'POST /ia/chat':               'Chat con tutor de IA',
      'GET  /health':                'Estado del gateway',
    },
  });
});

app.get('/favicon.ico', (req, res) => res.status(204).end());

app.get('/health', (req, res) => {
  res.json({
    status: 'ok',
    service: 'api-gateway',
    uptime: process.uptime(),
    services: {
      'ms-usuarios':  MS_USUARIOS_URL,
      'ms-canciones': MS_CANCIONES_URL,
    },
  });
});

// Rutas de IA (manejadas directamente, sin proxy)
registerIaRoutes(app, MS_CANCIONES_URL);

const proxyOptions = (target, serviceName) => ({
  target,
  changeOrigin: true,
  on: {
    error: (err, req, res) => {
      console.error(`[Gateway] Error al conectar con ${serviceName}:`, err.code || err.message || err);
      res.status(503).json({
        error: `Servicio ${serviceName} no disponible`,
        message: 'Intenta nuevamente en unos segundos',
      });
    },
    proxyReq: (proxyReq, req) => {
      proxyReq.setHeader('X-Forwarded-For', req.ip || req.connection.remoteAddress);
      proxyReq.setHeader('X-Gateway', 'notara-gateway');
      console.log(`[Gateway] -> ${serviceName}: ${req.method} ${req.originalUrl}`);
    },
    proxyRes: (proxyRes, req) => {
      console.log(`[Gateway] <- ${serviceName}: ${proxyRes.statusCode} ${req.originalUrl}`);
    },
  },
});

// ─── Spotify OAuth (ANTES del proxy /auth para que Express las intercepte) ───

/**
 * GET /auth/spotify?songId=xxx
 * Redirige a Spotify para autorizar. El songId viaja en `state` para
 * volver a la lección correcta tras el callback.
 */
app.get('/auth/spotify', (req, res) => {
  if (!SPOTIFY_CLIENT_ID) {
    return res.status(500).json({ error: 'SPOTIFY_CLIENT_ID no configurado' });
  }
  const { songId = '' } = req.query;
  const scope = 'streaming user-read-email user-read-private';
  const params = new URLSearchParams({
    client_id:     SPOTIFY_CLIENT_ID,
    response_type: 'code',
    redirect_uri:  SPOTIFY_REDIRECT_URI,
    scope,
    state: songId,
  });
  res.redirect(`https://accounts.spotify.com/authorize?${params}`);
});

/**
 * GET /auth/spotify/callback?code=xxx&state=songId
 * Intercambia el code por access_token + refresh_token y redirige al frontend.
 * El token va como query param para que el cliente lo guarde en localStorage.
 */
app.get('/auth/spotify/callback', async (req, res) => {
  const { code, state: songId, error } = req.query;

  if (error) {
    const dest = songId ? `${FRONTEND_URL}/lesson/${songId}` : FRONTEND_URL;
    return res.redirect(`${dest}?spotify_error=${encodeURIComponent(error)}`);
  }

  try {
    const credentials = Buffer.from(`${SPOTIFY_CLIENT_ID}:${SPOTIFY_CLIENT_SECRET}`).toString('base64');
    const body = new URLSearchParams({
      code,
      redirect_uri: SPOTIFY_REDIRECT_URI,
      grant_type:   'authorization_code',
    });

    const tokenRes = await fetch('https://accounts.spotify.com/api/token', {
      method:  'POST',
      headers: {
        Authorization:  `Basic ${credentials}`,
        'Content-Type': 'application/x-www-form-urlencoded',
      },
      body: body.toString(),
    });

    const data = await tokenRes.json();

    if (data.error) {
      const dest = songId ? `${FRONTEND_URL}/lesson/${songId}` : FRONTEND_URL;
      return res.redirect(`${dest}?spotify_error=${encodeURIComponent(data.error_description || data.error)}`);
    }

    const dest = songId ? `${FRONTEND_URL}/lesson/${songId}` : `${FRONTEND_URL}/search`;
    const params = new URLSearchParams({
      spotify_token:   data.access_token,
      spotify_refresh: data.refresh_token,
    });
    res.redirect(`${dest}?${params}`);
  } catch (err) {
    console.error('[Spotify OAuth] Error en callback:', err.message);
    res.status(500).json({ error: 'Error al intercambiar el código con Spotify' });
  }
});

// ─── Rutas proxy ──────────────────────────────────────────────────────────────
app.use('/auth',     createProxyMiddleware(proxyOptions(MS_USUARIOS_URL, 'ms-usuarios')));
app.use('/users',    createProxyMiddleware(proxyOptions(MS_USUARIOS_URL, 'ms-usuarios')));
app.use('/progress', createProxyMiddleware(proxyOptions(MS_USUARIOS_URL, 'ms-usuarios')));
app.use('/songs',    createProxyMiddleware(proxyOptions(MS_CANCIONES_URL, 'ms-canciones')));

app.use((req, res) => {
  res.status(404).json({
    error: 'Ruta no encontrada',
    path: req.originalUrl,
    availableRoutes: ['/auth', '/auth/spotify', '/users', '/songs', '/ia', '/health'],
  });
});

app.listen(PORT, '0.0.0.0', () => {
  console.log(`[Gateway] Servidor iniciado en http://localhost:${PORT}`);
  console.log(`[Gateway] ms-usuarios  -> ${MS_USUARIOS_URL}`);
  console.log(`[Gateway] ms-canciones -> ${MS_CANCIONES_URL}`);
  console.log(`[Gateway] IA           -> Claude Haiku (${process.env.ANTHROPIC_API_KEY ? 'configurada' : 'SIN API KEY'})`);
});
