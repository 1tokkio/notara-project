const config = {
  server: {
    port: parseInt(process.env.MS_CANCIONES_PORT || '3002', 10),
    host: '0.0.0.0',
  },
  mongodb: {
    uri: process.env.MONGODB_URI || 'mongodb://localhost:27017/linguaflow',
  },
  redis: {
    url: process.env.REDIS_URL || 'redis://localhost:6379',
  },
  spotify: {
    clientId: process.env.SPOTIFY_CLIENT_ID,
    clientSecret: process.env.SPOTIFY_CLIENT_SECRET,
  },
  circuitBreaker: {
    spotify: { failureThreshold: 3, resetTimeout: 30_000 },
    lyrics: { failureThreshold: 3, resetTimeout: 60_000 },
  },
  cache: {
    lyricsTtlSeconds: 60 * 60 * 24 * 7, // 7 días
  },
};

module.exports = config;
