# PayFlow - Payment Processing Engine

PayFlow is a robust, Spring Boot-based Payment Processing Engine that handles account management and secure payment transactions. It is designed to be reliable and scalable, integrating with PostgreSQL for persistent storage and RabbitMQ for message queuing.

## Features
- **Account Management**: Create and retrieve customer accounts with balance tracking.
- **Payment Processing**: Process payments securely between accounts.
- **Idempotency Support**: Safely handle retries in payment requests using idempotency keys.
- **Risk & Velocity Checks**: Configurable limits on maximum transaction amounts and transaction frequency to prevent abuse.

## Tech Stack
- **Language**: Java 17
- **Framework**: Spring Boot 3.2.3 (Web, Data JPA, Validation, AMQP)
- **Database**: PostgreSQL 15
- **Message Broker**: RabbitMQ
- **Tools**: Maven, Docker & Docker Compose, Lombok

## Prerequisites
Ensure you have the following installed on your system:
- Java 17 or higher
- Maven 3.8+
- Docker and Docker Compose

## Getting Started

### 1. Start Infrastructure Dependencies
The project relies on PostgreSQL and RabbitMQ. A `docker-compose.yml` file is provided to easily spin up these dependencies locally.

Run the following command from the project root:
```bash
docker-compose up -d
```
This will start:
- PostgreSQL on port `5432`
- RabbitMQ on port `5672` (and the management UI on `15672`)

### 2. Run the Application
Once the database and message broker are running, you can start the Spring Boot application using Maven:

```bash
mvn spring-boot:run
```
The application will start on port `8080` by default.

## Configuration
Application properties can be adjusted in `src/main/resources/application.yml`. Key configurations include:
- **Database Connection**: Configured for `localhost:5432/payflow`
- **RabbitMQ Connection**: Configured for `localhost:5672`
- **Risk Rules**:
  - `payflow.risk.max-transaction-amount` (default: 10000.00)
  - `payflow.risk.velocity.max-transactions` (default: 5)
  - `payflow.risk.velocity.time-window-seconds` (default: 60)

## API Documentation
Please refer to the [API_DOCS.md](API_DOCS.md) file for detailed information about the exposed REST endpoints.

## Testing
Run unit and integration tests using Maven:
```bash
mvn test
```
