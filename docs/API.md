# API Documentation

## Base URLs

**LocalStack:** `http://lb-{id}.elb.localhost.localstack.cloud`  
**Local:** `http://localhost:4004`

## Authentication

### Login

```bash
POST /auth/login
Content-Type: application/json

{
  "email": "testuser@test.com",
  "password": "password123"
}
```

**Response:**

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

### Validate Token

```bash
GET /auth/validate
Authorization: Bearer <jwt-token>
```

## Patient Management

### Create Patient

```bash
POST /api/patients
Authorization: Bearer <jwt-token>
Content-Type: application/json

{
  "name": "John Doe",
  "email": "john.doe@example.com",
  "phone": "123-456-7890"
}
```

### Get All Patients

```bash
GET /api/patients
Authorization: Bearer <jwt-token>
```

### Get Patient by ID

```bash
GET /api/patients/{id}
Authorization: Bearer <jwt-token>
```

## Swagger Documentation

- Auth Service: `/api-docs/auth`
- Patient Service: `/api-docs/patients`
