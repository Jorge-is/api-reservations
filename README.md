# API Reservations

![Java](https://img.shields.io/badge/Java-17-blue.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.1.3-brightgreen.svg)
![Docker](https://img.shields.io/badge/Docker-Enabled-blue.svg)

API RESTful para la gestión de reservas, construida con Java 17 y Spring Boot. El proyecto incluye integración con sistemas externos (API Catalog), resiliencia con Resilience4j, y está preparado para ser desplegado mediante Docker.

## 🚀 Tecnologías Usadas
- **Java 17**
- **Spring Boot 3.1.3** (Web, Actuator, Validation, AOP)
- **MapStruct** para mapeo de DTOs.
- **Resilience4j** (Circuit Breaker y Rate Limiter).
- **Springdoc OpenAPI (Swagger)** para la documentación de la API.
- **Docker & Docker Compose**

## 🏗️ Arquitectura y Estructura
El proyecto sigue una arquitectura en capas tradicional, asegurando separación de responsabilidades:
- `controller`: Expone los endpoints REST.
- `service`: Contiene la lógica de negocio.
- `repository`: Gestiona el acceso a los datos.
- `connector`: Comunicación con otras APIs (`api-catalog`).
- `exception`: Manejo centralizado de excepciones con `@RestControllerAdvice`.

## ⚙️ Instalación y Ejecución

### Ejecución Local (sin Docker)
1. Clonar el repositorio.
2. Contar con **Java 17** y **Maven** instalados localmente.
3. Ejecutar el proyecto usando Maven:
   ```bash
   ./mvnw spring-boot:run
   ```
La aplicación se levantará en `http://localhost:8080`.

### Ejecución con Docker y Docker Compose
La aplicación está configurada para ejecutarse a través de contenedores. Para levantar toda la infraestructura:

1. Compila el archivo `.jar` localmente:
   ```bash
   ./mvnw clean package -DskipTests
   ```
2. Construye la imagen de Docker de la API de reservaciones:
   ```bash
   docker build -t jorgeafais/api-reservations:1.0.0 .
   ```
3. *(Obligatorio)* Recuerda que este proyecto depende de `api-catalog`. Asegúrate de tener la imagen `jorgeafais/api-catalog:1.1.0` construida en tu máquina local o accesible en Docker Hub.
4. Levanta los contenedores:
   ```bash
   docker-compose up -d
   ```

## 🛠️ Variables de Entorno y Configuración
El archivo `application.yml` y `application-docker.yml` manejan la configuración base.
Para entornos de Docker, se sobrescribe la conexión al catálogo externo bajo el host `api-catalog`.

| Variable / Properties | Descripción | Valor por defecto |
| :--- | :--- | :--- |
| `server.port` | Puerto donde corre la aplicación | `8080` |
| `http-connector.hosts.api-catalog.host` | Host del microservicio de catálogo | `localhost` (local) / `api-catalog` (docker) |
| `http-connector.hosts.api-catalog.port` | Puerto de catálogo | `6070` |

## 📚 Endpoints Principales

Puedes acceder a la UI de Swagger (una vez levantada la app de manera local en modo openapi) para explorar todos los endpoints:
> `http://localhost:8080/api/v3/api-docs`

A continuación, la lista de los endpoints principales (con url base `/api/reservation`):

| Método | Endpoint | Descripción |
| :--- | :--- | :--- |
| `GET` | `/` | Lista todas las reservaciones. |
| `GET` | `/{id}` | Obtiene una reservación por su ID. |
| `POST` | `/` | Crea una nueva reservación. (Sujeta a Rate Limiting: 2 req/3s) |
| `PUT` | `/{id}` | Actualiza una reservación existente. |
| `DELETE` | `/{id}` | Elimina una reservación por su ID. |
| `GET` | `/actuator/health` | Verifica el estado del servicio de API. |

### Ejemplo de Uso (Crear Reservación)
```bash
curl -X POST http://localhost:8080/api/reservation \
-H "Content-Type: application/json" \
-d '{
  "passengers": [
    {
      "firstName": "Jorge",
      "lastName": "Flores",
      "documentType": "DNI",
      "documentNumber": "12345678",
      "birthday": "1999-05-01"
    }
  ],
  "itinerary": {
    "segment": [
      {
        "origin": "BUE",
        "destination": "MIA",
        "departure": "2024-12-31",
        "arrival": "2025-01-01",
        "carrier": "AA"
      }
    ],
    "price": {
      "basePrice": 100.0,
      "totalTax": 20.0,
      "totalPrice": 120.0
    }
  }
}'
```

---
**Nota sobre resiliencia**: La creación de reservas tiene un *Rate Limiter* implementado. Si excedes el límite (2 llamadas concurrentes en 3 segundos), el servicio te responderá con una excepción custom gestionada por `@ControllerAdvice`.
