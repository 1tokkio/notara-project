# ms-usuarios

Microservicio Spring Boot para autenticación y gestión de usuarios.

## Stack

- **Spring Boot 3.2** — framework principal
- **Spring Security** — autenticación y autorización
- **PostgreSQL** — base de datos de usuarios y progreso
- **JWT (JJWT)** — tokens de acceso y refresco
- **Swagger/OpenAPI** — documentación de endpoints

## Puerto

| Servicio     | Puerto |
|--------------|--------|
| ms-usuarios  | 8081   |
| PostgreSQL   | 5432   |

## Endpoints

```
POST /auth/register           Registro de nuevo usuario
POST /auth/login              Login, devuelve accessToken + refreshToken
GET  /usuarios/{id}           Obtener usuario por ID
GET  /usuarios                Listar todos los usuarios
GET  /progress/{userId}       Progreso del usuario
POST /progress/{userId}       Guardar progreso
```

## Variables de entorno

```env
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/notara-usuarios-db
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres
JWT_SECRET=tu_clave_secreta
```

## Tests

```bash
mvn test
```

Cubre: registro de usuarios, login exitoso, login con email inexistente,
login con contraseña incorrecta, registro con encriptación y eliminación.
