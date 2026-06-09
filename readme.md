# Construction Material API (BuildPOS backend)

A Kotlin + Spring Boot backend implementing the BuildPOS POS/ERP contract: auth (JWT),
products, customers, suppliers, warehouses, inventory, sales (with holds & refunds),
deliveries, and reports. Every response uses the standardized envelope, list endpoints are
paginated and searchable, money values are server-authoritative, and stock can never go
negative.

> The full client contract this service implements is preserved verbatim below
> ("Generate a production-ready REST API backend…"). This top section documents the
> delivered implementation and how to run it.

## Tech stack (as built)

- Kotlin 2.2 / Java 17, Spring Boot 4.0
- Spring Web MVC, Spring Data JPA (Hibernate), Spring Security + JWT (jjwt), Bean Validation
- PostgreSQL with Flyway migrations (`src/main/resources/db/migration/V1__init.sql`)
- Layering: Controller → Service → Repository → Entity, with DTOs (entities are never exposed)

Notable deviations from the original spec, all verified working: Spring Boot **4.0** (the
Gradle scaffold was already on 4.x) rather than 3.x; the build uses Groovy `build.gradle`
rather than Kotlin DSL. The API contract (JSON shapes, field names, enum tokens, endpoints,
business rules) is implemented exactly.

## Prerequisites

- Java 17+ (a 17 toolchain is configured; the bundled Gradle wrapper is used)
- A PostgreSQL database

## Database

The app expects a database named `construction_material`. Connection settings default to
`localhost:5432`, user/password `postgres`/`postgres`, and can be overridden with the
`DB_URL`, `DB_USERNAME`, `DB_PASSWORD` environment variables.

Quick start with Docker:

```bash
docker run -d --name buildpos-pg \
  -e POSTGRES_PASSWORD=postgres -e POSTGRES_USER=postgres -e POSTGRES_DB=construction_material \
  -p 5432:5432 postgres:18-alpine
```

Flyway creates the schema on first start; the app seeds demo data and default users when the
database is empty (disable with `APP_SEED_ENABLED=false`).

## Build & run

```bash
# Windows
.\gradlew.bat bootRun
# *nix
./gradlew bootRun
```

To point at a non-default database (e.g. Postgres on port 5544):

```powershell
$env:DB_URL='jdbc:postgresql://localhost:5544/construction_material'
$env:DB_USERNAME='postgres'; $env:DB_PASSWORD='postgres'
.\gradlew.bat bootRun
```

The service listens on `http://localhost:8080`. Run the tests (in-memory H2, no external DB
needed) with `.\gradlew.bat test`.

## Default users (seeded)

| email                    | password      | role      |
|--------------------------|---------------|-----------|
| admin@buildpos.local     | admin123      | admin     |
| cashier@buildpos.local   | cashier123    | cashier   |
| warehouse@buildpos.local | warehouse123  | warehouse |

## Quick try

```bash
curl -s localhost:8080/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"admin@buildpos.local","password":"admin123"}'
# → { "success": true, "data": { "accessToken": "…", "refreshToken": "…", "user": {…} }, … }
```

Pass `Authorization: Bearer <accessToken>` on every other request. See
[`requests.http`](requests.http) for a ready-to-run collection covering all endpoints.

## Implemented endpoints

- `POST /api/v1/auth/login` · `POST /api/v1/auth/refresh` · `POST /api/v1/auth/logout` · `GET /api/v1/users/me`
- `GET/POST /api/v1/products`, `GET/PUT/DELETE /api/v1/products/{id}` (`?q=&category=&lowStock=&page=&pageSize=`)
- `GET/POST /api/v1/customers`, `…/{id}` · `GET/POST /api/v1/suppliers`, `…/{id}` · `GET/POST /api/v1/warehouses`, `…/{id}`
- `GET /api/v1/inventory/stocks` · `GET/POST /api/v1/inventory/movements` · `GET /api/v1/inventory/dashboard`
- `GET /api/v1/sales-orders` (`?status=&q=`) · `GET /api/v1/sales-orders/{id}` · `POST /api/v1/sales-orders` (complete) · `POST /api/v1/sales-orders/hold` · `POST /api/v1/sales-orders/{id}/refund`
- `GET/POST /api/v1/deliveries` · `GET /api/v1/deliveries/{id}` · `PATCH /api/v1/deliveries/{id}/status`
- `GET /api/v1/reports?range=daily|weekly|monthly|yearly` · `GET /api/v1/reports/dashboard`

---

Generate a production-ready REST API backend in **Kotlin + Spring Boot 3.x** for a
construction-material POS/ERP app called **BuildPOS**. The mobile client is already
built (Flutter + dio) and expects the EXACT contract below — do not deviate from the
JSON shapes, field names, or enum values.

## Tech stack
- Kotlin 2.x, Spring Boot 3.3+, Java 21
- Spring Web (REST), Spring Data JPA, Spring Security + JWT (jjwt), Bean Validation
- PostgreSQL + Flyway migrations
- Gradle (Kotlin DSL)
- Layering: Controller → Service → Repository (JPA) → Entity, with DTOs + MapStruct (or manual mappers). Never expose entities directly.

## Global conventions (MUST match the client exactly)
- Base path: every route is under `/api/` and starts with `v1/...` (e.g. `/api/v1/products`).
- Every successful AND failed response uses this envelope:
  ```json
  { "success": true, "message": "OK", "data": <T|null>, "errorCode": null, "traceId": "uuid" }
  ```
  On error: `success=false`, `data=null`, `errorCode` = a stable UPPER_SNAKE code, `message` = human readable, plus correct HTTP status. Implement with a `@RestControllerAdvice` global exception handler and an `ApiResponse<T>` wrapper returned by every controller.
- List endpoints return a paginated object as `data`:
  ```json
  { "items": [...], "page": 1, "pageSize": 20, "total": 137, "totalPages": 7 }
  ```
  Accept `?page=&pageSize=&q=` query params (q = free-text search). page is 1-based.
- All money fields are JSON numbers (BigDecimal in Kotlin, scale 2). All timestamps are ISO-8601 strings (`createdAt`, `eta`, etc.). IDs are strings in JSON.
- camelCase JSON keys. Enum values are serialized as the exact lowercase/camelCase tokens listed below.

## Auth (JWT)
- `POST v1/auth/login` body `{ "email", "password" }` → `{ "accessToken", "refreshToken", "user": <User> }`
- `POST v1/auth/refresh` body `{ "refreshToken" }` → new token pair
- `POST v1/auth/logout`
- `GET  v1/users/me` → current `<User>`
- Bearer token in `Authorization` header. Roles: `admin`, `cashier`, `warehouse`. Enforce with `@PreAuthorize`.

## Enums (serialize names EXACTLY like this)
- UserRole: `admin`, `cashier`, `warehouse`
- ProductCategory: `cement`, `steel`, `sand`, `gravel`, `brick`, `tile`, `paint`, `electrical`, `plumbing`, `roofing`
- PaymentMethod: `cash`, `abaKhqr`, `acleda`, `wing`, `creditCard`
- StockMovementType: `stockIn`, `stockOut`, `transfer`, `adjustment`, `sale`
- SaleStatus: `held`, `completed`, `refunded`
- DeliveryStatus: `pending`, `dispatched`, `inTransit`, `delivered`, `failed`

## Domain models (JSON shape the client serializes/deserializes)
User:        { id, name, email, role, avatarUrl? }
Product:     { id, name, category, costPrice, sellingPrice, stockQuantity, lowStockThreshold, unit, description, imageUrl?, barcode? }
Customer:    { id, name, phone, email, address, loyaltyPoints, balance, discountRate }
Supplier:    { id, name, phone, email, address, payable }
Warehouse:   { id, code, name, location, capacity, used, incoming, outgoing, isPrimary }
SaleLine:    { productId, productName, quantity, unitPrice, lineDiscount }
Sale (Order):{ id, invoiceNumber, createdAt, lines:[SaleLine], discount, taxRate, paymentMethod, amountReceived, status, cashierName, customerId?, customerName? }
InventoryMovement: { id, productId, productName, type, quantity, note?, createdAt }
Delivery:    { id, reference, customerName, address, driverName, vehicle, status, createdAt, eta?, itemsSummary, note? }

## Endpoints (CRUD = GET list, GET /{id}, POST, PUT /{id}, DELETE /{id})
- `v1/products`      CRUD + `?category=&lowStock=true`
- `v1/customers`     CRUD + search by name/phone
- `v1/suppliers`     CRUD
- `v1/warehouses`    CRUD
- `v1/inventory/stocks`     GET stock levels
- `v1/inventory/movements`  GET list, POST a movement (adjusts product.stockQuantity atomically)
- `v1/inventory/dashboard`  GET aggregated inventory KPIs
- `v1/sales-orders`  the Sale/Order resource:
    - GET list `?status=&q=` (q matches invoiceNumber or customerName)
    - GET /{id}
    - POST            → complete a sale: generate `invoiceNumber` (e.g. INV-100001, monotonic), persist, and DEDUCT stock for each line in one transaction
    - POST /hold      → park a draft (status=held, NO stock deduction)
    - POST /{id}/refund → status=refunded (restock optional)
- `v1/deliveries`    GET list `?status=`, GET /{id}, POST, PATCH /{id}/status body `{ "status" }`
- `v1/reports`       GET `?range=daily|weekly|monthly|yearly` → { revenue, cost, profit, orders, topProducts:[{name, quantitySold, revenue}] }
- `v1/reports/dashboard` → { todaysSales, totalOrders, totalRevenue, lowStockCount, weeklySales:[{label,total}], recentSales:[Sale], lowStockProducts:[Product] }

## Business rules
- Product.stockStatus is DERIVED (don't store): outOfStock if qty<=0, lowStock if qty<=lowStockThreshold, else inStock.
- Sale totals are server-authoritative: subtotal = Σ(unitPrice*qty - lineDiscount); taxable = subtotal - discount; tax = taxable*taxRate; grandTotal = taxable + tax; change = amountReceived - grandTotal.
- Completing a sale and posting a stock movement must be transactional and never let stock go negative.
- Validate all inputs (@Valid); return errorCode `VALIDATION_ERROR` (400), `NOT_FOUND` (404), `UNAUTHORIZED` (401), `FORBIDDEN` (403), `CONFLICT` (409).

## Deliverables
- Full Gradle project structure, `build.gradle.kts`, `application.yml` (dev/prod profiles), Flyway `V1__init.sql` for all tables.
- All entities, DTOs, mappers, repositories, services, controllers.
- JWT security config + role-based authorization.
- Global exception handler producing the envelope above.
- Seed data (Flyway or CommandLineRunner) matching the demo data: a few products across categories, 2-3 customers/suppliers, 3 warehouses, sample sales + deliveries.
- A README with run instructions and a Postman/HTTP collection of example requests.
- Unit tests for the sale-completion stock-deduction flow.
