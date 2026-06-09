# api-reservations

API RESTful orientada a producción para la gestión de reservas de vuelos, construida con Java 17 y Spring Boot 3. Diseñada con arquitectura en capas, autenticación JWT, patrones de resiliencia, versionado de esquema de base de datos y documentación OpenAPI completa.

![Java](https://img.shields.io/badge/Java-17-blue?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.1.3-brightgreen?logo=springboot)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue?logo=postgresql)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker)
![License](https://img.shields.io/badge/license-MIT-green)

---

## Tabla de contenidos

- [Descripción general](#descripción-general)
- [Características](#características)
- [Stack tecnológico](#stack-tecnológico)
- [Arquitectura](#arquitectura)
- [Estructura del proyecto](#estructura-del-proyecto)
- [Primeros pasos](#primeros-pasos)
- [Variables de entorno](#variables-de-entorno)
- [Autenticación](#autenticación)
- [Referencia de la API](#referencia-de-la-api)
- [Modelo de datos](#modelo-de-datos)
- [Patrones de resiliencia](#patrones-de-resiliencia)
- [Ejecución de tests](#ejecución-de-tests)

---

## Descripción general

`api-reservations` gestiona el ciclo de vida completo de una reserva de vuelo: creación, consulta, actualización y eliminación. Cada reserva está compuesta por uno o más pasajeros y un itinerario que contiene segmentos de vuelo con su información de precios.

El servicio se integra con una API externa (`api-catalog`) para validar que los códigos de ciudad de origen y destino sean ubicaciones IATA reales antes de persistir cualquier reserva.

---

## Características

- CRUD completo de reservas de vuelos
- Autenticación stateless con JWT
- Validación de entrada con Jakarta Validation y un validador personalizado de códigos de ciudad IATA
- Listado paginado de reservas
- Circuit Breaker sobre las llamadas al catálogo externo (Resilience4j)
- Rate Limiting en la creación de reservas (2 requests / 3 segundos)
- Manejo centralizado de errores con respuestas JSON estructuradas
- Versionado del esquema de base de datos con Flyway
- Documentación OpenAPI 3 a través de Swagger UI
- Configuración lista para Docker Compose

---

## Stack tecnológico

| Capa | Tecnología |
|------|------------|
| Lenguaje | Java 17 |
| Framework | Spring Boot 3.1.3 |
| Persistencia | Spring Data JPA · PostgreSQL 15 · Flyway |
| Seguridad | Spring Security · JJWT 0.11.5 |
| Resiliencia | Resilience4j (Circuit Breaker, Rate Limiter) |
| Cliente HTTP | Spring WebFlux WebClient |
| Mapeo | MapStruct 1.5.5 |
| Documentación | SpringDoc OpenAPI 2 (Swagger UI) |
| Testing | JUnit 5 · Mockito · Spring Security Test |
| Build | Maven |
| Contenedores | Docker · Docker Compose |

---

## Arquitectura

El proyecto sigue una **arquitectura en capas** con separación clara de responsabilidades:

```
┌──────────────────────────────────────────────────────────┐
│                     Capa Controller                       │
│       Endpoints REST · validación de entrada              │
│          rate limiting · documentación OpenAPI            │
├──────────────────────────────────────────────────────────┤
│                      Capa Service                         │
│       Lógica de negocio · validación de ciudades          │
│              gestión de transacciones                     │
├──────────────────────────────────────────────────────────┤
│                    Capa Repository                        │
│              Spring Data JPA · PostgreSQL                 │
├───────────────────────────┬──────────────────────────────┤
│        Capa Mapper        │       Capa Connector          │
│   Conversores MapStruct   │  WebClient · Circuit Breaker  │
│   Mapeo DTO ↔ Entidad     │  Integración con api-catalog  │
└───────────────────────────┴──────────────────────────────┘
```

**Aspectos transversales:** Spring Security (cadena de filtros JWT), `@RestControllerAdvice` para el manejo global de excepciones, y AOP de Resilience4j para las anotaciones de resiliencia.

---

## Estructura del proyecto

```
src/
└── main/
    ├── java/com/edteam/reservations/
    │   ├── controller/          # Controladores REST
    │   │   └── resource/        # ReservationResource (contrato OpenAPI)
    │   ├── service/             # Lógica de negocio
    │   ├── repository/          # Repositorios Spring Data JPA
    │   ├── model/               # Entidades JPA
    │   ├── dto/                 # DTOs de request y response
    │   ├── mapper/              # Conversores MapStruct (DTO ↔ Entidad)
    │   ├── connector/           # Integración WebClient con api-catalog
    │   │   ├── configuration/   # Propiedades de host y endpoint
    │   │   └── response/        # DTOs de respuesta de la API externa
    │   ├── security/            # Filtro JWT, JwtUtil, SecurityConfig
    │   ├── exception/           # ReservationException, APIExceptionHandler
    │   ├── enums/               # APIError (catálogo de estados y mensajes)
    │   └── validation/          # @CityFormatConstraint (códigos IATA)
    └── resources/
        ├── application.yml          # Configuración local
        ├── application-docker.yml   # Configuración Docker / producción
        └── db/migration/            # Migraciones SQL de Flyway
```

---

## Primeros pasos

### Requisitos previos

- Java 17+
- Maven 3.8+
- Docker (para la base de datos y, opcionalmente, el servicio de catálogo)

### Opción 1 — Ejecución local (recomendada para desarrollo)

**1. Levantar la base de datos PostgreSQL:**

```bash
docker run -d \
  --name api-reservations-db \
  -e POSTGRES_DB=flights_reservations \
  -e POSTGRES_USER=myuser \
  -e POSTGRES_PASSWORD=mypassword \
  -p 5432:5432 \
  postgres:15
```

**2. Levantar el mock del catálogo** (necesario para crear y actualizar reservas):

```bash
python mock-catalog.py
```

> `mock-catalog.py` está incluido en la raíz del repositorio. Levanta un servidor HTTP liviano en el puerto `6070` que responde con datos válidos para cualquier código de ciudad IATA.

**3. Ejecutar la aplicación:**

```bash
./mvnw spring-boot:run
```

La API estará disponible en `http://localhost:8080/api/v1`.

---

### Opción 2 — Ejecución con Docker Compose

Primero compilar y construir la imagen:

```bash
./mvnw clean package -DskipTests
docker build -t jorgeafais/api-reservations:1.0.0 .
```

Luego levantar todos los servicios:

```bash
docker-compose up -d
```

> El compose inicia `api-reservations`, `api-reservations-db` (PostgreSQL), `api-catalog` y `api-catalog-db` (MySQL). Todas las credenciales se gestionan mediante variables de entorno con valores por defecto.

---

## Variables de entorno

Todos los valores sensibles están externalizados. Se proveen defaults para desarrollo local.

| Variable | Descripción | Valor por defecto |
|----------|-------------|-------------------|
| `DB_USERNAME` | Usuario de PostgreSQL | `myuser` |
| `DB_PASSWORD` | Contraseña de PostgreSQL | `mypassword` |
| `JWT_SECRET` | Clave HS256 codificada en Base64 (mín. 32 bytes) | *(valor demo — reemplazar en producción)* |
| `AUTH_USERNAME` | Usuario de login | `admin` |
| `AUTH_PASSWORD` | Contraseña de login | `admin123` |

> En producción, siempre reemplazar `JWT_SECRET`, `DB_USERNAME` y `DB_PASSWORD` con valores reales. Nunca usar los defaults.

---

## Autenticación

La API utiliza **autenticación JWT stateless**. Todos los endpoints salvo `/auth/token`, la UI de Swagger y el health de Actuator requieren un token `Bearer` válido.

### 1. Obtener el token

```bash
curl -X POST http://localhost:8080/api/v1/auth/token \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "admin123"}'
```

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

### 2. Usar el token

Incluirlo como `Bearer` token en cada request:

```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

Los tokens tienen una validez de **24 horas**.

---

## Referencia de la API

**URL base:** `http://localhost:8080/api/v1`

La documentación interactiva está disponible en `/api/v1/swagger-ui.html` con la aplicación en ejecución.

### Reservas

| Método | Endpoint | Auth | Descripción | Estado |
|--------|----------|------|-------------|--------|
| `GET` | `/reservation` | ✓ | Listar todas las reservas (paginado) | `200` |
| `GET` | `/reservation/{id}` | ✓ | Obtener una reserva por ID | `200` / `404` |
| `POST` | `/reservation` | ✓ | Crear una nueva reserva | `201` / `400` |
| `PUT` | `/reservation/{id}` | ✓ | Actualizar una reserva existente | `200` / `404` |
| `DELETE` | `/reservation/{id}` | ✓ | Eliminar una reserva | `204` / `404` |

> `POST /reservation` está limitado a **2 requests cada 3 segundos**. Superar ese límite retorna `429 Too Many Requests`.

### Autenticación

| Método | Endpoint | Auth | Descripción |
|--------|----------|------|-------------|
| `POST` | `/auth/token` | — | Obtener un token JWT |

### Paginación

`GET /reservation` acepta los parámetros estándar de paginación de Spring:

```
GET /reservation?page=0&size=10&sort=id,asc
```

### Respuestas de error

Todos los errores siguen una estructura consistente:

```json
{
  "description": "There are attributes with wrong values",
  "reasons": [
    "firstName - First name is mandatory.",
    "origin - Invalid format of the city."
  ]
}
```

---

## Modelo de datos

### Body de creación / actualización de reserva

```json
{
  "passengers": [
    {
      "firstName": "Jorge",
      "lastName": "Flores",
      "documentType": "DNI",
      "documentNumber": "12345678",
      "birthday": "1990-05-15"
    }
  ],
  "itinerary": {
    "segments": [
      {
        "origin": "MAD",
        "destination": "EZE",
        "departure": "2024-12-01T10:00:00",
        "arrival": "2024-12-01T22:00:00",
        "carrier": "IB"
      }
    ],
    "price": {
      "basePrice": 500.00,
      "totalTax": 100.00,
      "totalPrice": 600.00
    }
  }
}
```

### Reglas de validación

| Campo | Restricción |
|-------|-------------|
| `passengers` | Al menos uno requerido |
| `firstName` / `lastName` | No puede estar en blanco |
| `birthday` | Debe ser una fecha en el pasado |
| `origin` / `destination` | Exactamente 3 letras mayúsculas (código IATA) |
| `departure` / `arrival` / `carrier` | No puede estar en blanco |

> Los códigos de ciudad también se validan en tiempo de ejecución contra el servicio `api-catalog`. Códigos desconocidos retornan `404 City origin or destination not found`.

---

## Patrones de resiliencia

### Circuit Breaker — integración con `api-catalog`

Configurado sobre `CatalogConnector.getCity()`. Evita fallos en cascada cuando el servicio de catálogo no está disponible.

| Parámetro | Valor |
|-----------|-------|
| Umbral de tasa de fallo | 50% |
| Tamaño de ventana deslizante | 5 llamadas |
| Tiempo de espera en estado abierto | 10 segundos |
| Transición a semi-abierto | Automática |

Cuando el circuito está abierto, todos los requests `POST` y `PUT` retornan inmediatamente `404 City origin or destination not found`.

### Rate Limiter — `POST /reservation`

| Parámetro | Valor |
|-----------|-------|
| Límite por período | 2 requests |
| Período de refresco | 3 segundos |
| Duración de timeout | 1 segundo |

Los requests que superan el límite retornan `429 Too Many Requests`.

---

## Ejecución de tests

```bash
./mvnw test
```

La suite de tests incluye:

| Clase | Tipo | Cobertura |
|-------|------|-----------|
| `ReservationServiceTest` | Unitario (Mockito) | Lógica de negocio, validación de ciudades, caminos de excepción |
| `ReservationControllerTest` | Integración (`@WebMvcTest`) | Capa HTTP, seguridad, códigos de estado |
| `CityFormatValidatorTest` | Unitario | Reglas de validación de códigos IATA |
