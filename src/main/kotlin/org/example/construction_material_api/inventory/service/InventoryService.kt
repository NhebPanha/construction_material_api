package org.example.construction_material_api.inventory.service

import org.example.construction_material_api.inventory.model.*
import org.example.construction_material_api.inventory.repository.*
import org.example.construction_material_api.inventory.dto.*

import org.example.construction_material_api.common.InsufficientStockException
import org.example.construction_material_api.common.NotFoundException
import org.example.construction_material_api.common.PageResponse
import org.example.construction_material_api.common.Paging
import org.example.construction_material_api.common.StockMovementType
import org.example.construction_material_api.product.model.Product
import org.example.construction_material_api.product.repository.ProductRepository
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

/**
 * Owns all changes to product stock. Every change locks the product row, applies a signed
 * delta derived from the movement type, refuses to drive stock negative, and records an
 * immutable [InventoryMovement] for audit.
 */
@Service
class InventoryService(
    private val productRepository: ProductRepository,
    private val movementRepository: InventoryMovementRepository,
) {

    /**
     * Posts a movement and adjusts the product's stock atomically.
     * - stockIn / adjustment(+): increase
     * - stockOut / sale / adjustment(-): decrease (never below zero)
     * - transfer: recorded with no net change to global stock
     */
    @Transactional
    fun postMovement(productId: Long, type: StockMovementType, quantity: Int, note: String?): InventoryMovement {
        val product = productRepository.findForUpdate(productId)
            ?: throw NotFoundException("Product $productId not found")
        val delta = deltaFor(type, quantity)
        applyDelta(product, delta)
        productRepository.save(product)
        return movementRepository.save(
            InventoryMovement(product = product, type = type, quantity = quantity, note = note),
        )
    }

    /**
     * Deducts stock for a sale within the caller's transaction. Locks the row and fails
     * atomically (rolling back the whole sale) if stock is insufficient.
     */
    @Transactional
    fun deductForSale(productId: Long, quantity: Int, note: String?): Product {
        require(quantity > 0) { "Sale quantity must be positive" }
        val product = productRepository.findForUpdate(productId)
            ?: throw NotFoundException("Product $productId not found")
        if (product.stockQuantity < quantity) {
            throw InsufficientStockException(
                "Insufficient stock for '${product.name}': have ${product.stockQuantity}, need $quantity",
            )
        }
        product.stockQuantity -= quantity
        val saved = productRepository.save(product)
        movementRepository.save(
            InventoryMovement(product = saved, type = StockMovementType.sale, quantity = quantity, note = note),
        )
        return saved
    }

    /** Restores stock for a refunded sale. */
    @Transactional
    fun restock(productId: Long, quantity: Int, note: String?) {
        if (quantity <= 0) return
        val product = productRepository.findForUpdate(productId)
            ?: throw NotFoundException("Product $productId not found")
        product.stockQuantity += quantity
        productRepository.save(product)
        movementRepository.save(
            InventoryMovement(product = product, type = StockMovementType.stockIn, quantity = quantity, note = note),
        )
    }

    @Transactional(readOnly = true)
    fun stocks(q: String?, page: Int?, pageSize: Int?): PageResponse<StockResponse> {
        val pageable = Paging.of(page, pageSize, Sort.by(Sort.Direction.ASC, "name"))
        val result = productRepository.search(q, null, false, pageable)
        return PageResponse.from(result) { it.toStockResponse() }
    }

    @Transactional(readOnly = true)
    fun movements(productId: Long?, page: Int?, pageSize: Int?): PageResponse<MovementResponse> =
        PageResponse.from(movementRepository.findFiltered(productId, Paging.of(page, pageSize))) { it.toResponse() }

    @Transactional(readOnly = true)
    fun dashboard(): InventoryDashboardResponse {
        val agg = productRepository.stockAggregates().firstOrNull()
        val totalUnits = (agg?.get(0) as? Number)?.toLong() ?: 0L
        val costValue = (agg?.get(1) as? BigDecimal) ?: BigDecimal.ZERO
        val retailValue = (agg?.get(2) as? BigDecimal) ?: BigDecimal.ZERO
        return InventoryDashboardResponse(
            totalProducts = productRepository.count(),
            lowStockCount = productRepository.countLowStock(),
            outOfStockCount = productRepository.countByStockQuantityLessThanEqual(0),
            totalStockUnits = totalUnits,
            stockCostValue = costValue,
            stockRetailValue = retailValue,
        )
    }

    private fun deltaFor(type: StockMovementType, quantity: Int): Int = when (type) {
        StockMovementType.stockIn -> kotlin.math.abs(quantity)
        StockMovementType.stockOut, StockMovementType.sale -> -kotlin.math.abs(quantity)
        StockMovementType.adjustment -> quantity // signed
        StockMovementType.transfer -> 0
    }

    private fun applyDelta(product: Product, delta: Int) {
        val resulting = product.stockQuantity + delta
        if (resulting < 0) {
            throw InsufficientStockException(
                "Movement would drive stock negative for '${product.name}': " +
                    "have ${product.stockQuantity}, change $delta",
            )
        }
        product.stockQuantity = resulting
    }
}
