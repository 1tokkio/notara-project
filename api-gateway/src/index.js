require('dotenv').config();
const express = require('express');
const cors = require('cors');
const morgan = require('morgan');
const { createProxyMiddleware } = require('http-proxy-middleware');

const app = express();

// ─── URLs de los microservicios ───────────────────────────────────────────────
const MS_USUARIOS_URL = process.env.MS_USUARIOS_URL || 'http://localhost:3001';
const MS_CANCIONES_URL = process.env.MS_CANCIONES_URL || 'http://localhost:3002';
const PORT = process.env.API_GATEWAY_PORT || 3000;

// ─── Middlewares globales ─────────────────────────────────────────────────────
app.use(cors({ origin: '*', methods: ['GET', 'POST', 'PUT', 'DELETE', 'PATCH'] }));
app.use(morgan('[:date[clf]] :method :url :status :res[content-length] - :response-time ms'));

// ─── Ruta raíz ───────────────────────────────────────────────────────────────
app.get('/', (req, res) => {
  res.json({
    service: 'LinguaFlow API Gateway',
    version: '1.0.0',
    status: 'running',
    routes: {
      'POST /auth/register': 'Registro de usuario',
      'POST /auth/login':    'Login con JWT',
      'POST /auth/refresh':  'Renovar access token',
      'GET  /users/me':      'Perfil del usuario autenticado',
      'GET  /songs/search':  'Buscar canciones en Spotify (?q=query)',
      'GET  /songs/:id':     'Metadatos de una canción',
      'GET  /songs/:id/lyrics':      'Letra de la canción',
      'GET  /songs/:id/lesson-type': 'Tipo de lección (Factory Method)',
      'GET  /health':        'Estado del gateway',
    },
  });
});

// ─── Favicon (evita 404 en el browser) ───────────────────────────────────────
app.get('/favicon.ico', (req, res) => res.status(204).end());

// ─── Health check del Gateway ─────────────────────────────────────────────────
app.get('/health', (req, res) => {
  res.json({
    status: 'ok',
    service: 'api-gateway',
    uptime: process.uptime(),
    services: {
      'ms-usuarios': MS_USUARIOS_URL,
      'ms-canciones': MS_CANCIONES_URL,
    },
  });
});

// ─── Configuración de opciones de proxy ──────────────────────────────────────
const proxyOptions = (target, serviceName) => ({
  target,
  changeOrigin: true,
  on: {
    error: (err, req, res) => {
      console.error(`[Gateway] Error al conectar con ${serviceName}:`, err.message);
      res.status(503).json({
        error: `Servicio ${serviceName} no disponible`,
        message: 'Intenta nuevamente en unos segundos',
      });
    },
    proxyReq: (proxyReq, req) => {
      // Reenviar IP real del cliente
      proxyReq.setHeader('X-Forwarded-For', req.ip || req.connection.remoteAddress);
      proxyReq.setHeader('X-Gateway', 'linguaflow-gateway');
      console.log(`[Gateway] → ${serviceName}: ${req.method} ${req.originalUrl}`);
    },
    proxyRes: (proxyRes, req) => {
      console.log(`[Gateway] ← ${serviceName}: ${proxyRes.statusCode} ${req.originalUrl}`);
    },
  },
});

// ─── Rutas proxy ─────────────────────────────────────────────────────────────

/**
 * /auth/** → ms-usuarios (puerto 3001)
 * Maneja registro, login y refresh de tokens JWT
 */
app.use(
  '/auth',
  createProxyMiddleware(proxyOptions(MS_USUARIOS_URL, 'ms-usuarios'))
);

/**
 * /users/** → ms-usuarios (puerto 3001)
 * Maneja perfil y datos del usuario autenticado
 */
app.use(
  '/users',
  createProxyMiddleware(proxyOptions(MS_USUARIOS_URL, 'ms-usuarios'))
);

/**
 * /songs/** → ms-canciones (puerto 3002)
 * Maneja búsqueda, metadatos, letras y tipo de lección
 */
app.use(
  '/songs',
  createProxyMiddleware(proxyOptions(MS_CANCIONES_URL, 'ms-canciones'))
);

// ─── Ruta no encontrada ───────────────────────────────────────────────────────
app.use((req, res) => {
  res.status(404).json({
    error: 'Ruta no encontrada en el Gateway',
    path: req.originalUrl,
    availableRoutes: ['/auth', '/users', '/songs', '/health'],
  });
});

// ─── Arranque del servidor ────────────────────────────────────────────────────
app.listen(PORT, '0.0.0.0', () => {
  console.log(`\n🚀 API Gateway corriendo en http://localhost:${PORT}`);
  console.log(`   /auth, /users  → ms-usuarios  (${MS_USUARIOS_URL})`);
  console.log(`   /songs         → ms-canciones (${MS_CANCIONES_URL})`);
  console.log(`   /health        → estado del gateway\n`);
});
