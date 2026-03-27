# Banking Customer 360 CRM

Spring Boot CRM for a digital bank or core banking delivery team. The system focuses on customer master data, relationship management, annual maintenance contracts, follow-up execution, and a banking-style operator UI.

## Architecture

- Backend: Java 11, Spring Boot 2.7, Spring Web, Spring Security, Bean Validation
- Persistence:
  - MyBatis: `Customer`, `Contact`, `Deal`, `Task`, `Annual Maintenance`
  - Spring Data JPA: authentication, activity history, and transitional modules
- Databases: H2, MySQL, PostgreSQL
- Frontend: built-in static Customer 360 workbench
- Build: Maven

## Modules

### 1. Authentication

- JWT login endpoint: `POST /api/auth/login`
- Default user:
  - username: `admin`
  - password: `admin123`
- Security config allows static pages, while API modules remain protected by JWT

### 2. Customer

- Customer master for banking institutions and regulated clients
- Key fields:
  - `cifNumber`
  - `customerType`
  - `segment`
  - `kycStatus`
  - `riskLevel`
  - `preferredChannel`
  - `onboardingStage`
  - `relationshipManager`
- API:
  - `GET /api/customers`
  - `GET /api/customers/{id}`
  - `POST /api/customers`
  - `PUT /api/customers/{id}`
  - `DELETE /api/customers/{id}`

### 3. Contact

- Customer contact persons, decision makers, and operations counterparts
- Key fields:
  - `firstName`
  - `lastName`
  - `email`
  - `phone`
  - `jobTitle`
  - `customerId`
- API:
  - `GET /api/contacts`
  - `GET /api/contacts?customerId=1`
  - `POST /api/contacts`
  - `PUT /api/contacts/{id}`
  - `DELETE /api/contacts/{id}`

### 4. Deal

- Opportunity and pipeline records linked to a customer
- Key fields:
  - `title`
  - `amount`
  - `stage`
  - `expectedCloseDate`
  - `customerId`
- API:
  - `GET /api/deals`
  - `GET /api/deals?customerId=1`
  - `POST /api/deals`
  - `PUT /api/deals/{id}`
  - `DELETE /api/deals/{id}`

### 5. Task

- Follow-up execution, reminders, internal actions, and owner workload
- Key fields:
  - `title`
  - `description`
  - `status`
  - `priority`
  - `dueDate`
  - `customerId`
  - `dealId`
- API:
  - `GET /api/tasks`
  - `GET /api/tasks?customerId=1`
  - `GET /api/tasks?status=OPEN`
  - `POST /api/tasks`
  - `PUT /api/tasks/{id}`
  - `DELETE /api/tasks/{id}`

### 6. Activity

- Call notes, meetings, follow-up logs, and interaction trail
- API:
  - `GET /api/activities`
  - `GET /api/activities?customerId=1`
  - `POST /api/activities`
  - `PUT /api/activities/{id}`
  - `DELETE /api/activities/{id}`

### 7. Annual Maintenance

- Dedicated maintenance contract page from the Customer Insight area
- Each project is stored as one complete record
- Key fields:
  - `projectName`
  - `market`
  - `maintenanceYear`
  - `amount`
  - `paymentStatus`
  - `startDate`
  - `endDate`
  - `customerId`
- Payment status enum:
  - `PAID`
  - `NOT_PAID`
- Expired end dates are highlighted in red in the UI
- API:
  - `GET /api/annual-maintenance?customerId=1`
  - `GET /api/annual-maintenance/{id}`
  - `POST /api/annual-maintenance`
  - `POST /api/annual-maintenance/batch`
  - `PUT /api/annual-maintenance/{id}`
  - `DELETE /api/annual-maintenance/{id}` (ADMIN only)

## UI Pages

- Customer 360 main page: `http://localhost:8080/`
- Annual Maintenance page: `http://localhost:8080/annual-maintenance.html`

The UI is organized as:

- left navigation for module switching
- right work area for records
- add, edit, and delete actions from buttons
- customer insight panel with deep-link entry to Annual Maintenance

## Security

- JWT authentication required for all API endpoints except login and static pages
- DELETE operations require `ADMIN` role; all other operations require any authenticated user
- JWT secret must be set via environment variable `JWT_SECRET` in production:
  ```bash
  JWT_SECRET=your-strong-secret-here mvn spring-boot:run
  ```
- H2 console (`/h2-console`) is only enabled in the `h2` profile (development)
- MySQL and PostgreSQL profiles use `ddl-auto=validate` — schema must be in place before starting

## Pagination

All list endpoints support optional pagination parameters:

| Parameter | Default | Description |
|-----------|---------|-------------|
| `page` | `0` | Zero-based page index |
| `size` | `500` | Number of records per page |

Example: `GET /api/customers?page=1&size=100`

## Profiles And Run Commands

Use the included open-source Maven settings if your machine has a private Nexus configured globally:

```bash
mvn -s settings-open-source.xml test
```

Default H2 run:

```bash
mvn -s settings-open-source.xml spring-boot:run
```

MySQL run:

```bash
mvn -s settings-open-source.xml spring-boot:run -Dspring-boot.run.profiles=mysql
```

PostgreSQL run:

```bash
mvn -s settings-open-source.xml spring-boot:run -Dspring-boot.run.profiles=postgres
```

## MySQL Notes

Current local MySQL profile points to:

- database: `crm_system`
- host: `localhost`
- port: `3306`

See:

- `src/main/resources/application-mysql.properties`
- `settings-open-source.xml`

## Seed Data

- Default login user: `admin / admin123`
- Demo data is seeded only on a fresh install (no existing contacts). It attaches to the first customer in the database.
- Annual Maintenance sample records are auto-seeded for the first available customer:
  - Project: `NCBA Kenya MSL`, market: `Kenya`
  - Three maintenance years seeded with relative dates (current year ± offset)
  - Payment statuses: `PAID`, `NOT_PAID`

## Quick API Examples

Login:

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

Create a customer:

```bash
curl -X POST http://localhost:8080/api/customers \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "NCBA Kenya",
    "customerType": "COMMERCIAL_BANK",
    "cifNumber": "CIF-100001",
    "email": "ops@ncba.example.com",
    "segment": "CORPORATE",
    "status": "ACTIVE",
    "kycStatus": "VERIFIED",
    "riskLevel": "LOW"
  }'
```

Create annual maintenance:

```bash
curl -X POST http://localhost:8080/api/annual-maintenance \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "projectName": "NCBA Kenya MSL",
    "market": "Kenya",
    "maintenanceYear": 4,
    "amount": 25000,
    "paymentStatus": "PAID",
    "startDate": "2027-03-01",
    "endDate": "2028-02-29",
    "customerId": 1
  }'
```

## Useful URLs

- API base: `http://localhost:8080/api`
- H2 console: `http://localhost:8080/h2-console`
