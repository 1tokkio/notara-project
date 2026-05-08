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

app.use(cors({ origin: '*', methods: ['GET', 'POST', 'PUT', 'DELETE', 'PATCH'] }));
app.use(morgan('[:date[clf]] :method :url :status :res[content-length] - :response-time ms'));
app.use(express.json());

app.get('/', (req, res) => {
  res.json({
    service: 'Notara API Gateway',
    version: '1.0.0',
    status: 'running',
    routes: {
      'POST /auth/register':        'Registro de usuario',
      'POST /auth/login':           'Login con JWT',
      'POST /auth/refresh':         'Renovar access token',
      'GET  /users/me':             'Perfil del usuario autenticado',
      'GET  /songs/search':         'Buscar canciones en Spotify (?q=query)',
      'GET  /songs/:id':            'Metadatos de una canción',
      'GET  /songs/:id/lyrics':     'Letra de la canción',
      'GET  /songs/:id/lesson-type':'Tipo de lección',
      'POST /ia/explain':           'Explicar frase seleccionada de la letra',
      'POST /ia/exercises':         'Generar ejercicios sobre una frase',
      'POST /ia/chat':              'Chat con tutor de IA',
      'GET  /health':               'Estado del gateway',
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

app.use('/auth',  createProxyMiddleware(proxyOptions(MS_USUARIOS_URL, 'ms-usuarios')));
app.use('/users', createProxyMiddleware(proxyOptions(MS_USUARIOS_URL, 'ms-usuarios')));
app.use('/songs', createProxyMiddleware(proxyOptions(MS_CANCIONES_URL, 'ms-canciones')));

app.use((req, res) => {
  res.status(404).json({
    error: 'Ruta no encontrada',
    path: req.originalUrl,
    availableRoutes: ['/auth', '/users', '/songs', '/ia', '/health'],
  });
});

app.listen(PORT, '0.0.0.0', () => {
  console.log(`[Gateway] Servidor iniciado en http://localhost:${PORT}`);
  console.log(`[Gateway] ms-usuarios  -> ${MS_USUARIOS_URL}`);
  console.log(`[Gateway] ms-canciones -> ${MS_CANCIONES_URL}`);
  console.log(`[Gateway] IA           -> Claude Haiku (${process.env.ANTHROPIC_API_KEY ? 'configurada' : 'SIN API KEY'})`);
});
