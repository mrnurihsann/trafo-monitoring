# PLN Transformer Monitoring Backend

Backend REST API menggunakan Quarkus untuk aplikasi monitoring transformer PLN.

## Features

- **Framework**: Quarkus (Java 17)
- **Database**: PostgreSQL 10+
- **Authentication**: JWT dengan role-based access control
- **WebSocket**: Real-time notifications
- **Database Migration**: Flyway
- **API Documentation**: OpenAPI/Swagger
- **Testing**: JUnit 5 + REST Assured

## Prerequisites

- Java 17+
- Maven 3.8+
- Docker & Docker Compose (untuk database)

## Quick Start

### 1. Clone Repository

```bash
git clone <repository-url>
cd pln-trafo-backend
```

### 2. Start Database

```bash
docker-compose up -d postgres
```

### 3. Run Application

```bash
./mvnw compile quarkus:dev
```

Aplikasi akan berjalan di: http://localhost:8080

## API Documentation

Setelah aplikasi berjalan, akses dokumentasi API di:
- Swagger UI: http://localhost:8080/q/swagger-ui
- OpenAPI Spec: http://localhost:8080/q/openapi

## Default Users

| Username  | Password    | Role      |
|-----------|-------------|-----------|
| admin     | password123 | ADMIN     |
| engineer1 | password123 | ENGINEER  |
| operator1 | password123 | OPERATOR  |
| viewer1   | password123 | VIEWER    |

## Database Schema

### Users Table
- Manajemen user dengan role-based access
- Password di-hash menggunakan BCrypt

### Transformers Table
- Data transformer PLN
- Status: OPERATIONAL, MAINTENANCE, FAULT, OFFLINE
- Priority: LOW, MEDIUM, HIGH, CRITICAL

### Status History Table
- Riwayat perubahan status transformer
- Tracking siapa yang mengubah dan kapan

### Work Orders Table
- Manajemen work order untuk maintenance
- Status: PENDING, IN_PROGRESS, COMPLETED, CANCELLED

### Notifications Table
- Sistem notifikasi real-time
- Type: INFO, WARNING, ERROR, SUCCESS

## API Endpoints

### Authentication
- `POST /auth/login` - User login
- `POST /auth/logout` - User logout
- `POST /auth/refresh` - Refresh token

### Users Management
- `GET /api/users` - Get all users (Admin/Engineer)
- `POST /api/users` - Create user (Admin)
- `PUT /api/users/{id}` - Update user (Admin)
- `DELETE /api/users/{id}` - Delete user (Admin)

### Transformers Management
- `GET /api/transformers` - Get all transformers
- `POST /api/transformers` - Create transformer (Admin/Engineer)
- `PUT /api/transformers/{id}` - Update transformer (Admin/Engineer)
- `DELETE /api/transformers/{id}` - Delete transformer (Admin)
- `PUT /api/transformers/{id}/status` - Update status (Admin/Engineer/Operator)

### Work Orders Management
- `GET /api/work-orders` - Get all work orders
- `POST /api/work-orders` - Create work order
- `PUT /api/work-orders/{id}` - Update work order
- `DELETE /api/work-orders/{id}` - Delete work order

### Notifications
- `GET /api/notifications/user/{userId}` - Get user notifications
- `PUT /api/notifications/{id}/mark-read` - Mark as read
- `POST /api/notifications/broadcast` - Broadcast notification (Admin)

### Dashboard & Reports
- `GET /api/dashboard/stats` - Dashboard statistics
- `GET /api/reports/transformers` - Transformer reports
- `GET /api/reports/work-orders` - Work order reports

### WebSocket
- `/ws/notifications/{userId}` - Real-time notifications

## Development

### Run in Development Mode

```bash
./mvnw compile quarkus:dev
```

Development mode features:
- Live reload
- Dev UI: http://localhost:8080/q/dev/
- Database seeding dengan data sample

### Running Tests

```bash
./mvnw test
```

### Building for Production

```bash
./mvnw package
```

### Docker Build

```bash
./mvnw package
docker build -f Dockerfile -t pln-trafo-backend .
```

## Configuration

### Database Configuration

File: `src/main/resources/application.properties`

```properties
# Database
quarkus.datasource.db-kind=postgresql
quarkus.datasource.username=trafo_user
quarkus.datasource.password=trafo_password
quarkus.datasource.jdbc.url=jdbc:postgresql://localhost:5432/trafo_monitoring

# JWT
mp.jwt.verify.publickey.location=publickey.pem
mp.jwt.verify.issuer=https://pln-trafo.com
```

### Environment Variables

```bash
# Database
QUARKUS_DATASOURCE_JDBC_URL=jdbc:postgresql://localhost:5432/trafo_monitoring
QUARKUS_DATASOURCE_USERNAME=trafo_user
QUARKUS_DATASOURCE_PASSWORD=trafo_password

# CORS
QUARKUS_HTTP_CORS_ORIGINS=http://localhost:3000,http://localhost:5173
```

## Security

### JWT Configuration
- Token expiration: 24 hours
- Refresh token: 7 days
- RSA256 signing algorithm

### Role-based Access Control
- **ADMIN**: Full access
- **ENGINEER**: Manage transformers, work orders
- **OPERATOR**: Update status, manage work orders
- **VIEWER**: Read-only access

## WebSocket Integration

### Client Connection
```javascript
const ws = new WebSocket('ws://localhost:8080/ws/notifications/1');

ws.onopen = () => {
    console.log('Connected to notifications');
};

ws.onmessage = (event) => {
    const notification = JSON.parse(event.data);
    console.log('New notification:', notification);
};
```

### Message Types
- `notification`: New notification
- `system_alert`: System-wide alert
- `connected`: Connection confirmation

## Troubleshooting

### Common Issues

1. **Database Connection Error**
   ```bash
   docker-compose up -d postgres
   # Wait for database to be ready
   ```

2. **JWT Key Issues**
   ```bash
   # Regenerate keys if needed
   openssl genrsa -out src/main/resources/privatekey.pem 2048
   openssl rsa -in src/main/resources/privatekey.pem -pubout -out src/main/resources/publickey.pem
   ```

3. **Port Already in Use**
   ```bash
   # Change port in application.properties
   quarkus.http.port=8081
   ```

## Support

Untuk bantuan teknis atau pertanyaan:
- Email: it-support@pln.co.id
- Documentation: [API Docs](http://localhost:8080/q/swagger-ui)
