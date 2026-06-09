package org.example.construction_material_api.common

/**
 * Domain enums. Constant names are intentionally lower/camelCase so Jackson serializes
 * them as the EXACT tokens the BuildPOS client expects, and so @Enumerated(STRING)
 * persists those same tokens. Do not rename without updating the client contract.
 */

enum class UserRole { admin, cashier, warehouse }

enum class ProductCategory {
    cement, steel, sand, gravel, brick, tile, paint, electrical, plumbing, roofing
}

enum class PaymentMethod { cash, abaKhqr, acleda, wing, creditCard }

enum class StockMovementType { stockIn, stockOut, transfer, adjustment, sale }

enum class SaleStatus { held, completed, refunded }

enum class DeliveryStatus { pending, dispatched, inTransit, delivered, failed }
