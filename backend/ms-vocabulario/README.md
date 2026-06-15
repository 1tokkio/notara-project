# ms-vocabulario

Microservicio Spring Boot para el módulo de aprendizaje de vocabulario. Gestiona palabras, partidas de juego, preguntas y rankings. Integra **Redis** para cachear los rankings más consultados.

## Stack

- **Spring Boot 3.3** — framework principal
- **PostgreSQL** — persistencia de palabras, partidas y rankings
- **Redis** — caché de rankings (TTL configurable)
- **Eureka Client** — descubrimiento de servicios
- **Swagger / OpenAPI 3** — documentación de endpoints

## Puerto

| Servicio         | Puerto |
|------------------|--------|
| ms-vocabulario   | 8086   |
| PostgreSQL       | 5435   |
| Redis            | 6379   |

## Endpoints

```
# Palabras
GET    /vocabulario/palabras                        Listar todas las palabras
GET    /vocabulario/palabras/categorias             Resumen de palabras activas por categoría
GET    /vocabulario/palabras/categoria/{categoria}  Palabras activas de una categoría
GET    /vocabulario/palabras/{id}                   Obtener palabra por ID
POST   /vocabulario/palabras                        Crear nueva palabra
PUT    /vocabulario/palabras/{id}                   Actualizar palabra
DELETE /vocabulario/palabras/{id}                   Eliminar palabra

# Partidas
POST   /vocabulario/partidas                        Iniciar nueva partida
GET    /vocabulario/partidas/{id}/pregunta          Obtener pregunta actual
POST   /vocabulario/partidas/{id}/responder         Responder pregunta
PUT    /vocabulario/partidas/{id}/abandonar         Abandonar partida
GET    /vocabulario/partidas/{id}                   Obtener partida por ID
GET    /vocabulario/partidas/usuario/{idUsuario}    Historial de partidas de un usuario

# Ranking
GET    /vocabulario/ranking                         Ranking global (top 10)
GET    /vocabulario/ranking/categoria/{categoria}   Ranking por categoría (top 10)
GET    /vocabulario/ranking/usuario/{idUsuario}     Estadísticas de un usuario
```

## Swagger UI

```
http://localhost:8086/swagger-ui.html
```

## Variables de entorno

```env
SERVER_PORT=8086
EUREKA_URL=http://eureka-server:8761/eureka
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres-vocabulario:5432/notara-vocabulario-db
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=password
REDIS_HOST=redis
REDIS_PORT=6379
```

## Caché Redis

| Cache                  | Método                          | TTL       | Descripción                        |
|------------------------|---------------------------------|-----------|------------------------------------|
| `ranking-global`       | `rankingGlobal()`               | 5 minutos | Top 10 usuarios por puntuación global |
| `ranking-categoria`    | `rankingPorCategoria(categoria)`| 5 minutos | Top 10 por categoría               |
| `estadisticas-usuario` | `estadisticasUsuario(idUsuario)`| 10 minutos| Rankings individuales del usuario  |

El caché se invalida automáticamente con `@CacheEvict` cuando un usuario finaliza una partida y se actualizan los rankings.

## Categorías de vocabulario

| Categoría   | Descripción                        |
|-------------|------------------------------------|
| `MUSICA`    | Términos musicales (notas, géneros)|
| `ARTE`      | Conceptos artísticos               |
| `CIENCIA`   | Vocabulario científico             |
| `HISTORIA`  | Términos históricos                |
| `IDIOMAS`   | Vocabulario en idiomas             |

## Modelos principales

| Entidad          | Descripción |
|------------------|-------------|
| `Palabra`        | Palabra del vocabulario con definición, pista, categoría y dificultad |
| `Partida`        | Sesión de juego de un usuario con puntuación y racha |
| `PreguntaPartida`| Pregunta individual dentro de una partida |
| `Ranking`        | Estadísticas acumuladas de un usuario por categoría |

## Manejo de errores

- `GlobalExceptionHandler` — handler global con respuestas HTTP estructuradas
- `ResourceNotFoundException` — lanzada cuando un recurso no se encuentra (HTTP 404)

## Tests

```bash
mvn test
mvn verify   # incluye reporte JaCoCo (target/site/jacoco/index.html)
```

Cobertura mínima configurada: **85%**.

Casos cubiertos: iniciar partida, responder correcta/incorrecta, timeout, abandonar, ranking global, ranking por categoría, estadísticas de usuario, gestión de palabras, excepciones.
