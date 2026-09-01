# Backend Library — Guía de inicio rápido

## Requisitos previos

| Herramienta | Versión mínima |
|-------------|---------------|
| Java        | 21            |
| Maven       | 3.9+          |
| Docker      | 24+           |

---

## 1. Levantar la base de datos (PostgreSQL con Docker)

El proyecto incluye un `docker-compose.yml` listo para usar.

```bash
docker compose up -d
```

Esto levanta un contenedor PostgreSQL con:

| Parámetro  | Valor        |
|------------|--------------|
| Host       | localhost    |
| Puerto     | 5432         |
| Base de datos | library_db |
| Usuario    | postgres     |
| Contraseña | postgres     |

Para detener el contenedor:

```bash
docker compose down
```

Para detener y eliminar los datos (volumen):

```bash
docker compose down -v
```

---

## 2. Levantar la aplicación Spring Boot

```bash
./mvnw spring-boot:run
```

En Windows:

```cmd
mvnw.cmd spring-boot:run
```

La aplicación arranca en: `http://localhost:8080`

---

## 3. Swagger / OpenAPI

Una vez que la app esté corriendo, accedé a la documentación interactiva:

| URL | Descripción |
|-----|-------------|
| `http://localhost:8080/swagger-ui/index.html` | Swagger UI (interfaz visual) |
| `http://localhost:8080/v3/api-docs` | JSON OpenAPI spec |
| `http://localhost:8080/v3/api-docs.yaml` | YAML OpenAPI spec |

> En Swagger UI podés probar todos los endpoints directamente desde el navegador.

---

## 4. Autenticación JWT

La API usa JWT con access token y refresh token via cookies.

**Flujo básico:**
1. Registrarse o hacer login desde los endpoints de auth en Swagger.
2. El servidor devuelve las cookies `access_token` y `refresh_token` automáticamente.
3. Swagger UI las envía en cada request subsiguiente.

Variables de entorno opcionales para ajustar la expiración:

```bash
AUTH_ACCESS_EXPIRATION=900000      # 15 minutos (default)
AUTH_REFRESH_EXPIRATION=604800000  # 7 días (default)
```

---

## 5. Orden recomendado para levantar todo

```bash
# 1. Base de datos
docker compose up -d

# 2. Aplicación
./mvnw spring-boot:run

# 3. Abrir Swagger
start http://localhost:8080/swagger-ui/index.html
```

---

## 6. Verificar que todo esté corriendo

```bash
# Verificar contenedor Docker
docker ps

# Verificar la app (debe devolver 200 o la página de Swagger)
curl http://localhost:8080/swagger-ui/index.html
```

---

## 7. Estructura del proyecto — Arquitectura Hexagonal

El proyecto sigue **arquitectura hexagonal** (también llamada Ports & Adapters). La idea central es que el **dominio** no depende de nada externo: ni de la base de datos, ni de HTTP, ni de ningún framework. Todo lo externo se conecta a través de puertos (interfaces) y adaptadores (implementaciones).

```
src/main/java/dev/leo/library/
│
├── domain/                         ← Núcleo del negocio (sin dependencias externas)
│   ├── model/                      ← Enums del dominio (UserRole, CopyStatus, CopyCondition)
│   ├── exception/                  ← Excepciones de negocio (BookNotFoundException, etc.)
│   └── port/
│       ├── input/                  ← Puertos de entrada: interfaces que exponen los casos de uso
│       │   ├── AuthorUseCase.java
│       │   ├── BookUseCase.java
│       │   ├── BookCopyUseCase.java
│       │   ├── CategoryUseCase.java
│       │   ├── LoanUseCase.java
│       │   └── UserUseCase.java
│       └── output/                 ← Puertos de salida: interfaces hacia persistencia u otros servicios
│
├── application/                    ← Casos de uso (implementan los puertos de entrada)
│   ├── service/                    ← Lógica de aplicación (AuthorService, BookService, etc.)
│   └── dto/
│       ├── request/                ← DTOs de entrada (AuthorRequest, BookRequest, LoginRequest, etc.)
│       └── response/               ← DTOs de salida (UserResponse, CategorySelectResponse)
│
├── infrastructure/                 ← Todo lo externo: HTTP, DB, seguridad, config
│   ├── adapter/
│   │   ├── input/
│   │   │   ├── rest/               ← Controllers REST (adaptadores de entrada HTTP)
│   │   │   │   ├── AuthController.java
│   │   │   │   ├── AuthorController.java
│   │   │   │   ├── BookController.java
│   │   │   │   ├── BookCopyController.java
│   │   │   │   ├── CategoryController.java
│   │   │   │   ├── LoanController.java
│   │   │   │   └── UserController.java
│   │   │   └── scheduler/          ← Tareas programadas (OverdueScheduler — préstamos vencidos)
│   │   └── output/
│   │       └── persistence/        ← Adaptadores de salida hacia PostgreSQL
│   │           ├── entity/         ← Entidades JPA (AuthorEntity, BookEntity, LoanEntity, etc.)
│   │           ├── repository/     ← Repositorios Spring Data JPA
│   │           └── adapter/        ← Specs para filtros/búsquedas dinámicas (Specifications)
│   ├── config/
│   │   ├── SecurityConfig.java     ← Configuración de Spring Security
│   │   ├── SwaggerConfig.java      ← Configuración de OpenAPI/Swagger
│   │   └── DataSeeder.java         ← Datos iniciales al arrancar
│   └── security/                   ← JWT: filtros, utilidades, blacklist, cookies
│       ├── JWTAuthFilter.java
│       ├── JwtUtil.java
│       ├── CookieTokenManager.java
│       ├── TokenBlacklistService.java
│       ├── UserDetailService.java
│       └── UserPrincipal.java
│
├── shared/                         ← Clases transversales reutilizables
│   ├── dto/                        ← Respuestas genéricas (PaginatedResponse, SuccessResponse)
│   └── exception/                  ← GlobalExceptionHandler (manejo centralizado de errores)
│
└── BackendLibraryApplication.java  ← Punto de entrada
```

### Flujo de una request

```
HTTP Request
    │
    ▼
Controller (infrastructure/adapter/input/rest)
    │  llama al puerto de entrada
    ▼
UseCase interface (domain/port/input)
    │  implementado por
    ▼
Service (application/service)
    │  llama al puerto de salida
    ▼
Output Port interface (domain/port/output)
    │  implementado por
    ▼
Persistence Adapter (infrastructure/adapter/output/persistence)
    │
    ▼
PostgreSQL
```

### Regla de dependencias

```
infrastructure  →  application  →  domain
                                   (no depende de nada)
```

El dominio nunca importa clases de Spring, JPA ni ningún framework. Eso lo hace testeable de forma aislada y fácil de cambiar de tecnología sin tocar la lógica de negocio.

---

## Referencias

- [Spring Boot Docs](https://docs.spring.io/spring-boot/4.1.1/reference/)
- [Spring Data JPA](https://docs.spring.io/spring-boot/4.1.1/reference/data/sql.html#data.sql.jpa-and-spring-data)
- [springdoc-openapi](https://springdoc.org/)
- [Docker Compose](https://docs.docker.com/compose/)
