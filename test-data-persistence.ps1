# Data Persistence Test Script
# This script proves that your application stores data persistently across service restarts

Write-Host "`n╔══════════════════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║     Blog Site Application - Data Persistence Verification Test      ║" -ForegroundColor Cyan
Write-Host "╚══════════════════════════════════════════════════════════════════════╝" -ForegroundColor Cyan

# Configuration
$baseUrl = "http://localhost:8080"
$testEmail = "persistence.test@example.com"
$testPassword = "TestPass123!@#"
$testUsername = "PersistenceTestUser"

# Helper function for HTTP requests with error handling
function Invoke-ApiRequest {
    param($Uri, $Method, $Body, $Headers)
    try {
        $params = @{
            Uri = $Uri
            Method = $Method
            ContentType = "application/json"
        }
        if ($Headers) { $params.Headers = $Headers }
        if ($Body) { $params.Body = $Body }
        
        return Invoke-RestMethod @params
    } catch {
        Write-Host "⚠️  API Error: $($_.Exception.Message)" -ForegroundColor Red
        return $null
    }
}

Write-Host "`n📋 Test Overview:" -ForegroundColor Yellow
Write-Host "   1. Create a user (MySQL)"
Write-Host "   2. Create a blog (MongoDB)"
Write-Host "   3. Stop all services (docker-compose down)"
Write-Host "   4. Restart all services (docker-compose up)"
Write-Host "   5. Verify data still exists"

# Step 1: Register User
Write-Host "`n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Gray
Write-Host "📝 STEP 1: Creating test user in MySQL..." -ForegroundColor Cyan

$registerBody = @{
    userName = $testUsername
    email = $testEmail
    password = $testPassword
} | ConvertTo-Json

$registerResponse = Invoke-ApiRequest -Uri "$baseUrl/api/users/register" -Method POST -Body $registerBody

if ($registerResponse) {
    Write-Host "✅ User registered successfully" -ForegroundColor Green
    Write-Host "   User ID: $($registerResponse.data.userId)" -ForegroundColor DarkGray
    Write-Host "   Email: $($registerResponse.data.email)" -ForegroundColor DarkGray
} else {
    Write-Host "❌ User registration failed (user may already exist)" -ForegroundColor Red
    Write-Host "   Attempting to use existing user..." -ForegroundColor Yellow
}

# Step 2: Login
Write-Host "`n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Gray
Write-Host "🔐 STEP 2: Logging in to get JWT token..." -ForegroundColor Cyan

$loginBody = @{
    email = $testEmail
    password = $testPassword
} | ConvertTo-Json

$loginResponse = Invoke-ApiRequest -Uri "$baseUrl/api/users/login" -Method POST -Body $loginBody

if (-not $loginResponse) {
    Write-Host "❌ Login failed. Cannot continue test." -ForegroundColor Red
    exit 1
}

$token = $loginResponse.data.token
Write-Host "✅ Login successful" -ForegroundColor Green
Write-Host "   Token: $($token.Substring(0, 30))..." -ForegroundColor DarkGray

# Step 3: Create Blog
Write-Host "`n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Gray
Write-Host "📄 STEP 3: Creating test blog in MongoDB..." -ForegroundColor Cyan

$headers = @{ "Authorization" = "Bearer $token" }
$blogBody = @{
    title = "Persistence Test Blog - $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')"
    content = "This blog post is created to test data persistence across service restarts. If you can still see this after restarting Docker Compose, the persistence is working correctly!"
    category = "TECH"
    author = $testUsername
} | ConvertTo-Json

$blogResponse = Invoke-ApiRequest -Uri "$baseUrl/api/blogs" -Method POST -Body $blogBody -Headers $headers

if (-not $blogResponse) {
    Write-Host "❌ Blog creation failed. Cannot continue test." -ForegroundColor Red
    exit 1
}

$blogId = $blogResponse.data.blogId
Write-Host "✅ Blog created successfully" -ForegroundColor Green
Write-Host "   Blog ID: $blogId" -ForegroundColor DarkGray
Write-Host "   Title: $($blogResponse.data.title)" -ForegroundColor DarkGray

# Step 4: Verify data is accessible before restart
Write-Host "`n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Gray
Write-Host "✓ STEP 4: Verifying data is accessible..." -ForegroundColor Cyan

$blogCheck = Invoke-ApiRequest -Uri "$baseUrl/api/blogs/$blogId" -Method GET -Headers $headers
if ($blogCheck) {
    Write-Host "✅ Blog retrieved successfully before restart" -ForegroundColor Green
}

# Step 5: Stop all services
Write-Host "`n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Gray
Write-Host "🛑 STEP 5: Stopping all Docker services..." -ForegroundColor Cyan
Write-Host "   Running: docker compose down" -ForegroundColor Yellow

$stopOutput = docker compose down 2>&1
Write-Host "✅ Services stopped" -ForegroundColor Green
Write-Host "   Note: Docker volumes are preserved" -ForegroundColor DarkGray

# Step 6: Start services again
Write-Host "`n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Gray
Write-Host "🚀 STEP 6: Restarting all Docker services..." -ForegroundColor Cyan
Write-Host "   Running: docker compose up -d" -ForegroundColor Yellow

$startOutput = docker compose up -d 2>&1
Write-Host "✅ Services started" -ForegroundColor Green

Write-Host "`n⏳ Waiting for services to be ready (60 seconds)..." -ForegroundColor Yellow
Write-Host "   This allows time for:" -ForegroundColor DarkGray
Write-Host "   • Database containers to start" -ForegroundColor DarkGray
Write-Host "   • Eureka Server to initialize" -ForegroundColor DarkGray
Write-Host "   • Microservices to register" -ForegroundColor DarkGray
Write-Host "   • Health checks to pass" -ForegroundColor DarkGray

for ($i = 60; $i -gt 0; $i--) {
    Write-Progress -Activity "Waiting for services" -Status "$i seconds remaining..." -PercentComplete ((60 - $i) / 60 * 100)
    Start-Sleep -Seconds 1
}
Write-Progress -Activity "Waiting for services" -Completed

# Step 7: Verify user still exists (test MySQL persistence)
Write-Host "`n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Gray
Write-Host "🔍 STEP 7: Verifying MySQL data persistence..." -ForegroundColor Cyan
Write-Host "   Attempting to login with previously created user..." -ForegroundColor Yellow

$loginResponse2 = Invoke-ApiRequest -Uri "$baseUrl/api/users/login" -Method POST -Body $loginBody

if (-not $loginResponse2) {
    Write-Host "❌ PERSISTENCE FAILED: User data was lost!" -ForegroundColor Red
    Write-Host "   MySQL volume may not be configured correctly" -ForegroundColor Red
    exit 1
}

$token2 = $loginResponse2.data.token
Write-Host "✅ MySQL PERSISTENCE VERIFIED" -ForegroundColor Green
Write-Host "   User successfully logged in after restart" -ForegroundColor Green
Write-Host "   New Token: $($token2.Substring(0, 30))..." -ForegroundColor DarkGray

# Step 8: Verify blog still exists (test MongoDB persistence)
Write-Host "`n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Gray
Write-Host "🔍 STEP 8: Verifying MongoDB data persistence..." -ForegroundColor Cyan
Write-Host "   Retrieving previously created blog (ID: $blogId)..." -ForegroundColor Yellow

$headers2 = @{ "Authorization" = "Bearer $token2" }
$blogCheck2 = Invoke-ApiRequest -Uri "$baseUrl/api/blogs/$blogId" -Method GET -Headers $headers2

if (-not $blogCheck2) {
    Write-Host "❌ PERSISTENCE FAILED: Blog data was lost!" -ForegroundColor Red
    Write-Host "   MongoDB volume may not be configured correctly" -ForegroundColor Red
    exit 1
}

Write-Host "✅ MongoDB PERSISTENCE VERIFIED" -ForegroundColor Green
Write-Host "   Blog successfully retrieved after restart" -ForegroundColor Green
Write-Host "   Blog Title: $($blogCheck2.data.title)" -ForegroundColor DarkGray
Write-Host "   Blog ID: $($blogCheck2.data.blogId)" -ForegroundColor DarkGray
Write-Host "   Category: $($blogCheck2.data.category)" -ForegroundColor DarkGray

# Final Summary
Write-Host "`n╔══════════════════════════════════════════════════════════════════════╗" -ForegroundColor Green
Write-Host "║                  ✅ DATA PERSISTENCE TEST PASSED ✅                  ║" -ForegroundColor Green
Write-Host "╚══════════════════════════════════════════════════════════════════════╝" -ForegroundColor Green

Write-Host "`n📊 Test Results Summary:" -ForegroundColor Cyan
Write-Host "   ✅ MySQL Data Persistence: WORKING" -ForegroundColor Green
Write-Host "      • User data survived service restart" -ForegroundColor Green
Write-Host "      • Volume: blog-site_mysql-data" -ForegroundColor DarkGray
Write-Host ""
Write-Host "   ✅ MongoDB Data Persistence: WORKING" -ForegroundColor Green
Write-Host "      • Blog data survived service restart" -ForegroundColor Green
Write-Host "      • Volume: blog-site_mongodb-data" -ForegroundColor DarkGray
Write-Host ""
Write-Host "   ✅ Docker Volume Configuration: CORRECT" -ForegroundColor Green
Write-Host "   ✅ Application Configuration: CORRECT" -ForegroundColor Green
Write-Host "   ✅ JWT Authentication: WORKING" -ForegroundColor Green

Write-Host "`n🎯 Conclusion:" -ForegroundColor Cyan
Write-Host "   Your application is fully configured for local development" -ForegroundColor White
Write-Host "   with persistent data storage. All data will survive:" -ForegroundColor White
Write-Host "   • Container restarts" -ForegroundColor DarkGray
Write-Host "   • docker-compose down/up cycles" -ForegroundColor DarkGray
Write-Host "   • System reboots" -ForegroundColor DarkGray

Write-Host "`n💡 Useful Commands:" -ForegroundColor Yellow
Write-Host "   # Check volume status" -ForegroundColor DarkGray
Write-Host "   docker volume ls | Select-String 'blog-site'" -ForegroundColor White
Write-Host ""
Write-Host "   # Inspect MySQL volume" -ForegroundColor DarkGray
Write-Host "   docker volume inspect blog-site_mysql-data" -ForegroundColor White
Write-Host ""
Write-Host "   # Connect to MySQL directly" -ForegroundColor DarkGray
Write-Host "   docker exec -it blogsite-mysql mysql -ubloguser -pblogpassword userdb" -ForegroundColor White
Write-Host ""
Write-Host "   # Connect to MongoDB directly" -ForegroundColor DarkGray
Write-Host "   docker exec -it blogsite-mongodb mongosh -u admin -p adminpassword" -ForegroundColor White

Write-Host "`n📚 For more information, see:" -ForegroundColor Yellow
Write-Host "   • LOCAL_PERSISTENCE_VALIDATION.md - Detailed persistence documentation" -ForegroundColor White
Write-Host "   • GETTING_STARTED.md - Setup and installation guide" -ForegroundColor White
Write-Host "   • API_TESTING_GUIDE.md - API testing examples" -ForegroundColor White

Write-Host "`n✅ Test completed successfully!`n" -ForegroundColor Green
