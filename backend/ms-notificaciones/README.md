# ms-notificaciones

Microservicio Spring Boot responsable del envío de notificaciones por correo electrónico. Consume eventos de suscripción desde RabbitMQ y envía emails HTML a los usuarios mediante SMTP.

## Stack

- **Spring Boot 3.3** — framework principal
- **RabbitMQ (AMQP)** — mensajería asíncrona desde ms-pagos-subscripciones
- **JavaMailSender (SMTP)** — envío de emails HTML
- **Eureka Client** — descubrimiento de servicios
- **Swagger / OpenAPI 3** — documentación

## Puerto

| Servicio         | Puerto |
|------------------|--------|
| ms-notificaciones| 8085   |
| RabbitMQ (AMQP)  | 5672   |

## Swagger UI

```
http://localhost:8085/swagger-ui.html
```

> Este microservicio no expone endpoints REST propios. Opera completamente de forma **asíncrona** consumiendo mensajes de RabbitMQ.

## Flujo de mensajería

```
ms-pagos-subscripciones
        │
        │  RabbitMQ
        │  Exchange: notara.exchange
        │  Routing key: suscripcion.#
        ▼
  notificaciones.queue
        │
        ▼
  NotificacionListener
        │
        ▼
  EmailService ──(SMTP)──> Usuario
```

## Configuración RabbitMQ

| Parámetro         | Valor                  |
|-------------------|------------------------|
| Exchange          | `notara.exchange`      |
| Queue             | `notificaciones.queue` |
| Dead Letter Exchange | `notara.dlx`        |
| Routing Key       | `suscripcion.#`        |

## Variables de entorno

```env
SERVER_PORT=8085
EUREKA_URL=http://eureka-server:8761/eureka
RABBITMQ_HOST=rabbitmq
RABBITMQ_PORT=5672
RABBITMQ_USERNAME=guest
RABBITMQ_PASSWORD=guest
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=tu_email@gmail.com
MAIL_PASSWORD=tu_app_password
MAIL_FROM=noreply@notara.cl
```

## Eventos procesados

| Evento             | Acción del email                            |
|--------------------|---------------------------------------------|
| `CREADA`           | Email de bienvenida con detalles del plan   |
| `CANCELADA`        | Email de confirmación de cancelación        |
| `RENOVADA`         | Email de confirmación de renovación         |

## Configuración de correo (Gmail)

Para usar Gmail como SMTP, se requiere una **App Password** (no la contraseña normal):

1. Activar verificación en dos pasos en la cuenta de Google
2. Ir a **Cuenta > Seguridad > Contraseñas de aplicación**
3. Generar una contraseña para "Correo / Windows"
4. Usar esa contraseña como `MAIL_PASSWORD`

## Tests

```bash
mvn test
mvn verify   # incluye reporte JaCoCo (target/site/jacoco/index.html)
```

Casos cubiertos: procesamiento de evento CREADA, CANCELADA, RENOVADA, manejo de errores de envío, listener RabbitMQ.
