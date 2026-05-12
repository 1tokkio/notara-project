const CircuitBreaker = require('../../patterns/CircuitBreaker');

describe('CircuitBreaker', () => {
  let cb;

  beforeEach(() => {
    cb = new CircuitBreaker('TestService', { failureThreshold: 3, resetTimeout: 5000 });
    jest.useFakeTimers();
  });

  afterEach(() => {
    jest.useRealTimers();
  });

  describe('estado inicial', () => {
    test('inicia en estado CLOSED', () => {
      expect(cb.getState().state).toBe('CLOSED');
    });

    test('inicia con cero fallos', () => {
      expect(cb.getState().failureCount).toBe(0);
    });
  });

  describe('flujo CLOSED → OPEN', () => {
    test('ejecuta la función cuando el circuito está CLOSED', async () => {
      const fn = jest.fn().mockResolvedValue('ok');
      const result = await cb.execute(fn);
      expect(result).toBe('ok');
      expect(fn).toHaveBeenCalledTimes(1);
    });

    test('permanece CLOSED tras un éxito', async () => {
      await cb.execute(jest.fn().mockResolvedValue('ok'));
      expect(cb.getState().state).toBe('CLOSED');
    });

    test('incrementa failureCount en cada fallo', async () => {
      try { await cb.execute(jest.fn().mockRejectedValue(new Error('fallo'))); } catch {}
      expect(cb.getState().failureCount).toBe(1);
    });

    test('abre el circuito al alcanzar failureThreshold', async () => {
      const fn = jest.fn().mockRejectedValue(new Error('fallo'));
      for (let i = 0; i < 3; i++) {
        try { await cb.execute(fn); } catch {}
      }
      expect(cb.getState().state).toBe('OPEN');
    });
  });

  describe('estado OPEN', () => {
    beforeEach(async () => {
      const fn = jest.fn().mockRejectedValue(new Error('fallo'));
      for (let i = 0; i < 3; i++) {
        try { await cb.execute(fn); } catch {}
      }
    });

    test('rechaza llamadas sin ejecutar la función', async () => {
      const fn = jest.fn().mockResolvedValue('ok');
      await expect(cb.execute(fn)).rejects.toThrow();
      expect(fn).not.toHaveBeenCalled();
    });

    test('ejecuta el fallback cuando el circuito está OPEN', async () => {
      const fallback = jest.fn().mockReturnValue('fallback');
      const result = await cb.execute(jest.fn(), fallback);
      expect(result).toBe('fallback');
    });
  });

  describe('transición OPEN → HALF_OPEN → CLOSED', () => {
    beforeEach(async () => {
      const fn = jest.fn().mockRejectedValue(new Error('fallo'));
      for (let i = 0; i < 3; i++) {
        try { await cb.execute(fn); } catch {}
      }
    });

    test('vuelve a CLOSED si la llamada de prueba tiene éxito', async () => {
      jest.advanceTimersByTime(6000);
      await cb.execute(jest.fn().mockResolvedValue('ok'));
      expect(cb.getState().state).toBe('CLOSED');
    });

    test('vuelve a OPEN si la llamada de prueba falla', async () => {
      jest.advanceTimersByTime(6000);
      try { await cb.execute(jest.fn().mockRejectedValue(new Error('sigue fallando'))); } catch {}
      expect(cb.getState().state).toBe('OPEN');
    });
  });

  describe('recuperación', () => {
    test('resetea failureCount tras un éxito', async () => {
      const fail = jest.fn().mockRejectedValue(new Error('fallo'));
      try { await cb.execute(fail); } catch {}
      try { await cb.execute(fail); } catch {}
      await cb.execute(jest.fn().mockResolvedValue('ok'));
      expect(cb.getState().failureCount).toBe(0);
    });
  });
});
