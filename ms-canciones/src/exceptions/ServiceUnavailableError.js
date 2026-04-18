const AppError = require('./AppError');

class ServiceUnavailableError extends AppError {
  constructor(serviceName) {
    super(`Servicio ${serviceName} no disponible temporalmente`, 503, 'SERVICE_UNAVAILABLE');
  }
}

module.exports = ServiceUnavailableError;
