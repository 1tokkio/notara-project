# ms-pagos-subscripciones

Microservicio Spring Boot para la gestión de suscripciones y pagos. Publica eventos a RabbitMQ cuando ocurren cambios en el estado de una suscripción.

## Stack

- **Spring Boot 3.3** — framework principal
- **PostgreSQL** — base de datos de suscripciones
- **RabbitMQ (AMQP)** — mensajería asíncrona hacia ms-notificaciones
- **Eureka Client** — descubrimiento de servicios
- **Swagger / OpenAPI 3** — documentación de endpoints

## Puerto

| Servicio                  | Puerto |
|---------------------------|--------|
| ms-pagos-subscripciones   | 8084   |
| PostgreSQL                | 5434   |
| RabbitMQ (AMQP)           | 5672   |
| RabbitMQ (Management UI)  | 15672  |

## Endpoints

```
GET    /suscripciones                          Listar todas las suscripciones
GET    /suscripciones/{id}                     Obtener suscripción por ID
GET    /suscripciones/usuario/{idUsuario}      Obtener suscripción de un usuario
POST   /suscripciones                          Crear nueva suscripción
PUT    /suscripciones/{id}/cancelar            Cancelar suscripción
PUT    /suscripciones/{id}/renovar             Renovar suscripción
```

## Swagger UI

```
http://localhost:8084/swagger-ui.html
```

## Variables de entorno

```env
SERVER_PORT=8084
EUREKA_URL=http://eureka-server:8761/eureka
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres-pagos:5432/notara-pagos-db
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=password
RABBITMQ_HOST=rabbitmq
RABBITMQ_PORT=5672
RABBITMQ_USERNAME=guest
RABBITMQ_PASSWORD=guest
```

## Mensajería RabbitMQ

Publica eventos al exchange `notara.exchange` con routing key `suscripcion.<EVENTO>`:

| Acción     | Routing Key            | Consumidor          |
|------------|------------------------|---------------------|
| Crear      | `suscripcion.CREADA`   | ms-notificaciones   |
| Cancelar   | `suscripcion.CANCELADA`| ms-notificaciones   |
| Renovar    | `suscripcion.RENOVADA` | ms-notificaciones   |

```
ms-pagos-subscripciones ──(RabbitMQ)──> notara.exchange ──> notificaciones.queue ──> ms-notificaciones
```

## Modelos principales

| Entidad       | Descripción |
|---------------|-------------|
| `Suscripcion` | Suscripción de usuario con plan (BASICO / PREMIUM) y estado (ACTIVA / CANCELADA / EXPIRADA) |
| `Plan`        | Enum: BASICO, PREMIUM |
| `EstadoSuscripcion` | Enum: ACTIVA, CANCELADA, EXPIRADA |

## Manejo de errores

- `GlobalExceptionHandler` — handler global con respuestas HTTP estructuradas
- `ResourceNotFoundException` — lanzada cuando un recurso no se encuentra (HTTP 404)

## Tests

```bash
mvn test
mvn verify   # incluye reporte JaCoCo (target/site/jacoco/index.html)
```

Cobertura mínima configurada: **85%**.

Casos cubiertos: crear suscripción, obtener por ID, obtener por usuario, cancelar, renovar, publicación de eventos RabbitMQ, manejo de excepciones.
