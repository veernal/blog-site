# Deployment Guide

## Prerequisites

- Java 17 or higher
- Maven 3.8+
- Docker & Docker Compose
- MySQL 8.0
- MongoDB 6.0
- Apache Kafka (optional, included in docker-compose)
- Redis (optional, for caching and rate limiting)

## Local Development Setup

### 1. Build All Services

```powershell
# Windows PowerShell
.\build-all.ps1
```

Or manually:

```bash
# Build common library
cd common-lib
mvn clean install

# Build each service
cd ../eureka-server && mvn clean package
cd ../api-gateway && mvn clean package
cd ../user-service && mvn clean package
cd ../blog-command-service && mvn clean package
cd ../blog-query-service && mvn clean package
cd ../event-store-service && mvn clean package
```

### 2. Start Infrastructure Services

#### Option A: Using Docker Compose (Recommended)

```bash
docker-compose up -d mysql mongodb kafka redis
```

#### Option B: Manual Setup

**MySQL:**
```bash
docker run -d --name mysql \
  -e MYSQL_ROOT_PASSWORD=rootpassword \
  -e MYSQL_DATABASE=userdb \
  -e MYSQL_USER=bloguser \
  -e MYSQL_PASSWORD=blogpassword \
  -p 3306:3306 \
  mysql:8.0
```

**MongoDB:**
```bash
docker run -d --name mongodb \
  -e MONGO_INITDB_ROOT_USERNAME=admin \
  -e MONGO_INITDB_ROOT_PASSWORD=adminpassword \
  -p 27017:27017 \
  mongo:6.0
```

**Kafka (with Zookeeper):**
```bash
docker run -d --name zookeeper -p 2181:2181 zookeeper:3.7

docker run -d --name kafka \
  -p 9092:9092 \
  -e KAFKA_ZOOKEEPER_CONNECT=localhost:2181 \
  -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://localhost:9092 \
  confluentinc/cp-kafka:latest
```

**Redis:**
```bash
docker run -d --name redis -p 6379:6379 redis:latest
```

### 3. Start Microservices

Start services in the following order:

```bash
# 1. Eureka Server (Service Discovery)
cd eureka-server
mvn spring-boot:run

# 2. API Gateway (wait for Eureka to start)
cd ../api-gateway
mvn spring-boot:run

# 3. Event Store Service
cd ../event-store-service
mvn spring-boot:run

# 4. User Service
cd ../user-service
mvn spring-boot:run

# 5. Blog Command Service
cd ../blog-command-service
mvn spring-boot:run

# 6. Blog Query Service
cd ../blog-query-service
mvn spring-boot:run
```

### 4. Verify Services

Check Eureka Dashboard:
```
http://localhost:8761
```

All services should be registered:
- API-GATEWAY
- USER-SERVICE
- BLOG-COMMAND-SERVICE
- BLOG-QUERY-SERVICE
- EVENT-STORE-SERVICE

## Docker Deployment

### Build and Run All Services

```bash
# Build all services
mvn clean package -DskipTests

# Start all services with Docker Compose
docker-compose up -d

# Check logs
docker-compose logs -f

# Stop all services
docker-compose down
```

### Check Service Health

```bash
# API Gateway
curl http://localhost:8080/actuator/health

# User Service
curl http://localhost:8081/actuator/health

# Blog Command Service
curl http://localhost:8082/actuator/health

# Blog Query Service
curl http://localhost:8083/actuator/health

# Event Store Service
curl http://localhost:8084/actuator/health
```

## Production Deployment

### 1. Environment Variables

Create a `.env` file:

```env
# Database
MYSQL_HOST=prod-mysql-host
MYSQL_PORT=3306
MYSQL_DATABASE=userdb
MYSQL_USER=bloguser
MYSQL_PASSWORD=<strong-password>

# MongoDB
MONGO_HOST=prod-mongo-host
MONGO_PORT=27017
MONGO_DATABASE=blogdb
MONGO_USERNAME=admin
MONGO_PASSWORD=<strong-password>

# JWT
JWT_SECRET=<your-secret-key-min-256-bits>

# Kafka
KAFKA_BOOTSTRAP_SERVERS=prod-kafka:9092

# Redis
REDIS_HOST=prod-redis
REDIS_PORT=6379
```

### 2. SSL/TLS Configuration

Generate SSL certificate:

```bash
keytool -genkeypair -alias blogsite \
  -keyalg RSA -keysize 2048 \
  -storetype PKCS12 -keystore keystore.p12 \
  -validity 365
```

Update `application.yml` in API Gateway:

```yaml
server:
  port: 8443
  ssl:
    enabled: true
    key-store: classpath:keystore.p12
    key-store-password: changeit
    key-store-type: PKCS12
    key-alias: blogsite
```

### 3. Kubernetes Deployment

Create Kubernetes manifests:

```yaml
# deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: api-gateway
spec:
  replicas: 3
  selector:
    matchLabels:
      app: api-gateway
  template:
    metadata:
      labels:
        app: api-gateway
    spec:
      containers:
      - name: api-gateway
        image: blogsite/api-gateway:1.0.0
        ports:
        - containerPort: 8080
        env:
        - name: EUREKA_CLIENT_SERVICEURL_DEFAULTZONE
          value: http://eureka-server:8761/eureka/
```

Deploy:

```bash
kubectl apply -f k8s/
```

### 4. Monitoring

**Prometheus Configuration:**

```yaml
scrape_configs:
  - job_name: 'spring-actuator'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets:
        - 'localhost:8080'
        - 'localhost:8081'
        - 'localhost:8082'
        - 'localhost:8083'
```

**Access Metrics:**
```
http://localhost:8080/actuator/prometheus
```

## CI/CD Pipeline

### GitHub Actions Example

```yaml
name: Build and Deploy

on:
  push:
    branches: [ main ]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v2
    
    - name: Set up JDK 17
      uses: actions/setup-java@v2
      with:
        java-version: '17'
    
    - name: Build with Maven
      run: mvn clean package
    
    - name: Run Tests
      run: mvn test
    
    - name: Code Coverage
      run: mvn jacoco:report
    
    - name: Build Docker Images
      run: docker-compose build
    
    - name: Push to Registry
      run: docker-compose push
```

## Troubleshooting

### Services Not Connecting

Check Eureka registration:
```bash
curl http://localhost:8761/eureka/apps
```

### Database Connection Issues

```bash
# Test MySQL connection
mysql -h localhost -u bloguser -p userdb

# Test MongoDB connection
mongosh mongodb://admin:adminpassword@localhost:27017
```

### View Logs

```bash
# Docker logs
docker-compose logs -f service-name

# Local logs
tail -f user-service/logs/user-service.log
```

## Performance Tuning

### JVM Options

```bash
java -jar -Xms512m -Xmx1024m \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  app.jar
```

### Database Indexing

Ensure MongoDB indexes are created:

```javascript
db.blogs.createIndex({ "category": 1, "createdAt": -1 })
db.blogs.createIndex({ "userId": 1, "deleted": 1 })
```

## Backup Strategy

### MySQL Backup

```bash
mysqldump -u bloguser -p userdb > backup.sql
```

### MongoDB Backup

```bash
mongodump --uri="mongodb://admin:password@localhost:27017" \
  --db=blogdb --out=/backup
```

## Security Checklist

- [ ] Change default passwords
- [ ] Enable SSL/TLS
- [ ] Configure firewall rules
- [ ] Set up rate limiting
- [ ] Enable audit logging
- [ ] Rotate JWT secrets regularly
- [ ] Use secrets management (e.g., Vault)
- [ ] Enable CORS with specific origins
- [ ] Implement API versioning
- [ ] Set up DDoS protection
