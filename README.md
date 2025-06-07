# Personal Vault & API Key Management System

A secure, encrypted vault system for managing API keys, tokens, secrets, and environment credentials built with Java Spring Boot.

## Features

### 🔐 Security
- **JWT Authentication** with role-based access control (Admin/User)
- **AES-256 Encryption** for all stored secrets
- **BCrypt Password** hashing with salt
- **Rate limiting** and brute-force protection
- **Comprehensive audit logging** with IP tracking
- **Secure headers** (CORS, HSTS, CSP)

### 🗄️ Vault Management
- Store API keys, tokens, passwords, database URLs, SSH keys, certificates
- **Metadata tagging** with project names, environments, and custom tags
- **Search and filter** by project, type, keyword, or date
- **Encrypted storage** with secure retrieval
- **Access tracking** with last accessed timestamps

### 📊 Audit & Monitoring
- **Complete audit trail** of all user activities
- **IP address tracking** and user agent logging
- **Paginated log retrieval** with filtering capabilities
- **Failed login attempt tracking** with account locking
- **Scheduled reporting** (planned feature)

### 🔄 Export & Integration
- **Export vault data** in CSV and JSON formats
- **RESTful API** with comprehensive OpenAPI/Swagger documentation
- **Bulk operations** for managing multiple secrets
- **Project-based organization** of secrets

## Tech Stack

- **Java 17** with Spring Boot 3.2
- **Spring Security** with JWT authentication
- **Spring Data JPA** with Hibernate
- **PostgreSQL** database
- **Liquibase** for database migrations
- **Docker** containerization
- **Maven** build system
- **JUnit 5 + Mockito** for testing
- **Springdoc OpenAPI** for API documentation

## Quick Start

### Prerequisites
- Docker and Docker Compose
- Java 17+ (for local development)
- Maven 3.6+ (for local development)

### Using Docker (Recommended)

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd vault-management-system
   ```

2. **Start the application**
   ```bash
   docker-compose up -d
   ```

3. **Access the application**
   - API: http://localhost:8080/api
   - Swagger UI: http://localhost:8080/api/swagger-ui.html
   - Health Check: http://localhost:8080/api/actuator/health

### Local Development

1. **Set up PostgreSQL**
   ```bash
   # Start PostgreSQL container only
   docker-compose up vault-db -d
   ```

2. **Configure environment variables**
   ```bash
   export DB_USERNAME=vault_user
   export DB_PASSWORD=vault_password
   export JWT_SECRET=mySecretKey12345678901234567890123456789012
   export ENCRYPTION_KEY=myEncryptionKey1234567890123456
   ```

3. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

## API Usage

### Authentication

1. **Register a new user**
   ```bash
   curl -X POST http://localhost:8080/api/auth/register \
     -H "Content-Type: application/json" \
     -d '{
       "username": "john_doe",
       "email": "john@example.com",
       "password": "SecurePassword123!"
     }'
   ```

2. **Login**
   ```bash
   curl -X POST http://localhost:8080/api/auth/login \
     -H "Content-Type: application/json" \
     -d '{
       "username": "john_doe",
       "password": "SecurePassword123!"
     }'
   ```

### Managing Secrets

1. **Create a secret**
   ```bash
   curl -X POST http://localhost:8080/api/secrets \
     -H "Authorization: Bearer YOUR_JWT_TOKEN" \
     -H "Content-Type: application/json" \
     -d '{
       "name": "GitHub API Key",
       "value": "ghp_xxxxxxxxxxxxxxxxxxxx",
       "type": "API_KEY",
       "projectName": "MyProject",
       "environment": "production",
       "description": "GitHub API key for CI/CD",
       "tags": ["github", "ci-cd"]
     }'
   ```

2. **Retrieve secrets**
   ```bash
   # Get all secrets (metadata only)
   curl -X GET http://localhost:8080/api/secrets \
     -H "Authorization: Bearer YOUR_JWT_TOKEN"
   
   # Get specific secret with value
   curl -X GET "http://localhost:8080/api/secrets/1?includeValue=true" \
     -H "Authorization: Bearer YOUR_JWT_TOKEN"
   
   # Search secrets
   curl -X GET "http://localhost:8080/api/secrets?search=github&projectName=MyProject" \
     -H "Authorization: Bearer YOUR_JWT_TOKEN"
   ```

3. **Update a secret**
   ```bash
   curl -X PUT http://localhost:8080/api/secrets/1 \
     -H "Authorization: Bearer YOUR_JWT_TOKEN" \
     -H "Content-Type: application/json" \
     -d '{
       "name": "Updated GitHub API Key",
       "description": "Updated description"
     }'
   ```

4. **Delete a secret**
   ```bash
   curl -X DELETE http://localhost:8080/api/secrets/1 \
     -H "Authorization: Bearer YOUR_JWT_TOKEN"
   ```

### Audit Logs

```bash
# Get user's audit logs
curl -X GET http://localhost:8080/api/audit/logs \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Get all audit logs (admin only)
curl -X GET "http://localhost:8080/api/admin/audit/logs?action=READ_SECRET" \
  -H "Authorization: Bearer ADMIN_JWT_TOKEN"
```

## Security Considerations

### Encryption
- All secrets are encrypted using AES-256 encryption before storage
- Encryption keys should be managed securely and rotated regularly
- Database backups should be encrypted at rest

### Authentication
- JWT tokens expire after 24 hours by default
- Account lockout after 5 failed login attempts for 30 minutes
- Rate limiting: 60 requests per minute per IP address

### Best Practices
1. **Use strong passwords** for user accounts
2. **Rotate encryption keys** regularly
3. **Monitor audit logs** for suspicious activity
4. **Use HTTPS** in production environments
5. **Regular security updates** for dependencies
6. **Database backups** with encryption
7. **Environment-specific configurations**

## Development

### Running Tests
```bash
# Run all tests
mvn test

# Run with coverage report
mvn test jacoco:report

# View coverage report
open target/site/jacoco/index.html
```

### Building for Production
```bash
# Build JAR file
mvn clean package

# Build Docker image
docker build -t vault-system:latest .
```

### Database Migrations
Database schema is managed using Liquibase. Migration files are located in `src/main/resources/db/changelog/`.

To add a new migration:
1. Create a new changeset in the master changelog file
2. Use descriptive IDs and author information
3. Test migrations on a copy of production data

## Configuration

### Environment Variables
| Variable | Description | Default |
|----------|-------------|---------|
| `DB_USERNAME` | Database username | `vault_user` |
| `DB_PASSWORD` | Database password | `vault_password` |
| `JWT_SECRET` | JWT signing secret | Required |
| `ENCRYPTION_KEY` | AES encryption key | Required |
| `SPRING_PROFILES_ACTIVE` | Active Spring profile | `default` |

### Application Properties
Key configurations can be found in `src/main/resources/application.yml`:
- Database connection settings
- JWT token expiration
- Rate limiting configuration
- Logging levels
- Security headers

## Deployment

### Production Deployment
1. **Secure your environment variables**
2. **Use a reverse proxy** (nginx, Apache) with HTTPS
3. **Configure database backups**
4. **Set up monitoring and alerting**
5. **Regular security updates**

### Docker Production Setup
```bash
# Use environment-specific compose file
docker-compose -f docker-compose.prod.yml up -d
```

## Monitoring & Maintenance

### Health Checks
- Application health: `/api/actuator/health`
- Database connectivity check included
- Custom health indicators for encryption service

### Logging
- Structured logging with JSON format in production
- Log levels configurable per package
- Audit logs stored in database for compliance

### Metrics
Spring Boot Actuator provides metrics endpoints:
- `/api/actuator/metrics`
- JVM metrics, HTTP request metrics
- Custom metrics for secret operations

## Contributing

1. Fork the repository
2. Create a feature branch
3. Write tests for your changes
4. Ensure all tests pass
5. Submit a pull request

## License

This project is licensed under the Open Source License - see the LICENSE file for details.

## Support

For support and questions:
- Create an issue in the GitHub repository
- Check the API documentation at `/api/swagger-ui.html`
- Review the audit logs for troubleshooting

---

**Security Notice**: This system handles sensitive data. Always follow security best practices and keep the system updated with the latest security patches.
