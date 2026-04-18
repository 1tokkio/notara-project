const { createClient } = require('redis');

let redisClient = null;

const connectRedis = async () => {
  const url = process.env.REDIS_URL || 'redis://localhost:6379';
  redisClient = createClient({ url });

  redisClient.on('error', (err) => console.error('Redis error:', err));

  await redisClient.connect();
  console.log('Conectado a Redis');
};

const getRedis = () => {
  if (!redisClient) throw new Error('Redis no inicializado');
  return redisClient;
};

module.exports = { connectRedis, getRedis };
