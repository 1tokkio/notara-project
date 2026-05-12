# Guía de Presentación — Notara

---

## CRITERIO 1 y 5 — Patrones de diseño (10% implementación + 20% presentación)

### Qué decir:

"Implementamos seis patrones de diseño distribuidos entre frontend y backend. Voy a explicar cada uno con el problema que resuelve."

---

### BACKEND — ms-canciones

**1. Factory Method** (`src/patterns/LessonFactory.js`)

- **Problema:** Cuando el usuario entra a una lección, el sistema necesita crear un objeto distinto según el género de la canción: vocabulario para pop, gramática para rock, pronunciación para hip-hop. Sin este patrón, la ruta HTTP tendría un bloque switch gigante mezclando lógica de negocio con lógica de creación de objetos.
- **Solución:** `LessonFactory.create(genre)` encapsula toda esa lógica. La ruta solo llama al factory y recibe el objeto correcto, sin saber cómo se construye.
- **Mantenibilidad:** Agregar un nuevo tipo de lección (ej: "conversación") implica solo añadir una nueva clase y un caso en el factory, sin tocar ninguna ruta.

**2. Circuit Breaker** (`src/patterns/CircuitBreaker.js`)

- **Problema:** Spotify y LRCLib son servicios externos que pueden caerse. Sin protección, un timeout de 30 segundos bloquearía cada petición del usuario durante ese tiempo, agotaría las conexiones y haría caer todo el microservicio.
- **Solución:** El Circuit Breaker tiene tres estados: CLOSED (normal), OPEN (bloqueado), HALF_OPEN (probando recuperación). Tras 3 fallos consecutivos, abre el circuito y devuelve error inmediato. Después de 30 segundos, prueba una llamada de recuperación.
- **Mantenibilidad:** Cualquier llamada a servicio externo se puede proteger con una línea: `await circuitBreaker.execute(fn, fallback)`.

**3. Repository** (`src/repositories/SongRepository.js`)

- **Problema:** Las rutas necesitan persistir y consultar canciones en MongoDB, pero si conocen el esquema Mongoose directamente, un cambio de base de datos requeriría modificar todas las rutas.
- **Solución:** El Repository expone métodos de dominio (`findBySpotifyId`, `upsert`, `updateLyrics`) y oculta completamente Mongoose. Las rutas no saben si la BD es MongoDB, PostgreSQL o un archivo.
- **Mantenibilidad:** Migrar de MongoDB a otra BD implica solo reescribir el Repository, sin tocar ninguna ruta.

---

### FRONTEND — Next.js

**4. Strategy** (`src/patterns/LyricsDisplayStrategy.js`)

- **Problema:** La letra puede mostrarse en cuatro modos: solo inglés, solo español, bilingüe, sincronizada. Sin el patrón, `LessonPage` tendría cuatro bloques if/else con lógica de renderizado mezclada con lógica de negocio.
- **Solución:** Cada modo es una estrategia con interfaz `{ id, label, render(props) }`. El componente llama `getLyricsStrategy(id).render(props)` sin conocer los detalles de ningún modo.
- **Mantenibilidad:** Agregar un nuevo modo de visualización es añadir un objeto al array, sin modificar el componente principal.

**5. Observer / Context** (`src/context/AuthContext.js`)

- **Problema:** El estado de autenticación (usuario, token) es necesario en componentes distantes: Navbar, páginas protegidas, llamadas a la API. Sin Context habría que pasar el usuario por props en cada nivel del árbol.
- **Solución:** `AuthContext` implementa el patrón Observer. Los componentes se suscriben con `useAuth()` y se re-renderizan automáticamente cuando el estado de autenticación cambia.
- **Mantenibilidad:** Cambiar la lógica de autenticación afecta un solo archivo, no docenas de componentes.

**6. Facade** (`src/lib/api.js`)

- **Problema:** El frontend necesita hacer llamadas HTTP a múltiples endpoints, gestionar headers de autorización, manejar refresh de tokens y parsear errores. Sin una fachada, cada componente replicaría esa lógica.
- **Solución:** `api.js` expone funciones de alto nivel (`searchSongs`, `getLyrics`, `ia.explain`) que ocultan completamente fetch, tokens JWT y manejo de errores 401.
- **Mantenibilidad:** Si el backend cambia su esquema de autenticación, solo se modifica la fachada.

---

## CRITERIO 2 y 6 — Arquetipos arquitectónicos (10% + 20%)

### Qué decir:

"La arquitectura de Notara combina tres patrones arquitectónicos que juntos garantizan escalabilidad, separación de responsabilidades y mantenibilidad."

---

**1. Microservicios**

- **Por qué:** Separar usuarios, canciones y progreso en servicios independientes permite que cada uno escale según su demanda. Si Spotify hace muchas peticiones, solo escala ms-canciones, no todo el sistema.
- **Implementación:** ms-usuarios (Spring Boot + JWT), ms-canciones (Fastify + Spotify API), ms-notas-metas (Spring Boot + progreso).
- **Coherencia:** Cada microservicio tiene su propia base de datos, su propio puerto y su propio ciclo de vida.

**2. API Gateway** (`api-gateway/`)

- **Por qué:** Sin un punto de entrada único, el frontend necesitaría conocer la URL de cada microservicio, gestionar CORS por separado y replicar la validación de tokens. El Gateway centraliza todo eso.
- **Implementación:** Express enruta `/auth` y `/users` a ms-usuarios, `/songs` a ms-canciones, `/ia` al módulo de IA. Un solo punto de CORS, un solo punto de log.
- **Escalabilidad:** Agregar un nuevo microservicio es añadir una línea de proxy en el gateway.

**3. BFF — Backend for Frontend** (`bff/`)

- **Por qué:** El panel de dashboard requiere combinar datos de ms-usuarios y ms-notas-metas en una sola respuesta. Si eso lo hace el frontend, tiene dos peticiones lentas y lógica de combinación en el cliente.
- **Implementación:** Spring Boot con Feign clients que llama a ambos microservicios y devuelve un `DashboardDTO` consolidado en una sola respuesta.
- **Escalabilidad:** Si se agrega la app móvil, el BFF puede adaptarse al contrato de datos del cliente móvil sin cambiar los microservicios.

---

## CRITERIO 3 y 7 — Estrategia de branching (5% + 15%)

### Qué decir:

"Utilizamos una estrategia de branching basada en ramas por desarrollador y por funcionalidad, con Pull Requests como mecanismo de integración."

---

**Estructura de ramas:**

- `main` — rama de producción estable, solo recibe merges aprobados
- `1tokkio` — rama de desarrollo principal del proyecto
- `eliecer1` — rama de trabajo de otro integrante del equipo
- `panxo` — rama de trabajo del tercer integrante
- `claude/fix-deprecation-warning-PNWYS` — rama de hotfix para advertencias de deprecación

**Evidencia concreta:**

- 33 commits totales en el historial
- 7 merges via Pull Request documentados en GitHub
- Resolución de conflictos en PR #4 y PR #5 (conflicto en rutas de importación del frontend entre ramas)
- Cada rama integra su trabajo vía PR, nunca directamente a main

**Ejemplo de conflicto resuelto:**

En el PR #5 hubo un conflicto en `frontend/src/` entre la rama `eliecer1` que usaba alias `@/` y la rama `claude/solucionando-rutas` que usaba rutas relativas. Se resolvió manteniendo las rutas relativas por compatibilidad con el build de Next.js.

---

## CRITERIO 4 y 8 — Buenas prácticas y pruebas unitarias (5% + 15%)

### Qué decir:

"Implementamos pruebas unitarias y de integración con Jest en ms-canciones, con una cobertura superior al 70% en todos los umbrales configurados."

---

**Tests implementados:**

| Archivo | Tipo | Qué prueba |
|---------|------|------------|
| `CircuitBreaker.test.js` | Unitario | Los 3 estados del patrón, transiciones, fallback |
| `LessonFactory.test.js` | Unitario | Creación por género, por tipo, estructura de objetos |
| `SongRepository.test.js` | Unitario | findBySpotifyId, upsert, updateLyrics con mocks |
| `SpotifyService.test.js` | Unitario | Búsqueda, getTrack, manejo de errores |
| `LyricsService.test.js` | Unitario | Obtención de letras, parsing LRC |
| `songs.routes.test.js` | Integración | Endpoints HTTP con Fastify inject |

**Umbrales de cobertura configurados en jest.config.js:**
- Statements: 70% mínimo
- Branches: 70% mínimo
- Functions: 70% mínimo
- Lines: 70% mínimo

**Comando para mostrar en vivo durante la presentación:**
```bash
cd ms-canciones && npm test -- --coverage
```

**Buenas prácticas aplicadas:**

1. **Código limpio:** classNames extraídos a objetos `styles` en todos los componentes React
2. **Separación de responsabilidades:** cada archivo tiene una sola razón para cambiar
3. **Sin efectos secundarios no controlados:** MongoDB y Redis fallan silenciosamente sin bloquear el servicio
4. **Configuración centralizada:** variables de entorno en `.env`, config en `src/config/config.js`
5. **Manejo de errores en capas:** excepciones tipadas (`SongNotFoundError`, `ServiceUnavailableError`, `ValidationError`) con códigos HTTP correctos

---

## RESUMEN RÁPIDO para recordar en la presentación

| Patrón | Dónde | Por qué |
|--------|-------|---------|
| Factory Method | ms-canciones/patterns/LessonFactory.js | Crear lecciones distintas según género |
| Circuit Breaker | ms-canciones/patterns/CircuitBreaker.js | Proteger llamadas a Spotify/LRCLib |
| Repository | ms-canciones/repositories/SongRepository.js | Aislar MongoDB del resto del código |
| Strategy | frontend/patterns/LyricsDisplayStrategy.js | Modos de visualización intercambiables |
| Observer/Context | frontend/context/AuthContext.js | Estado de auth global sin prop drilling |
| Facade | frontend/lib/api.js | Ocultar HTTP, tokens y errores |
| API Gateway | api-gateway/ | Punto de entrada único |
| BFF | bff/ | Dashboard consolidado para el frontend |
