# API Testing Guide

This document provides examples for testing all REST API endpoints.

## Base URL
```
http://localhost:8080 (API Gateway)
```

## 1. User Registration

### POST /api/v1.0/blogsite/user/register

**Request:**
```json
{
  "userName": "John Doe",
  "userEmail": "john.doe@example.com",
  "password": "Password123"
}
```

**Response:**
```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "userId": "uuid",
    "userName": "John Doe",
    "email": "john.doe@example.com",
    "isActive": true,
    "createdAt": "2026-01-29T10:30:00"
  },
  "timestamp": "2026-01-29T10:30:00"
}
```

## 2. User Login

### POST /api/v1.0/blogsite/user/login

**Request:**
```json
{
  "email": "john.doe@example.com",
  "password": "Password123"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "userId": "uuid",
    "userName": "John Doe",
    "email": "john.doe@example.com",
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  },
  "timestamp": "2026-01-29T10:35:00"
}
```

## 3. Add New Blog (Secured)

### POST /api/v1.0/blogsite/user/blogs/add/{blogname}

**Headers:**
```
Authorization: Bearer <JWT_TOKEN>
```

**Request:**
```json
{
  "blogName": "Understanding Microservices Architecture Patterns",
  "category": "Software Engineering",
  "article": "Microservices architecture has become... (1000+ words)",
  "authorName": "John Doe"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Blog added successfully",
  "data": {
    "blogId": "blog-uuid",
    "blogName": "Understanding Microservices Architecture Patterns",
    "category": "Software Engineering",
    "article": "Microservices architecture has become...",
    "authorName": "John Doe",
    "authorEmail": "john.doe@example.com",
    "createdAt": "2026-01-29T10:40:00"
  },
  "timestamp": "2026-01-29T10:40:00"
}
```

## 4. Delete Blog (Secured)

### DELETE /api/v1.0/blogsite/user/delete/{blogname}

**Headers:**
```
Authorization: Bearer <JWT_TOKEN>
```

**Response:**
```json
{
  "success": true,
  "message": "Blog deleted successfully",
  "data": null,
  "timestamp": "2026-01-29T10:45:00"
}
```

## 5. Get Blogs by Category

### GET /api/v1.0/blogsite/blogs/info/{category}

**Example:** GET /api/v1.0/blogsite/blogs/info/Software%20Engineering

**Response:**
```json
{
  "success": true,
  "message": "Blogs fetched successfully",
  "data": [
    {
      "blogId": "blog-uuid-1",
      "blogName": "Understanding Microservices Architecture Patterns",
      "category": "Software Engineering",
      "article": "Microservices architecture has become...",
      "authorName": "John Doe",
      "authorEmail": "john.doe@example.com",
      "createdAt": "2026-01-29T10:40:00"
    }
  ],
  "timestamp": "2026-01-29T10:50:00"
}
```

## 6. Get All User Blogs (Secured)

### GET /api/v1.0/blogsite/user/getall

**Headers:**
```
Authorization: Bearer <JWT_TOKEN>
```

**Response:**
```json
{
  "success": true,
  "message": "User blogs fetched successfully",
  "data": [
    {
      "blogId": "blog-uuid-1",
      "blogName": "Understanding Microservices Architecture Patterns",
      "category": "Software Engineering",
      "article": "Microservices architecture has become...",
      "authorName": "John Doe",
      "authorEmail": "john.doe@example.com",
      "createdAt": "2026-01-29T10:40:00"
    }
  ],
  "timestamp": "2026-01-29T10:55:00"
}
```

## 7. Get Blogs by Category and Duration

### GET /api/v1.0/blogsite/blogs/get/{category}/{durationFromRange}/{durationToRange}

**Example:** GET /api/v1.0/blogsite/blogs/get/Software%20Engineering/2026-01-01/2026-01-31

**Response:**
```json
{
  "success": true,
  "message": "Blogs fetched successfully",
  "data": [
    {
      "blogId": "blog-uuid-1",
      "blogName": "Understanding Microservices Architecture Patterns",
      "category": "Software Engineering",
      "article": "Microservices architecture has become...",
      "authorName": "John Doe",
      "authorEmail": "john.doe@example.com",
      "createdAt": "2026-01-29T10:40:00"
    }
  ],
  "timestamp": "2026-01-29T11:00:00"
}
```

## Postman Collection

Import the following cURL commands into Postman:

### Register User
```bash
curl -X POST http://localhost:8080/api/v1.0/blogsite/user/register \
  -H "Content-Type: application/json" \
  -d '{
    "userName": "John Doe",
    "userEmail": "john.doe@example.com",
    "password": "Password123"
  }'
```

### Login
```bash
curl -X POST http://localhost:8080/api/v1.0/blogsite/user/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john.doe@example.com",
    "password": "Password123"
  }'
```

### Add Blog (Replace TOKEN with actual JWT)
```bash
curl -X POST "http://localhost:8080/api/v1.0/blogsite/user/blogs/add/Understanding Microservices Architecture Patterns" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer TOKEN" \
  -d '{
    "blogName": "Understanding Microservices Architecture Patterns",
    "category": "Software Engineering Best Practices",
    "article": "Microservices architecture represents a method of developing software applications as a suite of independently deployable, small, modular services. Each service runs a unique process and communicates through a well-defined, lightweight mechanism to serve a business goal. This architectural style has gained significant traction in recent years due to its ability to enable organizations to deliver large, complex applications rapidly, frequently, and reliably... (continue for 1000+ words)",
    "authorName": "John Doe"
  }'
```

### Get Blogs by Category
```bash
curl -X GET "http://localhost:8080/api/v1.0/blogsite/blogs/info/Software%20Engineering"
```

### Get User Blogs (Replace TOKEN with actual JWT)
```bash
curl -X GET http://localhost:8080/api/v1.0/blogsite/user/getall \
  -H "Authorization: Bearer TOKEN"
```

### Delete Blog (Replace TOKEN with actual JWT)
```bash
curl -X DELETE "http://localhost:8080/api/v1.0/blogsite/user/delete/Understanding Microservices Architecture Patterns" \
  -H "Authorization: Bearer TOKEN"
```

## Error Responses

### Validation Error
```json
{
  "success": false,
  "message": "Validation failed",
  "data": {
    "password": "Password must be alphanumeric and at least 8 characters",
    "userEmail": "Email must contain '@' and end with '.com'"
  }
}
```

### Unauthorized
```json
{
  "success": false,
  "message": "Invalid or expired token",
  "timestamp": "2026-01-29T11:05:00"
}
```

### Resource Not Found
```json
{
  "success": false,
  "message": "Blog 'Sample Blog' not found",
  "timestamp": "2026-01-29T11:10:00"
}
```
