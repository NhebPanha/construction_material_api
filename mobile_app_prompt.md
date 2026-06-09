# Prompt — BuildPOS Mobile App (Flutter)

> Paste everything below the line into your code-generation assistant. It is written to
> match the **already-built** `construction_material_api` backend exactly — do not change
> JSON field names, enum tokens, pagination shape, or the response envelope.

---

Generate a **production-ready Flutter mobile app** called **BuildPOS** — a point-of-sale /
ERP client for a construction-material business. The backend REST API already exists and
must be consumed **exactly** as specified below. Do not invent fields, rename keys, or
change enum values.

## Tech stack & architecture

- Flutter (stable, null-safe Dart 3), Material 3.
- HTTP: **dio** with interceptors.
- State management & architecture: **MVVM** using **Riverpod** (`flutter_riverpod`).
  Layer the code as Model → Repository → ViewModel (Riverpod `Notifier`/`AsyncNotifier`) → View (widgets).
- Models: immutable data classes via **freezed** + **json_serializable**.
- Navigation: **go_router** with auth-gated routes.
- Secure token storage: **flutter_secure_storage**.
- Local light caching of the auth session; everything else is server-driven.

### Folder structure (MVVM, feature-first)

```
lib/
  core/
    network/        dio client, interceptors (auth, refresh, trace), ApiResponse<T>, ApiException
    error/          failure mapping from errorCode
    router/         go_router config + auth guard
    theme/          Material 3 theme
    widgets/        shared widgets (envelope-aware list view, paginated list, error/empty states)
  features/
    auth/      model/  repository/  viewmodel/  view/
    dashboard/ model/  repository/  viewmodel/  view/
    products/  model/  repository/  viewmodel/  view/
    customers/ model/  repository/  viewmodel/  view/
    suppliers/ model/  repository/  viewmodel/  view/
    warehouses/model/  repository/  viewmodel/  view/
    inventory/ model/  repository/  viewmodel/  view/
    sales/     model/  repository/  viewmodel/  view/
    deliveries/model/  repository/  viewmodel/  view/
    reports/   model/  repository/  viewmodel/  view/
  main.dart
```

## Backend connection

- Base URL configurable; default `http://localhost:8080/api/v1` (use `10.0.2.2` for Android emulator).
- Send `Authorization: Bearer <accessToken>` on every request except the auth endpoints.

## Global response conventions (MUST match)

- **Every** response (success or error) is wrapped in this envelope:
  ```json
  { "success": true, "message": "OK", "data": <T|null>, "errorCode": null, "traceId": "uuid" }
  ```
  On error: `success=false`, `data=null`, `errorCode` is a stable UPPER_SNAKE string, with the
  matching HTTP status. Implement a generic `ApiResponse<T>` and unwrap `data`; on `success=false`
  throw an `ApiException(errorCode, message, traceId)`.
- **List endpoints** return this pagination object as `data`:
  ```json
  { "items": [ ... ], "page": 1, "pageSize": 20, "total": 137, "totalPages": 7 }
  ```
  `page` is **1-based**. List query params: `?page=&pageSize=&q=` (`q` = free-text search), plus
  endpoint-specific filters noted below.
- **IDs are strings** in JSON (parse/keep as `String`).
- Money values are JSON numbers with 2 decimals (use `num`/`double`, format with 2 decimals).
- Timestamps are ISO-8601 strings (`createdAt`, `eta`, …) → parse to `DateTime`.
- All keys are camelCase.

## Stable error codes (map to user-facing messages)

`VALIDATION_ERROR` (400), `UNAUTHORIZED` (401), `FORBIDDEN` (403), `NOT_FOUND` (404),
`CONFLICT` (409, e.g. insufficient stock / duplicate / invalid state), `INTERNAL_ERROR` (500).

## Auth (JWT)

- `POST /auth/login` body `{ "email", "password" }` → `data: { accessToken, refreshToken, user }`
- `POST /auth/refresh` body `{ "refreshToken" }` → `data: { accessToken, refreshToken, user }`
- `POST /auth/logout`
- `GET /users/me` → `data: User`

Auth flow:
- Store tokens securely. Add a dio interceptor that attaches the access token.
- On `401`, attempt **one** silent `/auth/refresh` using the stored refresh token, then retry the
  original request; if refresh fails, clear the session and route to Login.
- Optionally forward/generate an `X-Trace-Id` header.

Seeded test users (backend default):
| email | password | role |
|---|---|---|
| admin@buildpos.local | admin123 | admin |
| cashier@buildpos.local | cashier123 | cashier |
| warehouse@buildpos.local | warehouse123 | warehouse |

## Enums (serialize/deserialize as these EXACT tokens)

- `UserRole`: `admin`, `cashier`, `warehouse`
- `ProductCategory`: `cement`, `steel`, `sand`, `gravel`, `brick`, `tile`, `paint`, `electrical`, `plumbing`, `roofing`
- `PaymentMethod`: `cash`, `abaKhqr`, `acleda`, `wing`, `creditCard`
- `StockMovementType`: `stockIn`, `stockOut`, `transfer`, `adjustment`, `sale`
- `SaleStatus`: `held`, `completed`, `refunded`
- `DeliveryStatus`: `pending`, `dispatched`, `inTransit`, `delivered`, `failed`
- `StockStatus` (derived, read-only on Product): `inStock`, `lowStock`, `outOfStock`

## Domain models (exact JSON shapes)

```
User      { id, name, email, role, avatarUrl? }
Product   { id, name, category, costPrice, sellingPrice, stockQuantity, lowStockThreshold,
            unit, description?, imageUrl?, barcode?, stockStatus }
Customer  { id, name, phone?, email?, address?, loyaltyPoints, balance, discountRate }
Supplier  { id, name, phone?, email?, address?, payable }
Warehouse { id, code, name, location?, capacity, used, incoming, outgoing, isPrimary }
Stock     { productId, productName, stockQuantity, lowStockThreshold, stockStatus, unit }
Movement  { id, productId, productName, type, quantity, note?, createdAt }
SaleLine  { productId, productName, quantity, unitPrice, lineDiscount }
Sale      { id, invoiceNumber, createdAt, lines:[SaleLine], discount, taxRate, paymentMethod,
            amountReceived, status, cashierName, customerId?, customerName?,
            subtotal, tax, grandTotal, change }
Delivery  { id, reference, customerName, address, driverName?, vehicle?, status, createdAt,
            eta?, itemsSummary?, note? }
```

## Endpoints

### Products  `/products`
- `GET /products?q=&category=&lowStock=&page=&pageSize=` → paginated `Product`
- `GET /products/{id}` · `POST /products` · `PUT /products/{id}` · `DELETE /products/{id}`
- Create/update body: `{ name, category, costPrice, sellingPrice, stockQuantity, lowStockThreshold, unit, description?, imageUrl?, barcode? }`
- Writes require role `admin` or `warehouse` (delete: `admin`).

### Customers `/customers`, Suppliers `/suppliers`, Warehouses `/warehouses`
- Standard CRUD (`GET` list with `?q=&page=&pageSize=`, `GET/{id}`, `POST`, `PUT/{id}`, `DELETE/{id}`).
- Customer body: `{ name, phone?, email?, address?, loyaltyPoints, balance, discountRate }`
- Supplier body: `{ name, phone?, email?, address?, payable }`
- Warehouse body: `{ code, name, location?, capacity, used, incoming, outgoing, isPrimary }`

### Inventory `/inventory`
- `GET /inventory/stocks?q=&page=&pageSize=` → paginated `Stock`
- `GET /inventory/movements?productId=&page=&pageSize=` → paginated `Movement`
- `POST /inventory/movements` body `{ productId, type, quantity, note? }` — adjusts stock atomically.
  Direction by type: `stockIn` +, `stockOut`/`sale` −, `adjustment` signed, `transfer` no net change.
  Never allows negative stock (server returns `409 CONFLICT`).
- `GET /inventory/dashboard` → `{ totalProducts, lowStockCount, outOfStockCount, totalStockUnits, stockCostValue, stockRetailValue }`

### Sales `/sales-orders`
- `GET /sales-orders?status=&q=&page=&pageSize=` (`q` matches invoiceNumber or customerName)
- `GET /sales-orders/{id}`
- `POST /sales-orders` → **complete a sale** (deducts stock transactionally, generates `invoiceNumber`)
- `POST /sales-orders/hold` → park a draft (`status=held`, no stock deduction)
- `POST /sales-orders/{id}/refund` → `status=refunded`, restocks
- Create/hold body (the client sends quantities only — **unit prices and all totals are server-authoritative**):
  ```json
  {
    "customerId": "1",                // optional
    "lines": [ { "productId": "1", "quantity": 10, "lineDiscount": 0 } ],
    "discount": 5.00,
    "taxRate": 0.10,                  // fraction, e.g. 0.10 = 10%
    "paymentMethod": "abaKhqr",
    "amountReceived": 200.00
  }
  ```
- The response `Sale` includes server-computed `subtotal`, `tax`, `grandTotal`, `change` — display these,
  never compute totals on the client.

### Deliveries `/deliveries`
- `GET /deliveries?status=&page=&pageSize=` · `GET /deliveries/{id}`
- `POST /deliveries` body `{ customerName, address, driverName?, vehicle?, eta?, itemsSummary?, note? }`
- `PATCH /deliveries/{id}/status` body `{ "status" }`

### Reports `/reports`
- `GET /reports?range=daily|weekly|monthly|yearly` → `{ range, revenue, cost, profit, orders, topProducts:[{ name, quantitySold, revenue }] }`
- `GET /reports/dashboard` → `{ todaysSales, totalOrders, totalRevenue, lowStockCount, weeklySales:[{ label, total }], recentSales:[Sale], lowStockProducts:[Product] }`

## Screens / features

1. **Login** — email + password; friendly errors from `errorCode`.
2. **Home / Dashboard** — KPI cards from `/reports/dashboard`, a weekly-sales bar chart, recent sales, low-stock list.
3. **Products** — searchable + paginated list with category filter and a low-stock toggle; `stockStatus` badge (green/amber/red); detail + create/edit (role-gated).
4. **POS / New Sale** — product picker (search/scan barcode), cart with per-line quantity & line discount, order discount, tax rate, payment method picker, amount received → shows server-returned totals & change on the receipt. Buttons: **Complete** and **Hold**.
5. **Holds** — list of `status=held` sales; resume or refund.
6. **Sales history** — filter by status, search; sale detail / receipt; refund action.
7. **Customers / Suppliers / Warehouses** — list + CRUD.
8. **Inventory** — stock levels list, movement history, "post movement" form, inventory KPI cards.
9. **Deliveries** — list with status filter, create, update status (stepper: pending → dispatched → inTransit → delivered / failed).
10. **Reports** — range selector (daily/weekly/monthly/yearly) with revenue/cost/profit/orders and top products.
11. **Profile** — current user, role, logout.

## Behavior & quality requirements

- **Role-based UI**: hide/disable actions the user's role can't perform (admin = everything; cashier = sales/customers; warehouse = products/inventory/suppliers/deliveries). The server enforces this too; handle `403` gracefully.
- Reusable paginated list widget that consumes the `{ items, page, pageSize, total, totalPages }` shape with infinite scroll or pager.
- Centralized error handling that surfaces `message` and maps `errorCode` (e.g. `CONFLICT` on a sale → "Not enough stock").
- Loading / empty / error states everywhere; pull-to-refresh on lists.
- Form validation mirroring server validation (required fields, non-negative numbers).
- Money formatting (2 decimals, currency symbol configurable), date formatting from ISO-8601.
- Theming with Material 3, light/dark.

## Deliverables

- A complete, compiling Flutter project with the structure above.
- `dio` client + interceptors (auth attach, 401-refresh-retry, trace id, error → `ApiException`).
- freezed/json_serializable models for every shape above with the exact field names & enum tokens.
- Repositories per feature, Riverpod ViewModels, and the listed screens wired end-to-end.
- A short `README.md` with setup (`flutter pub get`, `build_runner`), the base-URL config, and the seeded test logins.
- Widget/unit test for the auth flow and the POS "complete sale" happy path (mock dio).
```
