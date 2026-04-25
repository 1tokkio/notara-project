<<<<<<< HEAD
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
=======
require("dotenv").config({
  path: require("path").resolve(__dirname, "../.env"),
});
const Fastify = require("fastify");
const { connectMongo } = require("./database/mongo");
const { connectRedis } = require("./database/redis");
const songRoutes = require("./routes/songs");
const config = require("./config/config");
const registerErrorHandler = require("./middleware/errorHandler");
const registerRequestLogger = require("./middleware/requestLogger");

const app = Fastify({ logger: true });

registerErrorHandler(app);
registerRequestLogger(app);

app.register(require("@fastify/cors"), {
  origin: "*",
  methods: ["GET", "POST", "PUT", "DELETE"],
});

app.register(songRoutes, { prefix: "" });

app.get("/health", async () => ({
  status: "ok",
  service: "ms-canciones",
  timestamp: new Date().toISOString(),
}));
>>>>>>> origin/panxo

const start = async () => {
  try {
    await connectMongo();
    await connectRedis();
<<<<<<< HEAD

    const port = process.env.MS_CANCIONES_PORT || 3002;
    await app.listen({ port, host: '0.0.0.0' });
    console.log(`ms-canciones corriendo en puerto ${port}`);
=======
    await app.listen({ port: config.server.port, host: config.server.host });
    app.log.info(`ms-canciones corriendo en puerto ${config.server.port}`);
>>>>>>> origin/panxo
  } catch (err) {
    app.log.error(err);
    process.exit(1);
  }
};

start();
