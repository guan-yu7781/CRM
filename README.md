# Banking Customer 360 CRM

Spring Boot CRM for a digital bank, payment institution, microfinance bank, SACCO, or a core banking delivery team. The system focuses on customer master data, customer 360 visibility, opportunity-to-project conversion, annual maintenance tracking, and operational follow-up.

## Overview

- Backend: Java 11, Spring Boot 2.7, Spring Web, Spring Security, Bean Validation
- Persistence:
  - MyBatis: `Customer`, `Contact`, `Deal`, `Project`, `Task`, `Annual Maintenance`
  - Spring Data JPA: authentication, activity history, and remaining transitional modules
- Databases: H2, MySQL, PostgreSQL
- Frontend:
  - Default entry: Vue 3 SPA served by Spring Boot static resources
  - Dev mode: Vite development server under [`frontend`](/Users/gary/Downloads/CRM-Personal/frontend)
- Build: Maven

## Role And Access Control Design

The CRM now includes a first-release role and access control model designed for banking and fintech operating teams.

### Design Intent

The access model follows four principles:

- Role-led access:
  users receive a business role that reflects their operational responsibility
- Permission-led enforcement:
  every major CRUD and conversion action is protected by a named permission
- Business-friendly roles:
  roles are aligned to operating teams rather than technical labels
- Progressive security:
  this release enforces module and action permissions now, while the next phase can extend into record ownership and market-level data scope filtering

### Supported Business Roles

- `SUPER_ADMIN`
  - full system and access-control ownership
- `CRM_ADMIN`
  - manages customer, contact, project, deal, maintenance, task, and activity data
- `SALES_MANAGER`
  - manages team pipeline, won opportunity conversion, and commercial visibility
- `RELATIONSHIP_MANAGER`
  - manages day-to-day customer, contact, opportunity, project, maintenance, task, and activity updates
- `FINANCE_OFFICER`
  - focuses on project financial visibility plus maintenance payment and renewal controls

Legacy roles `ADMIN` and `SALES` are still recognized so older seeded data keeps working. They are normalized to `SUPER_ADMIN` and `RELATIONSHIP_MANAGER`.

### Current Data Scope Model

The current role catalog also carries a scope design:

- `ALL`
- `TEAM`
- `OWN`

This release returns scope metadata in the API and UI so the operating model is visible. Fine-grained record filtering by team ownership and assigned relationship manager is the next implementation step.

### Permission Model

The following permission families are enforced:

- `CUSTOMER_*`
- `CONTACT_*`
- `DEAL_*`
- `PROJECT_*`
- `MAINTENANCE_*`
- `TASK_*`
- `ACTIVITY_*`
- `ACCESS_CONTROL_*`

Examples:

- `DEAL_CONVERT_TO_PROJECT`
- `PROJECT_VIEW_FINANCIALS`
- `MAINTENANCE_UPDATE_PAYMENT`
- `MAINTENANCE_UPDATE_RENEW`
- `ACCESS_CONTROL_MANAGE`

### What Is Implemented In This Release

- JWT login now returns:
  - effective role
  - role label
  - data scope
  - permission list
- all primary CRM APIs are protected with permission checks
- a new `Access Control` module is available in the main workspace for authorized users
- super admins can:
  - view the role catalog
  - list CRM users
  - create CRM users
  - update user roles and passwords
- the main workspace now adjusts visible modules and action buttons based on the signed-in user's permissions

### Access Control API

- `GET /api/auth/me`
- `GET /api/admin/access-control/roles`
- `GET /api/admin/access-control/users`
- `POST /api/admin/access-control/users`
- `PUT /api/admin/access-control/users/{id}`

### Seeded Users

The system seeds these demo accounts if they do not already exist:

- `admin / admin123` -> `SUPER_ADMIN`
- `crmadmin / admin123` -> `CRM_ADMIN`
- `sales.manager / admin123` -> `SALES_MANAGER`
- `rm.gary / admin123` -> `RELATIONSHIP_MANAGER`
- `finance.officer / admin123` -> `FINANCE_OFFICER`

## Solution Architecture

The application follows a layered architecture:

1. Presentation layer
   - Vue 3 SPA under [`frontend`](/Users/gary/Downloads/CRM-Personal/frontend)
   - Main application shell: [`App.vue`](/Users/gary/Downloads/CRM-Personal/frontend/src/App.vue)
   - Workspace: [`WorkspaceView.vue`](/Users/gary/Downloads/CRM-Personal/frontend/src/views/WorkspaceView.vue)
   - Customer 360 page: [`Customer360View.vue`](/Users/gary/Downloads/CRM-Personal/frontend/src/views/Customer360View.vue)
   - Annual Maintenance page: [`MaintenanceView.vue`](/Users/gary/Downloads/CRM-Personal/frontend/src/views/MaintenanceView.vue)
   - Built assets are emitted into [`src/main/resources/static`](/Users/gary/Downloads/CRM-Personal/src/main/resources/static) so Spring Boot serves Vue by default

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

## Frontend Delivery Model

- Vue is now the default frontend entry point
- Spring Boot serves the Vue production build directly from [`src/main/resources/static`](/Users/gary/Downloads/CRM-Personal/src/main/resources/static)
- SPA routes are forwarded to `index.html` by [`SpaForwardController.java`](/Users/gary/Downloads/CRM-Personal/src/main/java/com/crm/personal/crm/shared/SpaForwardController.java#L1)
- Security allows the SPA entry routes and static assets through [`SecurityConfig.java`](/Users/gary/Downloads/CRM-Personal/src/main/java/com/crm/personal/crm/security/SecurityConfig.java#L1)
- Vite build output is configured in [`frontend/vite.config.js`](/Users/gary/Downloads/CRM-Personal/frontend/vite.config.js#L1)

### Vue Routes

- `/login`
- `/app/:module`
- `/customer-360/:customerId`
- `/maintenance`

### Local Frontend Development

- Start backend on `http://localhost:8080`
- Start Vue dev server from [`frontend`](/Users/gary/Downloads/CRM-Personal/frontend)
- Vite proxy forwards `/api/*` to Spring Boot

Commands:

```bash
cd /Users/gary/Downloads/CRM-Personal/frontend
npm install
npm run dev
```

Vue dev URL:

- `http://127.0.0.1:5173`

### Production-Like Local Run

To rebuild the Vue frontend into Spring Boot static resources:

```bash
cd /Users/gary/Downloads/CRM-Personal/frontend
npm run build
```

Then start Spring Boot normally and open:

- `http://localhost:8080`

## Functional Modules

### 1. Authentication

- JWT login endpoint: `POST /api/auth/login`
- Current user endpoint: `GET /api/auth/me`
- Default seeded users:
  - `admin / admin123`
  - `crmadmin / admin123`
  - `sales.manager / admin123`
  - `rm.gary / admin123`
  - `finance.officer / admin123`

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
- Protected by:
  - `CUSTOMER_VIEW`
  - `CUSTOMER_CREATE`
  - `CUSTOMER_EDIT`
  - `CUSTOMER_DELETE`

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
- Protected by:
  - `CONTACT_VIEW`
  - `CONTACT_CREATE`
  - `CONTACT_EDIT`
  - `CONTACT_DELETE`

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
- Protected by:
  - `DEAL_VIEW`
  - `DEAL_CREATE`
  - `DEAL_EDIT`
  - `DEAL_DELETE`
  - `DEAL_CONVERT_TO_PROJECT`

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
- Protected by:
  - `PROJECT_VIEW`
  - `PROJECT_CREATE`
  - `PROJECT_EDIT`
  - `PROJECT_DELETE`
  - `PROJECT_VIEW_FINANCIALS`

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
- Expired end dates are highlighted in red in the UI
- API:
  - `GET /api/annual-maintenance?customerId=1`
  - `GET /api/annual-maintenance/{id}`
  - `POST /api/annual-maintenance`
  - `POST /api/annual-maintenance/batch`
  - `PUT /api/annual-maintenance/{id}`
  - `DELETE /api/annual-maintenance/{id}`
- Protected by:
  - `MAINTENANCE_VIEW`
  - `MAINTENANCE_CREATE`
  - `MAINTENANCE_EDIT`
  - `MAINTENANCE_DELETE`
  - `MAINTENANCE_UPDATE_PAYMENT`
  - `MAINTENANCE_UPDATE_RENEW`

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
- Protected by:
  - `TASK_VIEW`
  - `TASK_CREATE`
  - `TASK_EDIT`
  - `TASK_DELETE`

### 9. Activities

- Relationship and interaction history
- Examples:
  - calls
  - meetings
  - notes
  - follow-up logs
- Protected by:
  - `ACTIVITY_VIEW`
  - `ACTIVITY_CREATE`
  - `ACTIVITY_EDIT`
  - `ACTIVITY_DELETE`

### 10. Access Control

- New workspace module for authorized users
- Purpose:
  - view CRM users
  - review role catalog
  - create users
  - update user roles
- Protected by:
  - `ACCESS_CONTROL_VIEW`
  - `ACCESS_CONTROL_MANAGE`

## Frontend Pages

- Main CRM workspace: [http://localhost:8080](http://localhost:8080)
- Customer 360 page: [http://localhost:8080/customer-360.html](http://localhost:8080/customer-360.html)
- Annual Maintenance page: [http://localhost:8080/annual-maintenance.html](http://localhost:8080/annual-maintenance.html)

## Vue Frontend Upgrade

The repository now also includes a Vue 3 frontend upgrade under [frontend/](/Users/gary/Downloads/CRM-Personal/frontend).

### Frontend Stack

- Vue 3
- Vite
- Pinia
- Vue Router
- Axios

### Frontend Scope In This Upgrade

The Vue app introduces a modern SPA shell for:

- login
- branded navigation shell
- customer management workspace
- contact, project, opportunity, task, and interaction modules
- access control module
- dedicated Customer 360 route
- dedicated Annual Maintenance route

The existing Spring Boot static frontend is still present for backward compatibility during migration.

### Run The Vue Frontend

1. Start the Spring Boot backend on port `8080`
2. In a separate terminal:

```bash
cd frontend
npm install
npm run dev
```

3. Open:

```text
http://localhost:5173
```

Vite proxies `/api/*` requests to the Spring Boot backend on `http://localhost:8080`.

Main UI patterns:

- Left collapsible navigation
- Module-specific record workspace
- Table-based `Contacts` and `Projects`
- Role-aware `Access Control` workspace
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

#### `app_users`

- `id`
- `full_name`
- `username`
- `password`
- `role`
- `created_at`

### Access Control Model Tables

The first release keeps role metadata in application code for fast delivery and simple deployment:

- `UserRole`
  - business role catalog
  - data scope metadata
  - permission bundles
- `UserPermission`
  - system action codes enforced by Spring Security method rules

This means:

- user-role assignment is stored in the database
- role-to-permission mapping is versioned in code
- the next phase can externalize role and permission mapping into relational tables if business administration needs become more dynamic

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
- Demo data is seeded only on a fresh install (no existing contacts). It attaches to the first customer in the database.
- Banking customer seed data and relationship seed data are loaded on startup
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
    "segment": "CORPORATE",
    "status": "ACTIVE",
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

## Notes

- Static UI text is maintained in English
- Schema migration runners normalize legacy values and add missing columns at startup
- The project currently mixes MyBatis and JPA by design while preserving module stability
