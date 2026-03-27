# Banking Customer 360 CRM

Spring Boot CRM for a digital bank, payment institution, microfinance bank, SACCO, or a core banking delivery team. The system focuses on customer master data, customer 360 visibility, opportunity-to-project conversion, annual maintenance tracking, and operational follow-up.

## Overview

- Backend: Java 11, Spring Boot 2.7, Spring Web, Spring Security, Bean Validation
- Persistence:
  - MyBatis: `Customer`, `Contact`, `Deal`, `Project`, `Task`, `Annual Maintenance`
  - Spring Data JPA: authentication, activity history, and remaining transitional modules
- Databases: H2, MySQL, PostgreSQL
- Frontend: built-in static CRM workspace with Customer 360 and Annual Maintenance pages
- Build: Maven

## Solution Architecture

The application follows a layered architecture:

1. Presentation layer
   - Static pages under [`src/main/resources/static`](/Users/gary/Downloads/CRM-Personal/src/main/resources/static)
   - Main workspace: [`index.html`](/Users/gary/Downloads/CRM-Personal/src/main/resources/static/index.html)
   - Customer 360 page: [`customer-360.html`](/Users/gary/Downloads/CRM-Personal/src/main/resources/static/customer-360.html)
   - Annual Maintenance page: [`annual-maintenance.html`](/Users/gary/Downloads/CRM-Personal/src/main/resources/static/annual-maintenance.html)

2. API layer
   - REST controllers under [`src/main/java/com/crm/personal/crm`](/Users/gary/Downloads/CRM-Personal/src/main/java/com/crm/personal/crm)
   - JWT-protected APIs under `/api/*`

3. Service layer
   - Business rules for customer, contact, deal, project, task, activity, and annual maintenance
   - Opportunity conversion and maintenance alert logic live here

4. Persistence layer
   - MyBatis mappers for operational CRM modules
   - JPA repositories for auth and some legacy/transitional records

5. Startup migration / seed layer
   - Schema migration runners add missing columns and normalize old data
   - Seed runners create default admin user and demo relationship data

## Functional Modules

### 1. Authentication

- JWT login endpoint: `POST /api/auth/login`
- Default user:
  - username: `admin`
  - password: `admin123`

### 2. Customer Management

- Banking customer master
- Customer types:
  - `COMMERCIAL_BANK`
  - `PAYMENT_INSTITUTION`
  - `CENTRAL_BANK`
  - `MICROFINANCE_BANK`
  - `SACCO`
- Key fields:
  - `name`
  - `customerType`
  - `cifNumber`
  - `segment`
  - `status`
  - `riskLevel`
  - `notes`

### 3. Contact Directory

- Customer-linked contacts and stakeholder management
- Key fields:
  - `firstName`
  - `lastName`
  - `email`
  - `phone`
  - `jobTitle`
  - `customerId`
  - `notes`

### 4. Opportunities

- Revenue pipeline and commercial opportunities
- Key fields:
  - `title`
  - `amount`
  - `stage`
  - `expectedCloseDate`
  - `customerId`
  - `notes`
- Stages:
  - `NEW`
  - `QUALIFIED`
  - `PROPOSAL_SENT`
  - `NEGOTIATION`
  - `WON`
  - `LOST`

### 5. Won To Project Conversion

- When an opportunity is marked `WON`, users can convert it into a project
- Conversion creates a project with:
  - `projectName`
  - `market`
  - `licenseAmount`
  - `implementationAmount`
  - `taxRate`
  - `status = SIGNED_CONTRACT`
- The source deal stores:
  - `convertedProjectId`
  - `convertedAt`
- The created project stores:
  - `sourceDealId`
- Converted projects cannot be deleted while the conversion link remains active

### 6. Project Management

- Customer-linked implementation and commercial project tracking
- Key fields:
  - `projectName`
  - `market`
  - `licenseAmount`
  - `implementationAmount`
  - `taxRate`
  - `status`
  - `sourceDealId`
  - `customerId`
- Contract statuses:
  - `SIGNED_CONTRACT`
  - `UNSIGNED_CONTRACT`

### 7. Annual Maintenance

- Project-linked annual maintenance management
- Works from both Customer 360 and Projects
- Key fields:
  - `projectId`
  - `projectName`
  - `market`
  - `maintenanceYear`
  - `amount`
  - `paymentStatus`
  - `renewStatus`
  - `startDate`
  - `endDate`
  - `customerId`
- Payment statuses:
  - `PAID`
  - `NOT_PAID`
- Renew statuses:
  - `RENEWED`
  - `NOT_RENEWED`

### 8. Service Tasks

- Follow-up, fulfilment, and service execution tracking
- Key fields:
  - `title`
  - `description`
  - `status`
  - `priority`
  - `dueDate`
  - `customerId`
  - `dealId`

### 9. Activities

- Relationship and interaction history
- Examples:
  - calls
  - meetings
  - notes
  - follow-up logs

## Frontend Pages

- Main CRM workspace: [http://localhost:8080](http://localhost:8080)
- Customer 360 page: [http://localhost:8080/customer-360.html](http://localhost:8080/customer-360.html)
- Annual Maintenance page: [http://localhost:8080/annual-maintenance.html](http://localhost:8080/annual-maintenance.html)

Main UI patterns:

- Left collapsible navigation
- Module-specific record workspace
- Table-based `Contacts` and `Projects`
- Dedicated `Customer 360`
- Dedicated `Annual Maintenance`
- Modal-based add/edit flows
- JWT login persisted in browser local storage

## Database Design

The project uses MySQL locally by default, with support for H2 and PostgreSQL profiles.

### Core Tables

#### `customers`

- `id`
- `name`
- `customer_type`
- `cif_number`
- `segment`
- `status`
- `risk_level`
- `notes`
- `created_at`
- `updated_at`

#### `contacts`

- `id`
- `first_name`
- `last_name`
- `email`
- `phone`
- `job_title`
- `notes`
- `customer_id`
- `created_at`
- `updated_at`

#### `deals`

- `id`
- `title`
- `amount`
- `stage`
- `expected_close_date`
- `notes`
- `customer_id`
- `converted_project_id`
- `converted_at`
- `created_at`
- `updated_at`

#### `projects`

- `id`
- `project_name`
- `market`
- `amount` as license amount
- `implementation_amount`
- `tax_rate`
- `status`
- `source_deal_id`
- `customer_id`
- `created_at`
- `updated_at`

#### `annual_maintenance`

- `id`
- `project_id`
- `project_name`
- `market`
- `maintenance_year`
- `amount`
- `payment_status`
- `renew_status`
- `start_date`
- `end_date`
- `customer_id`
- `created_at`
- `updated_at`

#### `tasks`

- `id`
- `title`
- `description`
- `status`
- `priority`
- `due_date`
- `customer_id`
- `deal_id`
- `created_at`
- `updated_at`

#### `activities`

- `id`
- `type`
- `subject`
- `details`
- `activity_date`
- `customer_id`
- `contact_id`
- `deal_id`
- `created_by`
- `created_at`

#### `app_users`

- `id`
- `username`
- `password`
- `full_name`
- `role`
- `created_at`

### Important Relationships

- One customer -> many contacts
- One customer -> many deals
- One customer -> many projects
- One customer -> many tasks
- One customer -> many activities
- One deal -> zero or one converted project
- One project -> many annual maintenance records
- One deal -> many tasks
- One contact / one deal can be linked into activities

## Maintenance Alert Logic

Annual Maintenance status is based on effective years only, meaning `startDate <= today`.

Priority order:

1. `Pending Activation`
   - no maintenance year is effective yet
2. `Renewal Warning`
   - an effective year is `NOT_RENEWED`
3. `Payment Warning`
   - effective years are renewed, but one or more are `NOT_PAID`
4. `Active Coverage`
   - effective years are renewed and paid

Additional expiry warnings are applied from the currently effective coverage window, not from future years.

## API Summary

### Authentication

- `POST /api/auth/login`

### Customers

- `GET /api/customers`
- `GET /api/customers/{id}`
- `POST /api/customers`
- `PUT /api/customers/{id}`
- `DELETE /api/customers/{id}`

### Contacts

- `GET /api/contacts`
- `GET /api/contacts?customerId=1`
- `POST /api/contacts`
- `PUT /api/contacts/{id}`
- `DELETE /api/contacts/{id}`

### Opportunities

- `GET /api/deals`
- `GET /api/deals?customerId=1`
- `GET /api/deals/{id}`
- `POST /api/deals`
- `PUT /api/deals/{id}`
- `DELETE /api/deals/{id}`
- `POST /api/deals/{id}/convert-to-project`

### Projects

- `GET /api/projects`
- `GET /api/projects?customerId=1`
- `GET /api/projects/{id}`
- `POST /api/projects`
- `PUT /api/projects/{id}`
- `DELETE /api/projects/{id}`

### Tasks

- `GET /api/tasks`
- `POST /api/tasks`
- `PUT /api/tasks/{id}`
- `DELETE /api/tasks/{id}`

### Activities

- `GET /api/activities`
- `POST /api/activities`
- `PUT /api/activities/{id}`
- `DELETE /api/activities/{id}`

### Annual Maintenance

- `GET /api/annual-maintenance?customerId=1`
- `GET /api/annual-maintenance?customerId=1&projectId=1`
- `GET /api/annual-maintenance/{id}`
- `POST /api/annual-maintenance`
- `PUT /api/annual-maintenance/{id}`

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

## Local MySQL

Current MySQL profile points to:

- database: `crm_system`
- host: `localhost`
- port: `3306`

See:

- [application-mysql.properties](/Users/gary/Downloads/CRM-Personal/src/main/resources/application-mysql.properties#L1)
- [settings-open-source.xml](/Users/gary/Downloads/CRM-Personal/settings-open-source.xml#L1)

## Seed Data

- Default login user: `admin / admin123`
- Banking customer seed data and relationship seed data are loaded on startup
- Annual maintenance sample records are seeded for the primary demo customer

## Notes

- Static UI text is maintained in English
- Schema migration runners normalize legacy values and add missing columns at startup
- The project currently mixes MyBatis and JPA by design while preserving module stability
