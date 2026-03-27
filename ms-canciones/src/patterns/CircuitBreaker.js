/**
 * CircuitBreaker — Circuit Breaker Pattern
 *
 * Protege al sistema de fallos en cascada cuando un servicio externo (Spotify, LyricsAPI) falla.
 *
 * Estados:
 *  - CLOSED:    Funcionamiento normal, las llamadas pasan.
 *  - OPEN:      El servicio falló demasiado, bloqueamos llamadas por `resetTimeout` ms.
 *  - HALF_OPEN: Después del timeout, probamos con UNA sola llamada para ver si el servicio se recuperó.
 *
 * Si el servicio falla más de `failureThreshold` veces consecutivas → pasa a OPEN.
 * Si en HALF_OPEN la llamada de prueba falla → vuelve a OPEN.
 * Si en HALF_OPEN la llamada de prueba tiene éxito → vuelve a CLOSED.
 */

const STATE = {
  CLOSED: 'CLOSED',
  OPEN: 'OPEN',
  HALF_OPEN: 'HALF_OPEN',
};

class CircuitBreaker {
  /**
   * @param {string} name - Nombre del servicio protegido (para logs)
   * @param {object} options
   * @param {number} options.failureThreshold - Número de fallos consecutivos antes de abrir (default: 3)
   * @param {number} options.resetTimeout     - Tiempo en ms antes de intentar HALF_OPEN (default: 30000)
   */
  constructor(name, options = {}) {
    this.name = name;
    this.failureThreshold = options.failureThreshold ?? 3;
    this.resetTimeout = options.resetTimeout ?? 30_000; // 30 segundos

    this.state = STATE.CLOSED;
    this.failureCount = 0;
    this.lastFailureTime = null;
  }

  /**
   * Ejecuta la función protegida.
   * Si el circuito está OPEN y no ha pasado el resetTimeout, lanza error inmediatamente.
   * @param {Function} fn - Función async que realiza la llamada al servicio externo
   * @param {Function} fallback - Función que se ejecuta cuando el circuito está OPEN (opcional)
   * @returns {Promise<any>}
   */
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
