const AppError = require('../exceptions/AppError');

function registerErrorHandler(fastify) {
  fastify.setErrorHandler((error, request, reply) => {
    if (error instanceof AppError) {
      return reply.status(error.statusCode).send({
        error: error.code,
        message: error.message,
        statusCode: error.statusCode,
      });
    }

    // Errores de validación del schema de Fastify
    if (error.validation) {
      return reply.status(400).send({
        error: 'VALIDATION_ERROR',
        message: 'Parámetros de solicitud inválidos',
        details: error.validation,
        statusCode: 400,
      });
    }

    request.log.error({ err: error }, 'Error no manejado');
    return reply.status(500).send({
      error: 'INTERNAL_ERROR',
      message: 'Error interno del servidor',
      statusCode: 500,
    });
  });
}

module.exports = registerErrorHandler;
