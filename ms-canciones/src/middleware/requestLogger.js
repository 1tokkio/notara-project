function registerRequestLogger(fastify) {
  fastify.addHook('onRequest', async (request) => {
    request.log.info(
      { method: request.method, url: request.url, query: request.query },
      'incoming request'
    );
  });

  fastify.addHook('onResponse', async (request, reply) => {
    request.log.info(
      {
        method: request.method,
        url: request.url,
        statusCode: reply.statusCode,
        responseTimeMs: Math.round(reply.elapsedTime),
      },
      'request completed'
    );
  });
}

module.exports = registerRequestLogger;
