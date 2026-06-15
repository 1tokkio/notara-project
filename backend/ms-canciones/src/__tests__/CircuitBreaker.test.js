/**
 * Tests para CircuitBreaker
 * Verifica los estados CLOSED → OPEN → HALF_OPEN → CLOSED
 */
const CircuitBreaker = require('../patterns/CircuitBreaker');

describe('CircuitBreaker', () => {
  let cb;

  beforeEach(() => {
    cb = new CircuitBreaker('TestService', { failureThreshold: 3, resetTimeout: 1000 });
  });

  test('Estado inicial es CLOSED', () => {
    expect(cb.getState().state).toBe('CLOSED');
  });

  test('Ejecuta fn correctamente cuando está CLOSED', async () => {
    const result = await cb.execute(async () => 'ok');
    expect(result).toBe('ok');
    expect(cb.getState().state).toBe('CLOSED');
  });

  test('Cuenta fallos consecutivos', async () => {
    const failFn = async () => { throw new Error('fallo'); };
    const fallback = () => null;

    await cb.execute(failFn, fallback);
    expect(cb.getState().failureCount).toBe(1);

    await cb.execute(failFn, fallback);
    expect(cb.getState().failureCount).toBe(2);
  });

  test('Pasa a OPEN después de alcanzar failureThreshold', async () => {
    const failFn = async () => { throw new Error('fallo'); };
    const fallback = () => null;

    await cb.execute(failFn, fallback);
    await cb.execute(failFn, fallback);
    await cb.execute(failFn, fallback);

    expect(cb.getState().state).toBe('OPEN');
  });

  test('Llama al fallback cuando está OPEN', async () => {
    const failFn = async () => { throw new Error('fallo'); };
    const fallback = () => 'fallback_value';

    // Forzar estado OPEN
    await cb.execute(failFn, fallback);
    await cb.execute(failFn, fallback);
    await cb.execute(failFn, fallback);

    // Ahora en OPEN, debería devolver fallback
    const result = await cb.execute(async () => 'no_deberia_llegar', fallback);
    expect(result).toBe('fallback_value');
  });

  test('Lanza error cuando está OPEN y no hay fallback', async () => {
    const failFn = async () => { throw new Error('fallo'); };
    const fallback = () => null;

    await cb.execute(failFn, fallback);
    await cb.execute(failFn, fallback);
    await cb.execute(failFn, fallback);

    await expect(cb.execute(async () => 'x')).rejects.toThrow('no disponible temporalmente');
  });

  test('Pasa a HALF_OPEN después del resetTimeout', async () => {
    const failFn = async () => { throw new Error('fallo'); };
    const fallback = () => null;

    const fastCb = new CircuitBreaker('FastCB', { failureThreshold: 2, resetTimeout: 100 });

    await fastCb.execute(failFn, fallback);
    await fastCb.execute(failFn, fallback);
    expect(fastCb.getState().state).toBe('OPEN');

    await new Promise((r) => setTimeout(r, 150));

    // La próxima llamada debe intentar HALF_OPEN
    await fastCb.execute(async () => 'ok', fallback);
    expect(fastCb.getState().state).toBe('CLOSED');
  });

  test('Vuelve a CLOSED si HALF_OPEN tiene éxito', async () => {
    const failFn = async () => { throw new Error('fallo'); };
    const successFn = async () => 'recuperado';
    const fallback = () => null;

    const fastCb = new CircuitBreaker('FastCB2', { failureThreshold: 2, resetTimeout: 100 });

    await fastCb.execute(failFn, fallback);
    await fastCb.execute(failFn, fallback);
    await new Promise((r) => setTimeout(r, 150));

    const result = await fastCb.execute(successFn, fallback);
    expect(result).toBe('recuperado');
    expect(fastCb.getState().state).toBe('CLOSED');
    expect(fastCb.getState().failureCount).toBe(0);
  });

  test('Resetea contador en éxito', async () => {
    const failFn = async () => { throw new Error('fallo'); };
    const fallback = () => null;

    await cb.execute(failFn, fallback);
    await cb.execute(failFn, fallback);
    expect(cb.getState().failureCount).toBe(2);

    await cb.execute(async () => 'ok');
    expect(cb.getState().failureCount).toBe(0);
    expect(cb.getState().state).toBe('CLOSED');
  });

  test('Devuelve resultado del fallback cuando la fn falla en estado CLOSED', async () => {
    const failFn = async () => { throw new Error('fallo'); };
    const fallback = () => 'valor_fallback';

    const result = await cb.execute(failFn, fallback);
    expect(result).toBe('valor_fallback');
    expect(cb.getState().failureCount).toBe(1);
  });
});
