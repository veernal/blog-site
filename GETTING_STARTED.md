# Getting Started with Blog Site Microservices

This guide will help you set up and run the Blog Site Microservices application on your local machine.

## Prerequisites

Before you begin, ensure you have the following installed:

- **Java 17** or higher ([Download](https://adoptium.net/))
- **Maven 3.8+** ([Download](https://maven.apache.org/download.cgi))
- **Docker Desktop** ([Download](https://www.docker.com/products/docker-desktop))
- **Git** (for cloning the repository)
- **Postman** or **cURL** (for API testing)

## Quick Start (5 minutes)

### Step 1: Build the Common Library

```powershell
# Navigate to common-lib directory
cd common-lib

# Install to local Maven repository
mvn clean install -DskipTests

# Go back to root
cd ..
```

### Step 2: Start Infrastructure Services

```powershell
# Start MySQL and MongoDB using Docker
docker-compose up -d mysql mongodb
```

Wait for databases to be ready (about 30 seconds). You can check with:

```powershell
# Check MySQL
docker exec blogsite-mysql mysqladmin ping -h localhost

# Check MongoDB
docker exec blogsite-mongodb mongosh --eval "db.runCommand('ping')"
```

### Step 3: Start Microservices

Open **5 separate terminal windows** and run each service:

**Terminal 1 - Eureka Server:**
```powershell
cd eureka-server
mvn spring-boot:run
```
Wait until you see: "Started EurekaServerApplication"

**Terminal 2 - API Gateway:**
```powershell
cd api-gateway
mvn spring-boot:run
```
Wait until you see: "Started ApiGatewayApplication"

**Terminal 3 - User Service:**
```powershell
cd user-service
mvn spring-boot:run
```

**Terminal 4 - Blog Command Service:**
```powershell
cd blog-command-service
mvn spring-boot:run
```

**Terminal 5 - Blog Query Service:**
```powershell
cd blog-query-service
mvn spring-boot:run
```

### Step 4: Verify Everything is Running

Open your browser and check:

1. **Eureka Dashboard**: http://localhost:8761
   - You should see all services registered

2. **API Gateway Health**: http://localhost:8080/actuator/health
   - Should return: `{"status":"UP"}`

3. **Swagger UI**: http://localhost:8080/swagger-ui.html
   - Interactive API documentation

## Your First API Call

### 1. Register a User

```powershell
curl -X POST http://localhost:8080/api/v1.0/blogsite/user/register `
  -H "Content-Type: application/json" `
  -d '{
    "userName": "John Doe",
    "userEmail": "john.doe@example.com",
    "password": "Password123"
  }'
```

**Expected Response:**
```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "userId": "...",
    "userName": "John Doe",
    "email": "john.doe@example.com",
    "isActive": true
  }
}
```

### 2. Login to Get JWT Token

```powershell
curl -X POST http://localhost:8080/api/v1.0/blogsite/user/login `
  -H "Content-Type: application/json" `
  -d '{
    "email": "john.doe@example.com",
    "password": "Password123"
  }'
```

**Expected Response:**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "userId": "...",
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }
}
```

**IMPORTANT:** Copy the `token` value for the next steps!

### 3. Create a Blog (Use your token)

```powershell
$token = "YOUR_TOKEN_HERE"

curl -X POST "http://localhost:8080/api/v1.0/blogsite/user/blogs/add/Understanding%20Microservices%20Architecture%20Patterns" `
  -H "Content-Type: application/json" `
  -H "Authorization: Bearer $token" `
  -d '{
    "blogName": "Understanding Microservices Architecture Patterns",
    "category": "Software Engineering Best Practices and Modern Development",
    "article": "' + (your 1000+ word article here) + '",
    "authorName": "John Doe"
  }'
```

### 4. Search Blogs by Category (No authentication needed)

```powershell
curl -X GET "http://localhost:8080/api/v1.0/blogsite/blogs/info/Software%20Engineering%20Best%20Practices%20and%20Modern%20Development"
```

### 5. Get Your Blogs

```powershell
curl -X GET http://localhost:8080/api/v1.0/blogsite/user/getall `
  -H "Authorization: Bearer $token"
```

## Using Postman

### Import the Collection

1. Open Postman
2. Click **Import** → **Raw Text**
3. Paste the API examples from `API_TESTING_GUIDE.md`
4. Create an environment variable `{{baseUrl}}` = `http://localhost:8080`
5. Create an environment variable `{{token}}` and set it after login

### Recommended Testing Flow

1. **Register User** → Copy user details
2. **Login** → Copy JWT token to `{{token}}`
3. **Add Blog** → Use token
4. **Get User Blogs** → Use token
5. **Search by Category** → No token needed
6. **Delete Blog** → Use token

## Troubleshooting

### Problem: "Port already in use"

**Solution:**
```powershell
# Find process using port (e.g., 8080)
netstat -ano | findstr :8080

# Kill the process (use PID from above)
taskkill /PID <PID> /F
```

### Problem: "Cannot connect to MySQL"

**Solution:**
```powershell
# Check if MySQL container is running
docker ps | findstr mysql

# Restart MySQL
docker-compose restart mysql

# Check logs
docker logs blogsite-mysql
```

### Problem: "Cannot connect to MongoDB"

**Solution:**
```powershell
# Check if MongoDB container is running
docker ps | findstr mongodb

# Restart MongoDB
docker-compose restart mongodb

# Check logs
docker logs blogsite-mongodb
```

### Problem: "Service not registering with Eureka"

**Solution:**
1. Wait 30 seconds for registration
2. Check Eureka dashboard: http://localhost:8761
3. Restart the service
4. Check service logs for errors

### Problem: "JWT token invalid"

**Solution:**
1. Token expires after 24 hours - login again
2. Ensure token is in correct format: `Bearer <token>`
3. Check for extra spaces or newlines

## Testing

### Run Unit Tests

```powershell
# Test all services
mvn clean test

# Test specific service
cd user-service
mvn test
```

### Generate Code Coverage Report

```powershell
# Generate coverage
mvn clean test jacoco:report

# View report (open in browser)
start target/site/jacoco/index.html
```

## Stopping the Application

### Stop Services

Press `Ctrl+C` in each terminal window running the services.

### Stop Docker Containers

```powershell
docker-compose down
```

### Stop and Remove All Data

```powershell
# Stop and remove containers, networks, and volumes
docker-compose down -v
```

## Next Steps

1. **Explore Swagger UI**: http://localhost:8080/swagger-ui.html
   - Interactive API documentation
   - Try all endpoints

2. **Check Eureka Dashboard**: http://localhost:8761
   - Monitor service health
   - See registered instances

3. **View Actuator Endpoints**:
   - http://localhost:8080/actuator/health
   - http://localhost:8081/actuator/health
   - http://localhost:8082/actuator/health

4. **Read Documentation**:
   - `API_TESTING_GUIDE.md` - Complete API examples
   - `DEPLOYMENT_GUIDE.md` - Production deployment
   - `IMPLEMENTATION_SUMMARY.md` - Architecture details

## Common Use Cases

### Create Multiple Blogs

```powershell
# Blog 1
curl -X POST "http://localhost:8080/api/v1.0/blogsite/user/blogs/add/Blog1" ...

# Blog 2
curl -X POST "http://localhost:8080/api/v1.0/blogsite/user/blogs/add/Blog2" ...
```

### Search Blogs with Date Range

```powershell
curl -X GET "http://localhost:8080/api/v1.0/blogsite/blogs/get/Software/2026-01-01/2026-01-31"
```

### Delete a Blog

```powershell
curl -X DELETE "http://localhost:8080/api/v1.0/blogsite/user/delete/Blog1" `
  -H "Authorization: Bearer $token"
```

## Development Tips

### Hot Reload

Add Spring Boot DevTools to enable hot reload:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-devtools</artifactId>
    <scope>runtime</scope>
</dependency>
```

### Debug Mode

Run service in debug mode:

```powershell
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"
```

Attach debugger to port 5005.

### Change Log Level

Add to `application.yml`:

```yaml
logging:
  level:
    com.blogsite: DEBUG
```

## Architecture Overview

```
┌─────────────┐
│   Client    │
└──────┬──────┘
       │
       ↓
┌─────────────────┐
│  API Gateway    │ ← JWT Validation
│   Port: 8080    │ ← Rate Limiting
└────────┬────────┘
         │
    ┌────┴────┬─────────────┬──────────────┐
    ↓         ↓             ↓              ↓
┌────────┐ ┌──────┐  ┌──────────┐  ┌────────────┐
│ User   │ │ Blog │  │  Blog    │  │Event Store │
│Service │ │Cmd   │  │  Query   │  │  Service   │
│:8081   │ │:8082 │  │  :8083   │  │   :8084    │
└───┬────┘ └──┬───┘  └────┬─────┘  └──────┬─────┘
    │         │            │                │
    ↓         ↓            ↓                ↓
┌────────┐ ┌──────────────────────┐  ┌──────────┐
│ MySQL  │ │     MongoDB          │  │  Kafka   │
└────────┘ └──────────────────────┘  └──────────┘
```

## Support

For issues or questions:

1. Check logs in `logs/` directory of each service
2. Review `IMPLEMENTATION_SUMMARY.md` for architecture details
3. See `API_TESTING_GUIDE.md` for API examples
4. Check `DEPLOYMENT_GUIDE.md` for deployment issues

## Success Checklist

- [ ] Java 17 installed
- [ ] Maven working (`mvn --version`)
- [ ] Docker Desktop running
- [ ] Common-lib built
- [ ] MySQL container running
- [ ] MongoDB container running
- [ ] Eureka Server started (http://localhost:8761)
- [ ] API Gateway started (http://localhost:8080)
- [ ] All services registered in Eureka
- [ ] User registered successfully
- [ ] JWT token obtained
- [ ] Blog created successfully
- [ ] Blog search working

---

**Congratulations!** 🎉 

You now have a fully functional microservices application running locally. Start exploring the APIs and building amazing features!
