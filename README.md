# SilentSignals

# Silent Signals - Emergency Alert System

## Overview

Silent Signals is a comprehensive emergency alert system built with Spring Boot that enables users to send SOS alerts to their emergency contacts through multiple notification channels. The system provides real-time WebSocket notifications, email escalation, and comprehensive alert tracking.

## Features

### Core Functionality

- **User Authentication & Authorization**
  - JWT-based authentication with access and refresh tokens
  - Secure user registration and login
  - Token refresh mechanism

- **Contact Management**
  - Add emergency contacts from registered users
  - View all emergency contacts
  - Delete contacts
  - Prevent self-contact and duplicate contact additions

- **SOS Alert System**
  - Real-time SOS alert triggering with GPS coordinates
  - Automatic notification to all emergency contacts via WebSocket
  - Redis-based session management with 3-minute TTL
  - Alert status tracking (PENDING, TRIGGERED, RESOLVED)
  - Alert resolution by emergency contacts

- **Notification Channels**
  - **WebSocket**: Real-time notifications to online contacts
  - **Email**: Escalation notifications sent after 3 minutes if alert remains unresolved
  - Notification logging for audit trail

- **Alert History**
  - Complete SOS alert history for users
  - Detailed notification logs per alert
  - GPS coordinates and timestamps

## Technology Stack

- **Backend Framework**: Spring Boot
- **Security**: Spring Security with JWT
- **Database**: PostgreSQL (via JPA/Hibernate)
- **Caching**: Redis (for SOS session management)
- **Real-time Communication**: WebSocket (STOMP protocol)
- **Scheduling**: Quartz Scheduler (for alert escalation)
- **Email**: JavaMailSender
- **Build Tool**: Maven/Gradle

## Architecture

### Domain Model

#### User
- Contains user credentials and profile information
- Manages relationships with contacts, refresh tokens, and SOS alerts
- Implements `UserDetails` for Spring Security integration

#### Contact
- Represents emergency contact relationships between users
- Many-to-one relationship with owner and contact user

#### SosAlert
- Stores SOS alert information including GPS coordinates, status, and timestamps
- Links to the triggering user and notification logs

#### NotificationLog
- Tracks all notifications sent for each alert
- Records channel (WEBSOCKET/EMAIL) and timestamp

#### RefreshToken
- Manages JWT refresh tokens
- Tracks token revocation status

## API Endpoints

### Authentication (`/public/*`)

```
POST /public/register
- Register a new user
- Body: RegisterRequest (username, email, password, phoneNumber)
- Response: 200 OK

POST /public/login
- Authenticate user and receive tokens
- Body: LoginRequest (email, password)
- Response: AuthResponse (accessToken, refreshToken)

POST /public/refresh-accessToken
- Refresh access token using refresh token
- Body: RefreshTokenRequest (refreshToken)
- Response: AuthResponse (new accessToken, refreshToken)
```

### Contact Management (`/api/contacts`)

```
POST /api/contacts
- Add a new emergency contact
- Body: ContactRequest (contactEmail)
- Response: 201 Created with ContactResponse
- Authorization: Required

GET /api/contacts
- Retrieve all emergency contacts for authenticated user
- Response: 200 OK with List<ContactResponse>
- Authorization: Required

DELETE /api/contacts/{contactId}
- Remove an emergency contact
- Response: 204 No Content
- Authorization: Required
```

### SOS Alerts (`/api/sos`)

```
POST /api/sos/send
- Trigger an SOS alert
- Body: SosRequest (latitude, longitude)
- Response: 200 OK
- Authorization: Required
- Side Effects:
  - Creates SOS alert in database
  - Stores session in Redis (3-minute TTL)
  - Sends WebSocket notifications to all contacts
  - Schedules email escalation job (3 minutes)

POST /api/sos/respond/{alertId}
- Resolve an SOS alert (for emergency contacts)
- Response: 200 OK
- Authorization: Required
- Side Effects:
  - Updates alert status to RESOLVED
  - Removes session from Redis
  - Cancels scheduled escalation

GET /api/sos/history
- Retrieve SOS alert history for authenticated user
- Response: 200 OK with List<SosHistoryResponse>
- Authorization: Required
```

## Business Logic Flow

### SOS Alert Trigger Flow

1. **User triggers SOS** with GPS coordinates
2. **System creates SOS alert** in database with PENDING status
3. **Redis session created** with 3-minute expiration
4. **WebSocket notifications sent** to all emergency contacts
5. **Notification logs saved** for each contact
6. **Escalation job scheduled** (3 minutes delay)
7. **Previous PENDING alerts** marked as TRIGGERED

### SOS Resolution Flow

1. **Contact responds** to SOS alert
2. **Alert status updated** to RESOLVED
3. **Redis session removed**
4. **Escalation job cancelled** (implicit via status check)

### Email Escalation Flow

1. **Quartz job triggers** after 3 minutes
2. **System checks alert status** in database
3. **If still PENDING**: Send email to all contacts with:
   - Triggering user information
   - GPS coordinates
   - Google Maps link
   - Urgency message
4. **Update notification logs** with EMAIL channel

## Key Components

### Services

#### SosServiceImpl
- Core SOS alert management
- Redis session handling
- WebSocket broadcasting
- Quartz job scheduling
- Contact notification orchestration

#### ContactServiceImpl
- Contact CRUD operations
- Duplicate prevention
- Authorization checks

#### NotificationServiceImpl
- Email notification delivery
- Email content formatting with Google Maps integration

#### AuthService
- User authentication
- Token generation and validation
- User registration

### Configuration

#### Security Configuration
- JWT filter chain
- Public endpoints: `/public/**`
- Protected endpoints: `/api/**`
- WebSocket security configuration

#### WebSocket Configuration
- STOMP endpoint: `/ws`
- Message broker: `/topic`
- SOS destination pattern: `/topic/sos/{userId}`

#### Quartz Configuration
- Job factory configuration
- Trigger scheduling
- Job detail building for escalation tasks

## Data Flow

### WebSocket Communication

```
Client -> /ws (connect)
Server -> /topic/sos/{contactUserId} (broadcast SOS)
Client <- Receives SosWebSocketMessage {
  alertId,
  triggeringUsername,
  latitude,
  longitude,
  timestamp
}
```

### Redis Session Structure

```
Key: "sos:session:{alertId}"
Value: SosSession {
  createdAt,
  triggeringId,
  sosAlertId,
  latitude,
  longitude
}
TTL: 3 minutes
```

## Security Features

- **JWT Authentication**: Secure token-based authentication
- **Authorization Checks**: Endpoint-level security
- **Contact Ownership Validation**: Users can only delete their own contacts
- **Self-contact Prevention**: Users cannot add themselves as contacts
- **Duplicate Prevention**: Cannot add the same contact twice

## Error Handling

- **EntityNotFoundException**: Missing users, contacts, or alerts
- **SecurityException**: Unauthorized contact deletion attempts
- **IllegalArgumentException**: Self-contact addition attempts
- **RuntimeException**: Duplicate contact additions
- **MailException**: Email sending failures (logged but not thrown)

## Logging

The system uses SLF4J logging extensively:
- User actions (registration, login, contact management)
- SOS alert lifecycle events
- Notification delivery status
- Redis operations
- Email sending results
- Error conditions

## Environment Setup

### Required Dependencies

```xml
<dependencies>
    <!-- Spring Boot Starters -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-websocket</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-mail</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-redis</artifactId>
    </dependency>
    
    <!-- Quartz Scheduler -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-quartz</artifactId>
    </dependency>
    
    <!-- Database -->
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
    </dependency>
    
    <!-- JWT -->
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-api</artifactId>
    </dependency>
    
    <!-- Lombok -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
    </dependency>
</dependencies>
```

### Application Configuration

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/silentsignals
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
  
  redis:
    host: localhost
    port: 6379
  
  mail:
    host: smtp.gmail.com
    port: 587
    username: ${MAIL_USERNAME}
    password: ${MAIL_PASSWORD}
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true

jwt:
  secret: ${JWT_SECRET}
  expiration: 86400000  # 24 hours
  refresh-expiration: 604800000  # 7 days
```

## Installation & Running

1. **Clone the repository**
```bash
git clone https://github.com/yourusername/silent-signals.git
cd silent-signals
```

2. **Configure environment variables**
```bash
export DB_USERNAME=your_db_user
export DB_PASSWORD=your_db_password
export MAIL_USERNAME=your_email@gmail.com
export MAIL_PASSWORD=your_app_password
export JWT_SECRET=your_secret_key
```

3. **Start PostgreSQL and Redis**
```bash
# PostgreSQL
docker run -d -p 5432:5432 -e POSTGRES_DB=silentsignals postgres

# Redis
docker run -d -p 6379:6379 redis
```

4. **Build and run the application**
```bash
./mvnw clean install
./mvnw spring-boot:run
```

## Usage Example

### 1. Register Users
```bash
curl -X POST http://localhost:8080/public/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "John Doe",
    "email": "john@example.com",
    "password": "securePassword123",
    "phoneNumber": "+1234567890"
  }'
```

### 2. Login
```bash
curl -X POST http://localhost:8080/public/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "password": "securePassword123"
  }'
```

### 3. Add Emergency Contact
```bash
curl -X POST http://localhost:8080/api/contacts \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "contactEmail": "emergency@example.com"
  }'
```

### 4. Trigger SOS
```bash
curl -X POST http://localhost:8080/api/sos/send \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "latitude": 40.7128,
    "longitude": -74.0060
  }'
```

### 5. Connect to WebSocket (JavaScript)
```javascript
const socket = new SockJS('http://localhost:8080/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({}, function(frame) {
  stompClient.subscribe('/topic/sos/' + userId, function(message) {
    const sosAlert = JSON.parse(message.body);
    console.log('SOS Alert received:', sosAlert);
    // Handle the alert in your UI
  });
});
```
Əla! Mən bunu sənə **README.md** formatında hazırlayacam, `.env.example`-in necə istifadə olunacağını da göstərəcək.

````markdown
# Project Environment Setup

This project requires environment variables to be configured in a `.env` file. For security reasons, sensitive information such as passwords, API keys, and JWT secrets should **never** be committed to version control. Use `.env.example` as a template.

## 1. Create `.env` file

Copy the example file:

```bash
cp .env.example .env
````



## 2. Example `.env` configuration



```env
# Database configuration
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/your_database
SPRING_DATASOURCE_USERNAME=your_username
SPRING_DATASOURCE_PASSWORD=your_password
SPRING_JPA_PROPERTIES_HIBERNATE_DEFAULT_SCHEMA=your_schema

# JWT configuration
JWT_SECRET=your_jwt_secret_key

# CORS configuration
CORS_LINK=http://your_frontend_host:port

# Resend API (email service)
RESEND_API_KEY=your_resend_api_key
RESEND_EMAIL_FROM=your_email@example.com

# Spring Mail configuration
SPRING_MAIL_PASSWORD=your_email_password
```




## Future Enhancements

- SMS notification channel integration
- Push notification support for mobile apps
- Geofencing capabilities
- Alert severity levels
- Multi-language support for email notifications
- Admin dashboard for monitoring
- Alert analytics and reporting
- Voice call escalation
- Emergency services integration

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Contact

For questions or support, please contact: developerx73@gmail.com

---

**Note**: This is an emergency alert system. Ensure proper testing in non-production environments before deploying to production. Always comply with local regulations regarding emergency notification systems.

