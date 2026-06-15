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

const start = async () => {
  try {
    try {
      await connectMongo();
    } catch (err) {
      app.log.warn('MongoDB no disponible, persistencia desactivada: ' + err.message);
    }
    try {
      await connectRedis();
    } catch (err) {
      app.log.warn('Redis no disponible, caché de letras desactivado: ' + err.message);
    }
    await app.listen({ port: config.server.port, host: config.server.host });
    app.log.info(`ms-canciones corriendo en puerto ${config.server.port}`);
  } catch (err) {
    app.log.error(err);
    process.exit(1);
  }
};

start();
