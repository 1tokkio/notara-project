/**
 * Tests adicionales para notas, metas, suscripciones y vocabulario de api.js
 */

global.fetch = jest.fn();

let store = {};
const localStorageMock = {
  getItem:    jest.fn((key) => store[key] ?? null),
  setItem:    jest.fn((key, value) => { store[key] = String(value); }),
  removeItem: jest.fn((key) => { delete store[key]; }),
  clear:      jest.fn(() => { store = {}; }),
};
Object.defineProperty(window, 'localStorage', { value: localStorageMock, writable: true });

delete window.location;
window.location = { href: '' };

import { notas, metas, suscripciones, vocabulario } from '../lib/api';

function mockOk(body) {
  global.fetch.mockResolvedValueOnce({
    status: 200,
    ok: true,
    json: () => Promise.resolve(body),
  });
}

beforeEach(() => {
  store = { access_token: 'test-token' };
  jest.clearAllMocks();
  localStorageMock.getItem.mockImplementation((key) => store[key] ?? null);
});

// ─── notas ────────────────────────────────────────────────────────────────────

describe('notas', () => {
  test('porUsuario llama GET /notas/usuario/:id', async () => {
    mockOk([{ id: 1, titulo: 'Nota 1' }]);
    const res = await notas.porUsuario(42);
    expect(global.fetch).toHaveBeenCalledWith(
      expect.stringContaining('/notas/usuario/42'),
      expect.any(Object)
    );
    expect(res).toEqual([{ id: 1, titulo: 'Nota 1' }]);
  });

  test('crear llama POST /notas', async () => {
    mockOk({ id: 2, titulo: 'Nueva nota' });
    const res = await notas.crear('Nueva nota', 'contenido', 1);
    const [, opts] = global.fetch.mock.calls[0];
    expect(opts.method).toBe('POST');
    expect(JSON.parse(opts.body)).toMatchObject({ titulo: 'Nueva nota', contenido: 'contenido', idUsuario: 1 });
  });

  test('actualizar llama PUT /notas/:id', async () => {
    mockOk({ id: 3, titulo: 'Actualizada' });
    await notas.actualizar(3, 'Actualizada', 'nuevo contenido', 1);
    const [url, opts] = global.fetch.mock.calls[0];
    expect(url).toContain('/notas/3');
    expect(opts.method).toBe('PUT');
  });

  test('eliminar llama DELETE /notas/:id', async () => {
    mockOk({});
    await notas.eliminar(5);
    const [url, opts] = global.fetch.mock.calls[0];
    expect(url).toContain('/notas/5');
    expect(opts.method).toBe('DELETE');
  });
});

// ─── metas ────────────────────────────────────────────────────────────────────

describe('metas', () => {
  test('porUsuario llama GET /metas/usuario/:id', async () => {
    mockOk([]);
    await metas.porUsuario(7);
    expect(global.fetch).toHaveBeenCalledWith(
      expect.stringContaining('/metas/usuario/7'),
      expect.any(Object)
    );
  });

  test('crear llama POST /metas', async () => {
    mockOk({ id: 10 });
    await metas.crear({ titulo: 'Mi meta', idUsuario: 1 });
    const [, opts] = global.fetch.mock.calls[0];
    expect(opts.method).toBe('POST');
  });

  test('actualizar llama PUT /metas/:id', async () => {
    mockOk({ id: 10, titulo: 'Meta actualizada' });
    await metas.actualizar(10, { titulo: 'Meta actualizada' });
    const [url, opts] = global.fetch.mock.calls[0];
    expect(url).toContain('/metas/10');
    expect(opts.method).toBe('PUT');
  });

  test('eliminar llama DELETE /metas/:id', async () => {
    mockOk({});
    await metas.eliminar(10);
    const [url, opts] = global.fetch.mock.calls[0];
    expect(url).toContain('/metas/10');
    expect(opts.method).toBe('DELETE');
  });
});

// ─── suscripciones ────────────────────────────────────────────────────────────

describe('suscripciones', () => {
  test('porUsuario llama GET /suscripciones/usuario/:id', async () => {
    mockOk([]);
    await suscripciones.porUsuario(3);
    expect(global.fetch).toHaveBeenCalledWith(
      expect.stringContaining('/suscripciones/usuario/3'),
      expect.any(Object)
    );
  });

  test('crear llama POST /suscripciones', async () => {
    mockOk({ id: 1 });
    await suscripciones.crear({ idUsuario: 1, plan: 'PREMIUM' });
    const [, opts] = global.fetch.mock.calls[0];
    expect(opts.method).toBe('POST');
    expect(JSON.parse(opts.body)).toMatchObject({ plan: 'PREMIUM' });
  });

  test('cancelar llama PUT /suscripciones/:id/cancelar', async () => {
    mockOk({ id: 1, estado: 'CANCELADA' });
    await suscripciones.cancelar(1);
    const [url, opts] = global.fetch.mock.calls[0];
    expect(url).toContain('/suscripciones/1/cancelar');
    expect(opts.method).toBe('PUT');
  });

  test('renovar llama PUT /suscripciones/:id/renovar con fechaFin', async () => {
    mockOk({ id: 1, estado: 'ACTIVA' });
    await suscripciones.renovar(1, '2026-12-31');
    const [url, opts] = global.fetch.mock.calls[0];
    expect(url).toContain('/suscripciones/1/renovar');
    expect(JSON.parse(opts.body)).toEqual({ fechaFin: '2026-12-31' });
  });
});

// ─── vocabulario ──────────────────────────────────────────────────────────────

describe('vocabulario', () => {
  test('categorias llama GET /vocabulario/palabras/categorias', async () => {
    mockOk([]);
    await vocabulario.categorias();
    expect(global.fetch).toHaveBeenCalledWith(
      expect.stringContaining('/vocabulario/palabras/categorias'),
      expect.any(Object)
    );
  });

  test('iniciarPartida llama POST /vocabulario/partidas', async () => {
    mockOk({ id: 'partida-1' });
    await vocabulario.iniciarPartida(1, 'Ana', 'MUSICA');
    const [, opts] = global.fetch.mock.calls[0];
    expect(opts.method).toBe('POST');
    expect(JSON.parse(opts.body)).toMatchObject({ idUsuario: 1, categoria: 'MUSICA' });
  });

  test('preguntaActual llama GET /vocabulario/partidas/:id/pregunta', async () => {
    mockOk({ pregunta: '¿Qué es un acorde?' });
    await vocabulario.preguntaActual('partida-1');
    expect(global.fetch).toHaveBeenCalledWith(
      expect.stringContaining('/vocabulario/partidas/partida-1/pregunta'),
      expect.any(Object)
    );
  });

  test('responder llama POST /vocabulario/partidas/:id/responder', async () => {
    mockOk({ correcto: true });
    await vocabulario.responder('partida-1', 'mi respuesta');
    const [url, opts] = global.fetch.mock.calls[0];
    expect(url).toContain('/partidas/partida-1/responder');
    expect(JSON.parse(opts.body)).toEqual({ respuesta: 'mi respuesta' });
  });

  test('abandonar llama PUT /vocabulario/partidas/:id/abandonar', async () => {
    mockOk({});
    await vocabulario.abandonar('partida-1');
    const [url, opts] = global.fetch.mock.calls[0];
    expect(url).toContain('/partidas/partida-1/abandonar');
    expect(opts.method).toBe('PUT');
  });

  test('rankingGlobal llama GET /vocabulario/ranking', async () => {
    mockOk([]);
    await vocabulario.rankingGlobal();
    expect(global.fetch).toHaveBeenCalledWith(
      expect.stringContaining('/vocabulario/ranking'),
      expect.any(Object)
    );
  });

  test('estadisticasUsuario llama GET /vocabulario/ranking/usuario/:id', async () => {
    mockOk({});
    await vocabulario.estadisticasUsuario(5);
    expect(global.fetch).toHaveBeenCalledWith(
      expect.stringContaining('/vocabulario/ranking/usuario/5'),
      expect.any(Object)
    );
  });
});
