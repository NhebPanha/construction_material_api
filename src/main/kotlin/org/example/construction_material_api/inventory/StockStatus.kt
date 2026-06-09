package org.example.construction_material_api.inventory

/**
 * Derived stock status for a product. Never persisted — always computed from the
 * current on-hand quantity and the product's reorder level.
 */
enum class StockStatus {
    OUT_OF_STOCK,
    LOW_STOCK,
    IN_STOCK,
    ;

    companion object {
        fun of(quantityOnHand: Int, reorderLevel: Int): StockStatus = when {
            quantityOnHand <= 0 -> OUT_OF_STOCK
            quantityOnHand <= reorderLevel -> LOW_STOCK
            else -> IN_STOCK
        }
    }
}
