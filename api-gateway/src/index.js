require('dotenv').config();
const express = require('express');
const cors    = require('cors');
const morgan  = require('morgan');
const { createProxyMiddleware } = require('http-proxy-middleware');
const registerIaRoutes = require('./routes/ia');

const app = express();

const MS_USUARIOS_URL  = process.env.MS_USUARIOS_URL  || 'http://localhost:8081';
const MS_CANCIONES_URL = process.env.MS_CANCIONES_URL || 'http://localhost:3002';
const PORT             = process.env.API_GATEWAY_PORT  || 3000;

const SPOTIFY_CLIENT_ID     = process.env.SPOTIFY_CLIENT_ID;
const SPOTIFY_CLIENT_SECRET = process.env.SPOTIFY_CLIENT_SECRET;
const SPOTIFY_REDIRECT_URI  = process.env.SPOTIFY_REDIRECT_URI || 'http://localhost:3000/auth/spotify/callback';
const FRONTEND_URL          = process.env.FRONTEND_URL || 'http://localhost:3001';

app.use(cors({ origin: '*', methods: ['GET', 'POST', 'PUT', 'DELETE', 'PATCH'] }));
app.use(morgan('[:date[clf]] :method :url :status :res[content-length] - :response-time ms'));
app.use(express.json());

// ─── Rutas informativas ───────────────────────────────────────────────────────

app.get('/', (req, res) => {
  res.json({ service: 'Notara API Gateway', version: '1.0.0', status: 'running' });
});

app.get('/favicon.ico', (req, res) => res.status(204).end());

app.get('/health', (req, res) => {
  res.json({
    status: 'ok',
    service: 'api-gateway',
    uptime: process.uptime(),
    services: { 'ms-usuarios': MS_USUARIOS_URL, 'ms-canciones': MS_CANCIONES_URL },
  });
});

// ─── IA ───────────────────────────────────────────────────────────────────────
registerIaRoutes(app, MS_CANCIONES_URL);

// ─── Helper: forward manual con fetch ────────────────────────────────────────
// Usamos fetch en lugar de proxy para las rutas que tienen body JSON,
// porque http-proxy-middleware tiene conflicto con express.json().
async function forwardToUsuarios(req, res) {
  const url = `${MS_USUARIOS_URL}${req.originalUrl}`;
  console.log(`[Gateway] -> ms-usuarios: ${req.method} ${req.originalUrl}`);

  try {
    const headers = { 'Content-Type': 'application/json' };
    if (req.headers.authorization) {
      headers['Authorization'] = req.headers.authorization;
    }

    const options = {
      method: req.method,
      headers,
    };

    if (['POST', 'PUT', 'PATCH'].includes(req.method) && req.body) {
      options.body = JSON.stringify(req.body);
    }

    const upstream = await fetch(url, options);
    const text = await upstream.text();
    console.log(`[Gateway] <- ms-usuarios: ${upstream.status} ${req.originalUrl}`);

    res.status(upstream.status);
    upstream.headers.forEach((value, key) => {
      if (!['transfer-encoding', 'connection'].includes(key.toLowerCase())) {
        res.setHeader(key, value);
      }
    });

    res.send(text);
  } catch (err) {
    console.error(`[Gateway] Error al conectar con ms-usuarios:`, err.message);
    res.status(503).json({ error: 'Servicio ms-usuarios no disponible' });
  }
}

// ─── Spotify OAuth ────────────────────────────────────────────────────────────

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

// ─── Auth y Users: forward manual ─────────────────────────────────────────────
app.all('/auth/*', forwardToUsuarios);
app.all('/users/*', async (req, res) => {
  // Reescribir /users → /usuarios
  req.originalUrl = req.originalUrl.replace('/users', '/usuarios');
  await forwardToUsuarios(req, res);
});
app.all('/progress/*', forwardToUsuarios);

// ─── Songs: proxy normal (sin body JSON problemático en GET) ──────────────────
app.use('/songs', createProxyMiddleware({
  target: MS_CANCIONES_URL,
  changeOrigin: true,
  on: {
    error: (err, req, res) => {
      console.error(`[Gateway] Error ms-canciones:`, err.message);
      if (!res.headersSent) res.status(503).json({ error: 'ms-canciones no disponible' });
    },
    proxyReq: (proxyReq, req) => {
      console.log(`[Gateway] -> ms-canciones: ${req.method} ${req.originalUrl}`);
    },
    proxyRes: (proxyRes, req) => {
      console.log(`[Gateway] <- ms-canciones: ${proxyRes.statusCode} ${req.originalUrl}`);
    },
  },
}));

// ─── 404 ─────────────────────────────────────────────────────────────────────
app.use((req, res) => {
  res.status(404).json({
    error: 'Ruta no encontrada',
    path: req.originalUrl,
    availableRoutes: ['/auth', '/auth/spotify', '/users', '/songs', '/progress', '/ia', '/health'],
  });
});

// ─── Iniciar ──────────────────────────────────────────────────────────────────
app.listen(PORT, '0.0.0.0', () => {
  console.log(`[Gateway] Servidor iniciado en http://localhost:${PORT}`);
  console.log(`[Gateway] ms-usuarios  -> ${MS_USUARIOS_URL}`);
  console.log(`[Gateway] ms-canciones -> ${MS_CANCIONES_URL}`);
  console.log(`[Gateway] IA           -> Claude Haiku (${process.env.ANTHROPIC_API_KEY ? 'configurada' : 'SIN API KEY'})`);
});
