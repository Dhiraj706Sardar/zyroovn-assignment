# Finance Backend API

A RESTful API for managing financial records with role-based access control, built with Java 17 and Spring Boot 3.

## Overview

This backend system provides comprehensive financial data management with three user roles (Viewer, Analyst, Admin), JWT-based authentication, and dashboard analytics. The application demonstrates clean architecture, proper data modeling, comprehensive validation, and role-based access control.

## Features

- **User Management**: Create and manage users with different roles and statuses
- **Role-Based Access Control**: Three roles with distinct permissions (VIEWER, ANALYST, ADMIN)
- **Financial Record Management**: CRUD operations for income and expense tracking
- **Advanced Filtering**: Filter records by type, category, and date range with pagination
- **Dashboard Analytics**: Summary statistics, category breakdowns, and monthly trends
- **JWT Authentication**: Stateless authentication with secure token-based access
- **API Documentation**: Interactive Swagger UI for API exploration
- **Soft Delete**: Preserve data integrity with soft delete functionality
- **Comprehensive Validation**: Input validation with detailed error messages

## Technology Stack

- **Java 17**: LTS version with modern language features
- **Spring Boot 3.2.4**: Production-ready application framework
- **Spring Security**: Authentication and authorization
- **Spring Data JPA**: Data persistence abstraction
- **H2 Database**: In-memory database for development
- **JWT (jjwt 0.12.5)**: Token-based authentication
- **Lombok**: Reduce boilerplate code
- **SpringDoc OpenAPI**: API documentation
- **Maven**: Build and dependency management

## Prerequisites

- Java 17 or higher
- Maven 3.8+ or higher
- Git (for cloning the repository)

## Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/Dhiraj706Sardar/zyroovn-assignment.git

```

### 2. Build the Project

```bash
mvn clean install
```

### 3. Run the Application

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

### 4. Access the API Documentation

Open your browser and navigate to:
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- H2 Console: `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:financedb`)

## Default Test Users

The application comes with three pre-configured test users:

| Username | Password | Role | Email |
|----------|----------|------|-------|
| viewer_user | password123 | VIEWER | viewer@example.com |
| analyst_user | password123 | ANALYST | analyst@example.com |
| admin_user | password123 | ADMIN | admin@example.com |

## API Endpoints

### Authentication

#### Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin_user",
    "password": "password123"
  }'
```

Response:
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "userId": 3,
  "username": "admin_user",
  "email": "admin@example.com",
  "role": "ADMIN"
}
```

#### Register
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "newuser",
    "email": "newuser@example.com",
    "password": "password123",
    "fullName": "New User",
    "role": "VIEWER"
  }'
```

### User Management (Admin Only)

#### Create User
```bash
curl -X POST http://localhost:8080/api/users \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "password123",
    "fullName": "Test User",
    "role": "ANALYST"
  }'
```

#### Get All Users
```bash
curl -X GET http://localhost:8080/api/users \
  -H "Authorization: Bearer <token>"
```

#### Update User
```bash
curl -X PUT http://localhost:8080/api/users/1 \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "updated@example.com",
    "fullName": "Updated Name",
    "role": "ADMIN"
  }'
```

#### Delete User
```bash
curl -X DELETE http://localhost:8080/api/users/1 \
  -H "Authorization: Bearer <token>"
```

### Financial Records

#### Create Record (Admin Only)
```bash
curl -X POST http://localhost:8080/api/records \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 1500.00,
    "type": "INCOME",
    "category": "Salary",
    "transactionDate": "2026-04-03",
    "description": "Monthly salary",
    "notes": "April payment"
  }'
```

#### Get Records with Filters
```bash
curl -X GET "http://localhost:8080/api/records?type=INCOME&startDate=2026-03-01&endDate=2026-04-30&page=0&size=10" \
  -H "Authorization: Bearer <token>"
```

#### Update Record (Admin Only)
```bash
curl -X PUT http://localhost:8080/api/records/1 \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 2000.00,
    "category": "Bonus"
  }'
```

#### Delete Record (Admin Only)
```bash
curl -X DELETE http://localhost:8080/api/records/1 \
  -H "Authorization: Bearer <token>"
```

### Dashboard Analytics

#### Get Summary
```bash
curl -X GET "http://localhost:8080/api/dashboard/summary?startDate=2026-03-01&endDate=2026-04-30" \
  -H "Authorization: Bearer <token>"
```

Response:
```json
{
  "totalIncome": 11200.00,
  "totalExpense": 5395.00,
  "netBalance": 5805.00,
  "categoryTotals": {
    "Salary": 10000.00,
    "Rent": 3000.00,
    "Groceries": 850.00
  }
}
```

#### Get Category Totals
```bash
curl -X GET http://localhost:8080/api/dashboard/category-totals \
  -H "Authorization: Bearer <token>"
```

#### Get Recent Activity
```bash
curl -X GET "http://localhost:8080/api/dashboard/recent-activity?limit=5" \
  -H "Authorization: Bearer <token>"
```

#### Get Monthly Trends (Analyst/Admin Only)
```bash
curl -X GET "http://localhost:8080/api/dashboard/monthly-trends?months=6" \
  -H "Authorization: Bearer <token>"
```

## Role-Based Access Control

The system implements strict role-based access control with three distinct roles:

### VIEWER
- ✅ View dashboard summaries
- ✅ View category totals
- ✅ View recent activity
- ❌ NO access to detailed financial records
- ❌ NO access to insights/trends

### ANALYST
- ✅ All VIEWER permissions (dashboard summaries)
- ✅ View detailed financial records with filters
- ✅ View individual record details
- ✅ Access insights and monthly trends
- ❌ NO create/update/delete operations
- ❌ NO user management access

### ADMIN
- ✅ All ANALYST permissions (view records and insights)
- ✅ Create, update, delete financial records
- ✅ Full user management (create, update, delete, deactivate users)

For detailed information about role permissions and access control implementation, see [ROLE_BASED_ACCESS_CONTROL.md](ROLE_BASED_ACCESS_CONTROL.md)

## Configuration

### Environment Variables

You can override default configuration using environment variables:

```bash
export JWT_SECRET=your-secret-key-minimum-256-bits
export SPRING_PROFILES_ACTIVE=dev
```

### Application Profiles

- **dev**: Development profile with H2 database and debug logging
- **prod**: Production profile (configure PostgreSQL/MySQL in application-prod.yml)

## Testing

### Run All Tests
```bash
mvn test
```

### Run Specific Test Class
```bash
mvn test -Dtest=UserServiceTest
```

### Generate Test Coverage Report
```bash
mvn clean test jacoco:report
```

## Project Structure

```
finance-backend/
├── src/
│   ├── main/
│   │   ├── java/com/finance/backend/
│   │   │   ├── config/          # Configuration classes
│   │   │   ├── controller/      # REST controllers
│   │   │   ├── dto/             # Data Transfer Objects
│   │   │   ├── entity/          # JPA entities
│   │   │   ├── enums/           # Enumerations
│   │   │   ├── exception/       # Custom exceptions
│   │   │   ├── repository/      # JPA repositories
│   │   │   ├── security/        # Security components
│   │   │   └── service/         # Business logic
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       └── data.sql
│   └── test/                    # Test classes
├── pom.xml
└── README.md
```

## Design Decisions

### 1. JWT Authentication
- **Decision**: Use JWT for stateless authentication
- **Rationale**: Scalable, works well with microservices, no server-side session storage

### 2. Soft Delete
- **Decision**: Implement soft delete for users and financial records
- **Rationale**: Preserves data for audit trails and allows recovery

### 3. H2 Database
- **Decision**: Use H2 for development, support PostgreSQL/MySQL for production
- **Rationale**: Simplifies local development while maintaining production flexibility

### 4. Role-Based Access Control
- **Decision**: Three roles with clear permission boundaries
- **Rationale**: Meets requirements and is simpler than attribute-based access control

### 5. DTO Pattern
- **Decision**: Separate DTOs for requests and responses
- **Rationale**: Decouples API from domain model, enables validation, prevents over-posting

## Assumptions

1. Single-tenant system (no multi-tenancy)
2. Users have one role only
3. Financial records are visible to all authenticated users
4. Categories are free-text (no predefined list)
5. Single currency (no multi-currency support)
6. Time zones handled in UTC
7. No file attachments for receipts
8. No email notifications
9. No audit log beyond soft delete timestamps

## Error Handling

The API returns consistent error responses:

```json
{
  "timestamp": "2026-04-03T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/records",
  "errors": {
    "amount": "must be greater than 0",
    "category": "must not be blank"
  }
}
```

### HTTP Status Codes

- `200 OK`: Successful GET, PUT
- `201 Created`: Successful POST
- `204 No Content`: Successful DELETE
- `400 Bad Request`: Validation errors
- `401 Unauthorized`: Missing/invalid authentication
- `403 Forbidden`: Insufficient permissions
- `404 Not Found`: Resource not found
- `409 Conflict`: Duplicate resource
- `500 Internal Server Error`: Unexpected errors

## Troubleshooting

### Port Already in Use
```bash
# Change port in application.yml or use environment variable
export SERVER_PORT=8081
mvn spring-boot:run
```

### JWT Token Expired
- Tokens expire after 24 hours
- Login again to get a new token

### H2 Console Not Accessible
- Ensure dev profile is active
- Check URL: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:financedb`

## Future Enhancements

- Multi-currency support
- File attachments for receipts
- Email notifications
- Audit logging
- Rate limiting
- Caching for dashboard queries
- Export to CSV/PDF
- Scheduled reports

## License

MIT License

## Contact

For questions or support, contact: support@financebackend.com
