# Blog Site Microservices Application

A comprehensive microservice architecture application for managing blogs with user registration, blog management, and category-based search capabilities.

## Architecture Overview

This application follows Domain-Driven Design (DDD) and implements:
- **Microservices Architecture**: Separate services for User Management, Blog Management, and Query Service
- **API Gateway**: Single entry point using Spring Cloud Gateway
- **CQRS Pattern**: Command Query Responsibility Segregation for event sourcing
- **Service Discovery**: Eureka Server for service registration
- **Security**: JWT-based authentication and SSL configuration
- **Databases**: MongoDB and MySQL for different microservices

## Microservices

### 1. **eureka-server** (Port: 8761)
Service discovery server for all microservices

### 2. **api-gateway** (Port: 8080)
API Gateway for routing requests to appropriate microservices

### 3. **user-service** (Port: 8081)
Handles user registration and authentication
- Database: MySQL
- Endpoints: User registration, authentication

### 4. **blog-command-service** (Port: 8082)
Handles all write operations (Commands) for blogs
- Database: MongoDB
- Endpoints: Add blog, delete blog
- Event sourcing for audit trail

### 5. **blog-query-service** (Port: 8083)
Handles all read operations (Queries) for blogs
- Database: MongoDB (read replica)
- Endpoints: Search blogs by category, get user blogs, filter by duration

### 6. **event-store-service** (Port: 8084)
Stores all events for CQRS pattern
- Database: MongoDB

## Technology Stack

- **Framework**: Spring Boot 3.2.x
- **Language**: Java 17
- **Databases**: MySQL 8.0, MongoDB 6.0
- **API Documentation**: SpringDoc OpenAPI 3.0
- **Security**: Spring Security with JWT
- **Testing**: JUnit 5, Mockito, TestContainers
- **Build Tool**: Maven
- **Containerization**: Docker & Docker Compose

## REST API Endpoints

### User Management
- `POST /api/v1.0/blogsite/user/register` - Register new user

### Blog Management
- `POST /api/v1.0/blogsite/user/blogs/add/{blogname}` - Add new blog (Secured)
- `DELETE /api/v1.0/blogsite/user/delete/{blogname}` - Delete blog (Secured)

### Blog Query
- `GET /api/v1.0/blogsite/blogs/info/{category}` - Get blogs by category
- `GET /api/v1.0/blogsite/user/getall` - Get all blogs by user (Secured)
- `GET /api/v1.0/blogsite/blogs/get/{category}/{durationFromRange}/{durationToRange}` - Get blogs by category and duration

## Design Patterns Implemented

1. **Domain-Driven Design (DDD)**: Clear separation of domains
2. **CQRS Pattern**: Separate models for read and write operations
3. **Event Sourcing**: All state changes captured as events
4. **Builder Pattern**: For model object composition (response DTOs)
5. **Repository Pattern**: Data access abstraction
6. **Factory Pattern**: Object creation

## Getting Started

### Prerequisites
- Java 17 or higher
- Maven 3.8+
- Docker & Docker Compose
- MySQL 8.0
- MongoDB 6.0

### Quick Start (Recommended)

1. **Build all services**:
```bash
mvn clean install
```

2. **Start with Docker Compose**:
```bash
docker-compose up -d
```

3. **Verify data persistence** (optional):
```powershell
# Run the persistence test script
.\test-data-persistence.ps1
```

This will create test data, restart services, and verify that all data persists correctly.

### 📚 Documentation

- **[GETTING_STARTED.md](GETTING_STARTED.md)** - Detailed setup and installation guide
- **[LOCAL_PERSISTENCE_VALIDATION.md](LOCAL_PERSISTENCE_VALIDATION.md)** - Data persistence verification and troubleshooting
- **[API_TESTING_GUIDE.md](API_TESTING_GUIDE.md)** - Complete API testing examples
- **[DEPLOYMENT_GUIDE.md](DEPLOYMENT_GUIDE.md)** - Production deployment instructions
- **[IMPLEMENTATION_SUMMARY.md](IMPLEMENTATION_SUMMARY.md)** - Technical architecture details

### Run Individual Services
```bash
# Start Eureka Server first
cd eureka-server
mvn spring-boot:run

# Start API Gateway
cd api-gateway
mvn spring-boot:run

# Start other services
cd user-service
mvn spring-boot:run

cd blog-command-service
mvn spring-boot:run

cd blog-query-service
mvn spring-boot:run
```

## API Documentation

Once services are running, access Swagger UI:
- API Gateway: http://localhost:8080/swagger-ui.html
- User Service: http://localhost:8081/swagger-ui.html
- Blog Command Service: http://localhost:8082/swagger-ui.html
- Blog Query Service: http://localhost:8083/swagger-ui.html

## 🎯 Key Features

### ✅ Data Persistence
- **MySQL** for user data with Docker named volumes
- **MongoDB** for blog data with Docker named volumes
- Data survives container restarts and system reboots
- Automatic schema creation via JPA and MongoDB
- See [LOCAL_PERSISTENCE_VALIDATION.md](LOCAL_PERSISTENCE_VALIDATION.md) for details

### 🔐 Security

All write operations require JWT authentication:
1. Register user via `/api/v1.0/blogsite/user/register`
2. Login to get JWT token
3. Include token in Authorization header: `Bearer <token>`

## Testing

Run all tests:
```bash
mvn test
```

Generate code coverage report:
```bash
mvn clean test jacoco:report
```

View coverage report: `target/site/jacoco/index.html`

## Code Quality

Run SonarQube analysis:
```bash
mvn clean verify sonar:sonar
```

## Monitoring & Logging

- Centralized logging with ELK Stack
- Actuator endpoints enabled for monitoring
- Prometheus metrics exposed

## License

Copyright © 2026 Blog Site Application
