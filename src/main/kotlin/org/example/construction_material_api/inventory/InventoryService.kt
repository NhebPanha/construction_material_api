package org.example.construction_material_api.inventory

import org.example.construction_material_api.common.InsufficientStockException
import org.example.construction_material_api.common.NotFoundException
import org.example.construction_material_api.product.Product
import org.example.construction_material_api.product.ProductRepository
import org.example.construction_material_api.warehouse.Warehouse
import org.example.construction_material_api.warehouse.WarehouseRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Owns all changes to stock. Every mutation runs in a transaction, takes a pessimistic
 * lock on the stock row, refuses to drive inventory negative and records an immutable
 * [InventoryMovement] for audit.
 */
@Service
class InventoryService(
    private val stockLevelRepository: StockLevelRepository,
    private val movementRepository: InventoryMovementRepository,
    private val productRepository: ProductRepository,
    private val warehouseRepository: WarehouseRepository,
) {

    fun totalOnHand(productId: Long): Int = stockLevelRepository.totalQuantityForProduct(productId)

    fun stockLevelsForProduct(productId: Long): List<StockLevel> =
        stockLevelRepository.findByProductId(productId)

    @Transactional
    fun receive(productId: Long, warehouseId: Long, quantity: Int, reference: String?, note: String?): StockLevel {
        require(quantity > 0) { "Quantity to receive must be positive" }
        val stock = lockOrCreate(productId, warehouseId)
        stock.quantity += quantity
        val saved = stockLevelRepository.save(stock)
        record(saved, MovementType.IN, quantity, reference, note)
        return saved
    }

    @Transactional
    fun adjust(productId: Long, warehouseId: Long, delta: Int, reference: String?, note: String?): StockLevel {
        require(delta != 0) { "Adjustment delta must not be zero" }
        val stock = lockOrCreate(productId, warehouseId)
        val resulting = stock.quantity + delta
        if (resulting < 0) {
            throw InsufficientStockException(
                "Adjustment would drive stock negative for product $productId in warehouse $warehouseId",
            )
        }
        stock.quantity = resulting
        val saved = stockLevelRepository.save(stock)
        record(saved, MovementType.ADJUSTMENT, kotlin.math.abs(delta), reference, note)
        return saved
    }

    /**
     * Deducts [quantity] from a single warehouse, failing atomically if insufficient.
     * Intended to be called from within a larger transaction (e.g. confirming a sale).
     */
    @Transactional
    fun deduct(productId: Long, warehouseId: Long, quantity: Int, reference: String?, note: String?): StockLevel {
        require(quantity > 0) { "Quantity to deduct must be positive" }
        val stock = stockLevelRepository.findForUpdate(productId, warehouseId)
            ?: throw InsufficientStockException("No stock for product $productId in warehouse $warehouseId")
        if (stock.quantity < quantity) {
            throw InsufficientStockException(
                "Insufficient stock for product $productId in warehouse $warehouseId: " +
                    "have ${stock.quantity}, need $quantity",
            )
        }
        stock.quantity -= quantity
        val saved = stockLevelRepository.save(stock)
        record(saved, MovementType.OUT, quantity, reference, note)
        return saved
    }

    private fun lockOrCreate(productId: Long, warehouseId: Long): StockLevel {
        val existing = stockLevelRepository.findForUpdate(productId, warehouseId)
        if (existing != null) return existing
        val product: Product = productRepository.findById(productId)
            .orElseThrow { NotFoundException("Product $productId not found") }
        val warehouse: Warehouse = warehouseRepository.findById(warehouseId)
            .orElseThrow { NotFoundException("Warehouse $warehouseId not found") }
        return stockLevelRepository.save(StockLevel(product = product, warehouse = warehouse, quantity = 0))
    }

    private fun record(stock: StockLevel, type: MovementType, quantity: Int, reference: String?, note: String?) {
        movementRepository.save(
            InventoryMovement(
                product = stock.product,
                warehouse = stock.warehouse,
                type = type,
                quantity = quantity,
                balanceAfter = stock.quantity,
                reference = reference,
                note = note,
            ),
        )
    }
}
