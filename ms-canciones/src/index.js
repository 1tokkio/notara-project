require('dotenv').config();
const Fastify = require('fastify');
const { connectMongo } = require('./database/mongo');
const { connectRedis } = require('./database/redis');
const songRoutes = require('./routes/songs');

const app = Fastify({ logger: true });

app.register(require('@fastify/cors'), {
  origin: '*',
  methods: ['GET', 'POST', 'PUT', 'DELETE'],
});

app.register(songRoutes, { prefix: '/songs' });

app.get('/health', async () => ({ status: 'ok', service: 'ms-canciones' }));

const start = async () => {
  try {
    await connectMongo();
    await connectRedis();

    const port = process.env.MS_CANCIONES_PORT || 3002;
    await app.listen({ port, host: '0.0.0.0' });
    console.log(`ms-canciones corriendo en puerto ${port}`);
  } catch (err) {
    app.log.error(err);
    process.exit(1);
  }
};

start();
