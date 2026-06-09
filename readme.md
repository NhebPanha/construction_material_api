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
