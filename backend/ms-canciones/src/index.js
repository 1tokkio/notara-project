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

// Swagger / OpenAPI
app.register(require("@fastify/swagger"), {
  openapi: {
    info: {
      title: "ms-canciones API",
      description:
        "Microservicio de canciones — búsqueda Spotify, letras (lrclib) y caché Redis",
      version: "1.0.0",
    },
    tags: [
      { name: "canciones", description: "Endpoints de canciones" },
      { name: "health", description: "Estado del servicio" },
    ],
  },
});

app.register(require("@fastify/swagger-ui"), {
  routePrefix: "/docs",
  uiConfig: { docExpansion: "list" },
});

registerErrorHandler(app);
registerRequestLogger(app);

app.register(require("@fastify/cors"), {
  origin: "*",
  methods: ["GET", "POST", "PUT", "DELETE"],
});

app.register(songRoutes, { prefix: "" });

app.get(
  "/health",
  {
    schema: {
      tags: ["health"],
      summary: "Estado del servicio",
      response: {
        200: {
          type: "object",
          properties: {
            status: { type: "string" },
            service: { type: "string" },
            timestamp: { type: "string" },
          },
        },
      },
    },
  },
  async () => ({
    status: "ok",
    service: "ms-canciones",
    timestamp: new Date().toISOString(),
  })
);

const start = async () => {
  try {
    try {
      await connectMongo();
    } catch (err) {
      app.log.warn(
        "MongoDB no disponible, persistencia desactivada: " + err.message
      );
    }
    try {
      await connectRedis();
    } catch (err) {
      app.log.warn(
        "Redis no disponible, caché de letras desactivado: " + err.message
      );
    }
    await app.listen({ port: config.server.port, host: config.server.host });
    app.log.info(`ms-canciones corriendo en puerto ${config.server.port}`);
  } catch (err) {
    app.log.error(err);
    process.exit(1);
  }
};

start();
