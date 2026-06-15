# ms-notas-metas

Microservicio Spring Boot para la gestión de notas y metas de aprendizaje de los usuarios.

## Stack

- **Spring Boot 3.3** — framework principal
- **PostgreSQL** — base de datos de notas y metas
- **Spring Cloud OpenFeign** — comunicación con ms-usuarios
- **Eureka Client** — descubrimiento de servicios
- **Swagger / OpenAPI 3** — documentación de endpoints

## Puerto

| Servicio          | Puerto |
|-------------------|--------|
| ms-notas-metas    | 8083   |
| PostgreSQL        | 5433   |

## Endpoints

```
# Notas
GET    /notas                          Listar todas las notas
POST   /notas                          Crear nueva nota
GET    /notas/{id}                     Obtener nota por ID
GET    /notas/usuario/{idUsuario}      Obtener notas de un usuario
PUT    /notas/{id}                     Actualizar nota
DELETE /notas/{id}                     Eliminar nota

# Metas
GET    /metas                          Listar todas las metas
POST   /metas                          Crear nueva meta
GET    /metas/{id}                     Obtener meta por ID
GET    /metas/usuario/{idUsuario}      Obtener metas de un usuario
PUT    /metas/{id}                     Actualizar meta
DELETE /metas/{id}                     Eliminar meta
```

## Swagger UI

```
http://localhost:8083/swagger-ui.html
```

## Variables de entorno

```env
SERVER_PORT=8083
EUREKA_URL=http://eureka-server:8761/eureka
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres-notas-metas:5432/notara-notas-metas-db
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=password
```

## Comunicación entre servicios

Este microservicio consulta `ms-usuarios` vía **OpenFeign** para validar que el usuario exista antes de crear o modificar notas y metas.

```
ms-notas-metas ──(Feign)──> ms-usuarios (8081)
```

## Modelos principales

| Entidad | Descripción |
|---------|-------------|
| `Nota`  | Nota de texto del usuario con estado (ACTIVA / ARCHIVADA) |
| `Meta`  | Meta de aprendizaje con estado (PENDIENTE / EN_PROGRESO / COMPLETADA) |

## Manejo de errores

- `GlobalExceptionHandler` — handler global con respuestas HTTP estructuradas
- `ResourceNotFoundException` — lanzada cuando un recurso no se encuentra (HTTP 404)

## Tests

```bash
mvn test
mvn verify   # incluye reporte JaCoCo (target/site/jacoco/index.html)
```

Cobertura mínima configurada: **85%** (instrucciones) y **80%** (ramas).

Casos cubiertos: crear nota, obtener nota por ID, obtener por usuario, actualizar, eliminar, crear meta, obtener meta, casos de error 404.
