const { createClient } = require('redis');
<<<<<<< HEAD
=======
const config = require('../config/config');
>>>>>>> origin/panxo

let redisClient = null;

const connectRedis = async () => {
<<<<<<< HEAD
  const url = process.env.REDIS_URL || 'redis://localhost:6379';
  redisClient = createClient({ url });
=======
  redisClient = createClient({ url: config.redis.url });
>>>>>>> origin/panxo

  redisClient.on('error', (err) => console.error('Redis error:', err));

  await redisClient.connect();
  console.log('Conectado a Redis');
};

const getRedis = () => {
  if (!redisClient) throw new Error('Redis no inicializado');
  return redisClient;
};

module.exports = { connectRedis, getRedis };
