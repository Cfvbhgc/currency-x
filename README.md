# CurrencyX

Currency exchange microservice built with Spring Boot, gRPC and Redis caching. Provides real-time exchange rate lookups, currency conversion and historical rate tracking through both REST and gRPC interfaces.

## Architecture

```
                          +------------------+
                          |   REST Clients   |
                          +--------+---------+
                                   |
                            HTTP :8080
                                   |
+------------------+      +--------+---------+      +------------------+
|  gRPC Clients    +----->|   CurrencyX App  +----->|     Redis        |
|  (port 9090)     |      |                  |      |  (Cache + Store) |
+------------------+      +--------+---------+      +------------------+
                                   |
                          +--------+---------+
                          | Mock Central Bank|
                          |    (Built-in)    |
                          +------------------+
```

### Component Overview

```
com.currencyx/
├── config/           # Redis, gRPC and app configuration
├── model/            # Domain objects (CurrencyRate, ConversionResult, RateHistory)
├── exception/        # Custom exception hierarchy with global handler
├── service/          # Business logic, mock Central Bank client
│   └── impl/         # Service implementations with caching
├── controller/       # REST API endpoints
│   └── dto/          # Request/response DTOs with validation
├── grpc/             # gRPC service implementation
└── cache/            # Redis cache operations
```

## Tech Stack

- **Java 17** with Spring Boot 3.2
- **gRPC** via grpc-spring-boot-starter for high-performance inter-service communication
- **Redis** for caching exchange rates and storing rate history
- **Protobuf** for gRPC service definitions
- **Gradle** build system
- **Docker** with multi-stage builds

## Getting Started

### Prerequisites

- Java 17+
- Docker and Docker Compose
- Gradle 8+ (or use the wrapper)

### Run with Docker

```bash
docker-compose up --build
```

The application starts on port **8080** (REST) and **9090** (gRPC).

### Run locally

Start Redis first:
```bash
docker run -d -p 6379:6379 redis:7-alpine
```

Then build and run:
```bash
./gradlew bootRun
```

## REST API

### Get All Exchange Rates

```
GET /api/rates
```

Response:
```json
[
  {
    "fromCurrency": "USD",
    "toCurrency": "EUR",
    "rate": 0.923400,
    "timestamp": "2024-01-15T10:30:00Z"
  }
]
```

### Get Specific Rate

```
GET /api/rates/{from}/{to}
```

Example: `GET /api/rates/USD/EUR`

### Convert Currency

```
POST /api/convert
Content-Type: application/json

{
  "fromCurrency": "USD",
  "toCurrency": "EUR",
  "amount": 1000.00
}
```

Response:
```json
{
  "fromCurrency": "USD",
  "toCurrency": "EUR",
  "originalAmount": 1000.00,
  "convertedAmount": 923.40,
  "rate": 0.923400,
  "timestamp": "2024-01-15T10:30:00Z"
}
```

### Get Rate History

```
GET /api/history/{from}/{to}?limit=30
```

Example: `GET /api/history/USD/EUR?limit=10`

## gRPC API

Service definition in `src/main/proto/currency_service.proto`:

| Method | Request | Response | Description |
|--------|---------|----------|-------------|
| `GetRate` | `RateRequest` | `RateResponse` | Get current exchange rate |
| `Convert` | `ConvertRequest` | `ConvertResponse` | Convert currency amount |
| `GetRateHistory` | `RateHistoryRequest` | `RateHistoryResponse` | Get historical rates |

### gRPC Client Example (grpcurl)

```bash
grpcurl -plaintext -d '{"from_currency":"USD","to_currency":"EUR"}' \
  localhost:9090 currencyx.CurrencyService/GetRate
```

## Supported Currencies

USD, EUR, GBP, JPY, CNY, RUB, CHF, CAD, AUD, BRL

## Configuration

Key properties in `application.yml`:

| Property | Default | Description |
|----------|---------|-------------|
| `server.port` | 8080 | REST API port |
| `grpc.server.port` | 9090 | gRPC server port |
| `spring.data.redis.host` | localhost | Redis host |
| `currency.cache.rate-ttl-seconds` | 300 | Rate cache TTL |
| `currency.cache.history-ttl-seconds` | 3600 | History cache TTL |
| `currency.mock-bank.fluctuation-percent` | 0.5 | Rate fluctuation range |

## Build

```bash
./gradlew clean build
```

Run tests:
```bash
./gradlew test
```
