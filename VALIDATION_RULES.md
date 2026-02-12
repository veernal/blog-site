# Validation Rules & Business Logic

This document details all validation rules and business logic implemented in the Blog Site application.

## User Registration Validation

### Field: userName
- ✅ **Required**: Yes
- ✅ **Minimum Length**: 3 characters
- ✅ **Maximum Length**: 50 characters
- ❌ **Cannot be**: Empty, null, whitespace only

**Valid Examples:**
```json
"userName": "John Doe"
"userName": "Alice"
"userName": "Bob Smith Jr."
```

**Invalid Examples:**
```json
"userName": "Jo"              // Too short (< 3)
"userName": ""                // Empty
"userName": "   "             // Whitespace only
```

### Field: userEmail
- ✅ **Required**: Yes
- ✅ **Format**: Must contain "@"
- ✅ **Format**: Must end with ".com"
- ✅ **Must be**: Valid email format
- ❌ **Cannot be**: Duplicate (unique constraint)

**Valid Examples:**
```json
"userEmail": "john.doe@example.com"
"userEmail": "alice@company.com"
"userEmail": "bob.smith@test.com"
```

**Invalid Examples:**
```json
"userEmail": "notanemail.com"        // Missing @
"userEmail": "user@domain.net"       // Doesn't end with .com
"userEmail": "user@.com"             // Invalid format
"userEmail": "existing@user.com"     // Already registered
```

### Field: password
- ✅ **Required**: Yes
- ✅ **Pattern**: Alphanumeric (letters AND numbers)
- ✅ **Minimum Length**: 8 characters
- ❌ **Cannot be**: Only letters or only numbers

**Valid Examples:**
```json
"password": "Password123"
"password": "myPass2023"
"password": "Admin1234"
"password": "Test9876Word"
```

**Invalid Examples:**
```json
"password": "Pass123"         // Too short (< 8)
"password": "OnlyLetters"     // No numbers
"password": "12345678"        // No letters
"password": "Pass@123"        // Special characters not required but allowed
```

---

## Blog Creation Validation

### Field: blogName
- ✅ **Required**: Yes
- ✅ **Minimum Length**: 20 characters
- ❌ **Cannot be**: Duplicate for same user
- ⚠️ **Note**: Must match path parameter in URL

**Valid Examples:**
```json
"blogName": "Understanding Microservices Architecture Patterns"
"blogName": "Complete Guide to Spring Boot Development Practices"
```

**Invalid Examples:**
```json
"blogName": "Short Title"                    // Too short (< 20)
"blogName": ""                               // Empty
"blogName": "Existing Blog Title"            // Duplicate
```

### Field: category
- ✅ **Required**: Yes
- ✅ **Minimum Length**: 20 characters
- ℹ️ **Used for**: Searching and filtering blogs

**Valid Examples:**
```json
"category": "Software Engineering Best Practices"
"category": "Advanced Java Programming Techniques"
"category": "Cloud Computing and DevOps"
```

**Invalid Examples:**
```json
"category": "Tech"                  // Too short (< 20)
"category": ""                      // Empty
```

### Field: article
- ✅ **Required**: Yes
- ✅ **Minimum Length**: 1000 characters (approx. 150-200 words)
- ℹ️ **Note**: Represents the blog content

**Valid Example:**
```json
"article": "Microservices architecture represents a method of developing software applications as a suite of independently deployable, small, modular services. Each service runs a unique process and communicates through a well-defined, lightweight mechanism to serve a business goal. This architectural style has gained significant traction in recent years... (continue for 1000+ characters)"
```

**Invalid Example:**
```json
"article": "Short article."        // Too short (< 1000 characters)
"article": ""                      // Empty
```

### Field: authorName
- ✅ **Required**: Yes
- ℹ️ **Auto-filled**: From JWT token user information

**Valid Examples:**
```json
"authorName": "John Doe"
"authorName": "Alice Smith"
```

**Invalid Example:**
```json
"authorName": ""                   // Empty
```

---

## Business Rules

### User Registration

1. **Email Uniqueness**
   - Each email can only be registered once
   - Case-sensitive check
   - Returns: `409 Conflict` if duplicate

2. **Password Encryption**
   - All passwords encrypted using BCrypt
   - Never stored in plain text
   - Minimum strength: Alphanumeric with 8+ characters

3. **User Activation**
   - New users are active by default (`isActive: true`)
   - Inactive users cannot login

4. **Backup Trigger**
   - Automatic backup triggered when user count ≥ 10,000
   - Runs asynchronously
   - Backup file format: `userdb_backup_YYYYMMDD_HHmmss.sql`

### Blog Management

1. **Blog Ownership**
   - Users can only delete their own blogs
   - User ID validated from JWT token
   - Returns: `404 Not Found` if blog doesn't belong to user

2. **Soft Delete**
   - Blogs are marked as deleted, not physically removed
   - Deleted blogs excluded from search results
   - `deleted: true` flag set

3. **Timestamp Management**
   - `createdAt`: Auto-generated on creation (current server time)
   - `updatedAt`: Auto-updated on any modification
   - Format: `yyyy-MM-dd'T'HH:mm:ss`

4. **Author Details**
   - Author name from request body
   - Author email from JWT token
   - Both included in blog responses

### Blog Search

1. **Category Search**
   - Case-insensitive partial matching
   - Returns all active blogs in category
   - Sorted by creation date (newest first)

2. **Date Range Search**
   - Inclusive range (from date to to date)
   - Date formats supported:
     - `yyyy-MM-dd` (ISO format)
     - `dd-MM-yyyy` (European format)
   - Time: From 00:00:00 to 23:59:59

3. **User Blog List**
   - Returns only user's own blogs
   - Excludes deleted blogs
   - Sorted by creation date (newest first)

### Authentication & Authorization

1. **JWT Token**
   - Expiration: 24 hours
   - Algorithm: HS256
   - Contains: userId, email
   - Format: `Bearer <token>`

2. **Secured Endpoints**
   - POST `/api/v1.0/blogsite/user/blogs/add/{blogname}`
   - DELETE `/api/v1.0/blogsite/user/delete/{blogname}`
   - GET `/api/v1.0/blogsite/user/getall`

3. **Public Endpoints**
   - POST `/api/v1.0/blogsite/user/register`
   - POST `/api/v1.0/blogsite/user/login`
   - GET `/api/v1.0/blogsite/blogs/info/{category}`
   - GET `/api/v1.0/blogsite/blogs/get/{category}/{from}/{to}`

### Rate Limiting

1. **Registration Endpoint**
   - Limit: 10 requests per second
   - Burst: Up to 20 requests
   - Response: `429 Too Many Requests` if exceeded

2. **Blog Creation**
   - Limit: 5 requests per second
   - Burst: Up to 10 requests
   - Per user basis

### CQRS Event Sourcing

1. **Blog Created Event**
   - Published on successful blog creation
   - Contains: Full blog details
   - Consumers: Query Service, Event Store

2. **Blog Deleted Event**
   - Published on blog deletion
   - Contains: Blog ID, user ID
   - Consumers: Query Service, Event Store

3. **Event Store**
   - All events persisted with version number
   - Enables event replay
   - Audit trail for compliance

---

## HTTP Status Codes

### Success Responses

| Code | Meaning | Used When |
|------|---------|-----------|
| 200 | OK | Successful GET, DELETE |
| 201 | Created | Successful POST (user, blog) |

### Client Error Responses

| Code | Meaning | Used When |
|------|---------|-----------|
| 400 | Bad Request | Validation failed, malformed request |
| 401 | Unauthorized | Missing or invalid JWT token |
| 404 | Not Found | Resource doesn't exist |
| 409 | Conflict | Duplicate resource (email, blog name) |
| 429 | Too Many Requests | Rate limit exceeded |

### Server Error Responses

| Code | Meaning | Used When |
|------|---------|-----------|
| 500 | Internal Server Error | Unexpected server error |

---

## Performance Requirements

1. **Blog Search Response Time**
   - Target: < 30 seconds
   - Optimization: MongoDB indexes, Redis caching
   - Cache TTL: 10 minutes

2. **Database Queries**
   - Indexes on: `category`, `userId`, `createdAt`, `deleted`
   - Connection pooling: HikariCP (max 10 connections)

3. **Caching Strategy**
   - Cache: Blog searches by category
   - Cache: Blog searches by category and date range
   - No cache: User-specific data

---

## Sample Test Scenarios

### Positive Scenarios ✅

1. Register user with valid data
2. Login with correct credentials
3. Add blog with all valid fields
4. Search blogs by existing category
5. Get user's own blogs
6. Delete user's own blog
7. Search blogs within date range

### Negative Scenarios ❌

1. Register with existing email → 409
2. Register with invalid email → 400
3. Register with short password → 400
4. Login with wrong password → 404
5. Add blog without authentication → 401
6. Add blog with short name → 400
7. Add blog with short article → 400
8. Delete non-existent blog → 404
9. Delete another user's blog → 404
10. Search non-existent category → 404
11. Invalid date format → 400

---

## Validation Error Response Format

```json
{
  "success": false,
  "message": "Validation failed",
  "data": {
    "fieldName1": "Error message 1",
    "fieldName2": "Error message 2"
  },
  "timestamp": "2026-01-29T10:30:00"
}
```

**Example:**
```json
{
  "success": false,
  "message": "Validation failed",
  "data": {
    "password": "Password must be alphanumeric and at least 8 characters",
    "userEmail": "Email must contain '@' and end with '.com'"
  },
  "timestamp": "2026-01-29T10:30:00"
}
```

---

## Testing Checklist

### User Registration
- [ ] Valid registration succeeds
- [ ] Duplicate email rejected
- [ ] Short username rejected
- [ ] Invalid email format rejected
- [ ] Short password rejected
- [ ] Only letters password rejected
- [ ] Only numbers password rejected
- [ ] Password encrypted in database
- [ ] Backup triggered at 10,000 users

### Blog Creation
- [ ] Valid blog created
- [ ] Short blog name rejected
- [ ] Short category rejected
- [ ] Short article rejected
- [ ] Duplicate blog name rejected
- [ ] Missing authentication rejected
- [ ] Event published to Kafka
- [ ] Timestamp auto-generated

### Blog Search
- [ ] Search by category returns results
- [ ] Case-insensitive search works
- [ ] Non-existent category returns 404
- [ ] Date range search works
- [ ] Invalid date format rejected
- [ ] Results sorted by date (newest first)

### Blog Deletion
- [ ] User can delete own blog
- [ ] Cannot delete other's blog
- [ ] Cannot delete non-existent blog
- [ ] Missing authentication rejected
- [ ] Soft delete applied
- [ ] Event published to Kafka

---

This document serves as a complete reference for all validation rules, business logic, and testing scenarios in the Blog Site application.
