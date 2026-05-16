// Patrón Circuit Breaker — protege de fallos en cascada cuando Spotify o LyricsAPI no responden.

const STATE = {
  CLOSED: 'CLOSED',
  OPEN: 'OPEN',
  HALF_OPEN: 'HALF_OPEN',
};

class CircuitBreaker {
  constructor(name, options = {}) {
    this.name = name;
    this.failureThreshold = options.failureThreshold ?? 3;
    this.resetTimeout = options.resetTimeout ?? 30_000; // 30 segundos

    this.state = STATE.CLOSED;
    this.failureCount = 0;
    this.lastFailureTime = null;
  }

  async execute(fn, fallback = null) {
    if (this.state === STATE.OPEN) {
      const elapsed = Date.now() - this.lastFailureTime;

      if (elapsed < this.resetTimeout) {
        console.warn(`[CircuitBreaker:${this.name}] OPEN — rechazando llamada. Tiempo restante: ${Math.round((this.resetTimeout - elapsed) / 1000)}s`);

        if (fallback) return fallback();
        throw new Error(`Servicio ${this.name} no disponible temporalmente (Circuit Breaker OPEN)`);
      }

      // Ha pasado el timeout → intentar recuperación
      this.state = STATE.HALF_OPEN;
      console.info(`[CircuitBreaker:${this.name}] → HALF_OPEN. Probando recuperación...`);
    }

    try {
      const result = await fn();
      this._onSuccess();
      return result;
    } catch (err) {
      console.error(`[CircuitBreaker:${this.name}] Error:`, err.response?.data || err.message);
      this._onFailure();
      if (fallback) return fallback();
      throw err;
    }
  }

  _onSuccess() {
    if (this.state === STATE.HALF_OPEN) {
      console.info(`[CircuitBreaker:${this.name}] Recuperado → CLOSED`);
    }
    this.state = STATE.CLOSED;
    this.failureCount = 0;
    this.lastFailureTime = null;
  }

  _onFailure() {
    this.failureCount++;
    this.lastFailureTime = Date.now();

    if (this.state === STATE.HALF_OPEN || this.failureCount >= this.failureThreshold) {
      this.state = STATE.OPEN;
      console.error(`[CircuitBreaker:${this.name}] Demasiados fallos (${this.failureCount}) → OPEN`);
    } else {
      console.warn(`[CircuitBreaker:${this.name}] Fallo #${this.failureCount}/${this.failureThreshold}`);
    }
  }

  getState() {
    return {
      name: this.name,
      state: this.state,
      failureCount: this.failureCount,
      lastFailureTime: this.lastFailureTime,
    };
  }
}

module.exports = CircuitBreaker;
