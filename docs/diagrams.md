# Diagramas del Sistema — Notara

## 1. Diagrama de Arquitectura

Visión general del sistema con todos los microservicios, bases de datos e infraestructura.

```mermaid
graph TB
    subgraph Cliente
        FE["🖥️ Frontend\nNext.js 14 · React 18\npuerto :3001"]
    end

    subgraph Gateway["API Gateway (Node.js · puerto :3000)"]
        GW["api-gateway\nEnrutamiento · Proxy · JWT forward"]
    end

    subgraph Microservicios["Microservicios (Spring Boot 3.3 / Node.js)"]
        MS1["ms-usuarios\n:8081\nAutenticación · JWT · Spring Security"]
        MS2["ms-canciones\n:3002\nSpotify · Letras · Lecciones · Node.js"]
        MS3["ms-notas-metas\n:8083\nNotas · Metas · OpenFeign"]
        MS4["ms-pagos-subscripciones\n:8084\nSuscripciones · RabbitMQ Publisher"]
        MS5["ms-notificaciones\n:8085\nEmail SMTP · RabbitMQ Consumer"]
        MS6["ms-vocabulario\n:8086\nJuego · Ranking · Redis Cache"]
        EUR["eureka-server\n:8761\nService Discovery"]
    end

    subgraph Datos["Capa de Datos"]
        PG1[("PostgreSQL\nnotara-usuarios-db\n:5432")]
        PG2[("PostgreSQL\nnotara-notas-metas-db\n:5433")]
        PG3[("PostgreSQL\nnotara-pagos-db\n:5434")]
        PG4[("PostgreSQL\nnotara-vocabulario-db\n:5435")]
        MG[("MongoDB\nlinguaflow\n:27017")]
        RD[("Redis\ncaché ranking\n:6379")]
        RMQ["RabbitMQ\n:5672 AMQP\n:15672 UI"]
    end

    FE -->|HTTP| GW
    GW -->|proxy| MS1
    GW -->|proxy| MS2
    GW -->|proxy| MS3
    GW -->|proxy| MS4
    GW -->|proxy| MS6

    MS1 --- EUR
    MS2 --- EUR
    MS3 --- EUR
    MS4 --- EUR
    MS5 --- EUR
    MS6 --- EUR

    MS3 -->|OpenFeign| MS1

    MS4 -->|AMQP publish| RMQ
    RMQ -->|AMQP consume| MS5

    MS1 --- PG1
    MS1 --- RD
    MS2 --- MG
    MS2 --- RD
    MS3 --- PG2
    MS4 --- PG3
    MS6 --- PG4
    MS6 --- RD
```

---

## 2. Diagrama ERD — Entidades por Microservicio

### ms-usuarios

```mermaid
erDiagram
    USUARIO {
        bigint id PK
        varchar nombre
        varchar email
        varchar password
        varchar rol
        timestamp created_at
    }
    PROGRESO {
        bigint id PK
        bigint id_usuario FK
        varchar leccion_completada
        int puntaje
        timestamp fecha
    }
    USUARIO ||--o{ PROGRESO : "tiene"
```

### ms-notas-metas

```mermaid
erDiagram
    NOTA {
        bigint id PK
        bigint id_usuario
        varchar titulo
        text contenido
        varchar estado
        timestamp created_at
        timestamp updated_at
    }
    META {
        bigint id PK
        bigint id_usuario
        varchar titulo
        text descripcion
        varchar estado
        timestamp created_at
        timestamp updated_at
    }
```

### ms-pagos-subscripciones

```mermaid
erDiagram
    SUSCRIPCION {
        bigint id PK
        bigint id_usuario
        varchar nombre_usuario
        varchar email_usuario
        varchar plan
        varchar estado
        timestamp fecha_inicio
        timestamp fecha_fin
    }
```

### ms-vocabulario

```mermaid
erDiagram
    PALABRA {
        bigint id PK
        varchar palabra
        text definicion
        varchar pista
        varchar categoria
        varchar dificultad
        boolean activa
    }
    PARTIDA {
        bigint id PK
        bigint id_usuario
        varchar nombre_usuario
        varchar categoria
        varchar estado
        int total_preguntas
        int pregunta_actual_index
        int puntuacion
        int palabras_correctas
        int racha_actual
        int mejor_racha
        int tiempo_maximo_segundos
        timestamp fecha_inicio
        timestamp fecha_fin
    }
    PREGUNTA_PARTIDA {
        bigint id PK
        bigint partida_id FK
        bigint palabra_id FK
        int orden
        varchar estado
        varchar respuesta_usuario
        boolean es_correcta
        long tiempo_respuesta_ms
        int puntos_obtenidos
        timestamp fecha_entregada
    }
    RANKING {
        bigint id PK
        bigint id_usuario
        varchar nombre_usuario
        varchar categoria
        int puntuacion_total
        int mejor_puntuacion
        int total_partidas
        int total_palabras_correctas
        int total_palabras
        int mejor_racha
    }

    PARTIDA ||--o{ PREGUNTA_PARTIDA : "contiene"
    PALABRA ||--o{ PREGUNTA_PARTIDA : "usada en"
    PARTIDA }o--o| RANKING : "actualiza"
```

---

## 3. Diagrama de Secuencia — Flujo de Autenticación JWT

```mermaid
sequenceDiagram
    actor Usuario
    participant FE as Frontend (Next.js)
    participant GW as API Gateway
    participant MU as ms-usuarios

    Usuario->>FE: Ingresar email y contraseña
    FE->>GW: POST /auth/login { email, password }
    GW->>MU: POST /auth/login { email, password }
    MU->>MU: Validar credenciales (BCrypt)
    MU->>MU: Generar accessToken (HS256, exp: configurable)
    MU->>MU: Generar refreshToken (exp: 7x accessToken)
    MU-->>GW: 200 { accessToken, refreshToken }
    GW-->>FE: 200 { accessToken, refreshToken }
    FE->>FE: Guardar tokens en memoria/cookie

    Usuario->>FE: Acceder a recurso protegido
    FE->>GW: GET /notas (Authorization: Bearer <token>)
    GW->>GW: Validar JWT (firma + expiración)
    GW->>MU: GET /notas (forward con token)
    MU->>MU: JwtFilter valida token
    MU-->>GW: 200 { datos }
    GW-->>FE: 200 { datos }
    FE-->>Usuario: Mostrar datos
```

---

## 4. Diagrama de Secuencia — Flujo Suscripción + Notificación (RabbitMQ)

```mermaid
sequenceDiagram
    actor Usuario
    participant FE as Frontend
    participant GW as API Gateway
    participant MP as ms-pagos-subscripciones
    participant RMQ as RabbitMQ
    participant MN as ms-notificaciones
    participant SMTP as Servidor SMTP

    Usuario->>FE: Contratar plan PREMIUM
    FE->>GW: POST /suscripciones { idUsuario, plan: PREMIUM }
    GW->>MP: POST /suscripciones

    MP->>MP: Crear suscripción en PostgreSQL
    MP->>RMQ: Publicar evento (exchange: notara.exchange, key: suscripcion.CREADA)
    MP-->>GW: 201 { suscripcion }
    GW-->>FE: 201 { suscripcion }
    FE-->>Usuario: Confirmación en pantalla

    RMQ->>MN: Entregar mensaje a notificaciones.queue
    MN->>MN: NotificacionListener.procesarEvento()
    MN->>MN: EmailService.enviarBienvenida()
    MN->>SMTP: Enviar email HTML
    SMTP-->>Usuario: 📧 "¡Bienvenido a Notara Premium!"
```

---

## 5. Diagrama de Secuencia — Flujo de Partida de Vocabulario con Redis

```mermaid
sequenceDiagram
    actor Usuario
    participant FE as Frontend
    participant MV as ms-vocabulario
    participant PG as PostgreSQL
    participant RD as Redis

    Usuario->>FE: Ver ranking global
    FE->>MV: GET /vocabulario/ranking
    MV->>RD: ¿Existe clave "ranking-global"?
    RD-->>MV: MISS (no existe o expiró)
    MV->>PG: findTop10ByCategoriaIsNullOrderByMejorPuntuacionDesc()
    PG-->>MV: Lista de rankings
    MV->>RD: SET "ranking-global" (TTL: 5 min)
    MV-->>FE: 200 Top 10 usuarios

    Usuario->>FE: Jugar partida de vocabulario
    FE->>MV: POST /vocabulario/partidas { categoria: MUSICA, preguntas: 10 }
    MV->>PG: Crear Partida + Preguntas aleatorias
    MV-->>FE: Primera pregunta

    loop Para cada pregunta
        FE->>MV: POST /vocabulario/partidas/1/responder { respuesta }
        MV->>MV: Calcular puntos y racha
        MV->>PG: Guardar resultado pregunta
    end

    Note over MV: Última pregunta — FINALIZADA
    MV->>MV: RankingService.actualizarRanking()
    MV->>PG: Actualizar/crear entrada Ranking
    MV->>RD: EVICT "ranking-global" (allEntries)
    MV->>RD: EVICT "ranking-categoria" (allEntries)
    MV->>RD: EVICT "estadisticas-usuario::1"
    MV-->>FE: { gameOver: true, puntuacion: 850 }

    Usuario->>FE: Ver ranking actualizado
    FE->>MV: GET /vocabulario/ranking
    MV->>RD: ¿Existe clave "ranking-global"?
    RD-->>MV: MISS (fue eviccionado)
    MV->>PG: Consultar ranking actualizado
    MV->>RD: SET "ranking-global" (TTL: 5 min)
    MV-->>FE: Top 10 actualizado con nueva puntuación
```
