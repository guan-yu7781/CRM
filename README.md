# CRM System

Starter Spring Boot project for building a CRM system.

## Stack

- Java 11
- Spring Boot 2.7
- Spring Web
- Spring Data JPA
- Bean Validation
- H2 Database
- Maven

## Features Included

- JWT login authentication
- Customer management REST API
- Contact management REST API
- Deal management REST API
- Task management REST API
- Activity/follow-up tracking REST API
- Validation and global error handling
- H2, MySQL, and PostgreSQL configuration profiles

## Run

```bash
mvn -s settings-open-source.xml spring-boot:run
```

The app runs on `http://localhost:8080`.
The built-in CRM web page is available at `http://localhost:8080/`.

If your global Maven config points to a private Nexus, you can use the open-source settings file in this project:

```bash
mvn -s settings-open-source.xml test
```

or

```bash
mvn -s settings-open-source.xml spring-boot:run
```

Default login credentials:

- username: `admin`
- password: `admin123`

Login to get a JWT:

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'
```

Use the returned token in protected requests:

```bash
curl http://localhost:8080/api/customers \
  -H "Authorization: Bearer <your-jwt-token>"
```

Database profiles:

- H2 default: `mvn -s settings-open-source.xml spring-boot:run`
- MySQL: `mvn -s settings-open-source.xml spring-boot:run -Dspring-boot.run.profiles=mysql`
- PostgreSQL: `mvn -s settings-open-source.xml spring-boot:run -Dspring-boot.run.profiles=postgres`

## Example Endpoints

### Create a customer

```bash
curl -X POST http://localhost:8080/api/customers \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Alice Johnson",
    "email": "alice@example.com",
    "phone": "+254700000001",
    "company": "Acme Ltd",
    "status": "LEAD",
    "notes": "Interested in premium support"
  }'
```

### Create a deal

```bash
curl -X POST http://localhost:8080/api/deals \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Annual CRM Subscription",
    "amount": 12000,
    "stage": "NEW",
    "expectedCloseDate": "2026-04-30",
    "notes": "Follow up next week",
    "customerId": 1
  }'
```

### Create a task

```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Call Alice about proposal",
    "description": "Walk through pricing and implementation timeline",
    "status": "OPEN",
    "priority": "HIGH",
    "dueDate": "2026-04-10",
    "customerId": 1,
    "dealId": 1
  }'
```

### Create a contact

```bash
curl -X POST http://localhost:8080/api/contacts \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <your-jwt-token>" \
  -d '{
    "firstName": "Alice",
    "lastName": "Johnson",
    "email": "alice.contact@example.com",
    "phone": "+254700000001",
    "jobTitle": "Procurement Manager",
    "notes": "Primary decision maker",
    "customerId": 1
  }'
```

### Create a follow-up activity

```bash
curl -X POST http://localhost:8080/api/activities \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <your-jwt-token>" \
  -d '{
    "type": "CALL",
    "subject": "Introductory sales call",
    "details": "Discussed needs, timeline, and budget range",
    "activityDate": "2026-03-26T16:00:00",
    "customerId": 1,
    "contactId": 1,
    "dealId": 1
  }'
```

### Useful URLs

- API base: `http://localhost:8080/api`
- H2 Console: `http://localhost:8080/h2-console`
