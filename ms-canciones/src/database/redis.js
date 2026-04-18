const { createClient } = require('redis');
const config = require('../config/config');

let redisClient = null;

const connectRedis = async () => {
  redisClient = createClient({ url: config.redis.url });

  redisClient.on('error', (err) => console.error('Redis error:', err));

  await redisClient.connect();
  console.log('Conectado a Redis');
};

const getRedis = () => {
  if (!redisClient) throw new Error('Redis no inicializado');
  return redisClient;
};

module.exports = { connectRedis, getRedis };
