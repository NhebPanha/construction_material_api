package org.example.construction_material_api.inventory

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import java.time.Instant

data class ReceiveStockRequest(
    @field:NotNull val productId: Long,
    @field:NotNull val warehouseId: Long,
    @field:Min(1) val quantity: Int,
    val reference: String? = null,
    val note: String? = null,
)

data class AdjustStockRequest(
    @field:NotNull val productId: Long,
    @field:NotNull val warehouseId: Long,
    /** Positive to increase, negative to decrease. Must not be zero. */
    @field:NotNull val delta: Int,
    val reference: String? = null,
    val note: String? = null,
)

data class StockLevelResponse(
    val productId: Long,
    val productSku: String,
    val productName: String,
    val warehouseId: Long,
    val warehouseCode: String,
    val quantity: Int,
    val stockStatus: StockStatus,
)

data class MovementResponse(
    val id: Long,
    val productId: Long,
    val productSku: String,
    val warehouseId: Long,
    val warehouseCode: String,
    val type: MovementType,
    val quantity: Int,
    val balanceAfter: Int,
    val reference: String?,
    val note: String?,
    val timestamp: Instant,
)

fun StockLevel.toResponse(): StockLevelResponse = StockLevelResponse(
    productId = product.id ?: 0,
    productSku = product.sku,
    productName = product.name,
    warehouseId = warehouse.id ?: 0,
    warehouseCode = warehouse.code,
    quantity = quantity,
    stockStatus = StockStatus.of(quantity, product.reorderLevel),
)

fun InventoryMovement.toResponse(): MovementResponse = MovementResponse(
    id = id ?: 0,
    productId = product.id ?: 0,
    productSku = product.sku,
    warehouseId = warehouse.id ?: 0,
    warehouseCode = warehouse.code,
    type = type,
    quantity = quantity,
    balanceAfter = balanceAfter,
    reference = reference,
    note = note,
    timestamp = createdAt,
)
