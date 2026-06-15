const { createClient } = require('redis');
const config = require('../config/config');

let redisClient = null;

const connectRedis = async () => {
  redisClient = createClient({
    url: config.redis.url,
    socket: { reconnectStrategy: false, connectTimeout: 3000 },
  });

  redisClient.on('error', () => {});

  await redisClient.connect();
  console.log('Conectado a Redis');
};

const getRedis = () => {
  if (!redisClient) throw new Error('Redis no inicializado');
  return redisClient;
};

module.exports = { connectRedis, getRedis };
