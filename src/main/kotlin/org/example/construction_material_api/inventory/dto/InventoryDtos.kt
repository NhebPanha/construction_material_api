package org.example.construction_material_api.inventory.dto

import org.example.construction_material_api.inventory.model.*
import org.example.construction_material_api.inventory.repository.*
import org.example.construction_material_api.inventory.service.*

import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import org.example.construction_material_api.common.StockMovementType
import org.example.construction_material_api.product.model.Product
import org.example.construction_material_api.product.model.StockStatus
import java.math.BigDecimal
import java.time.Instant

data class CreateMovementRequest(
    @field:NotBlank val productId: String,
    @field:NotNull val type: StockMovementType,
    @field:NotNull val quantity: Int,
    val note: String? = null,
)

/** Movement shape: { id, productId, productName, type, quantity, note?, createdAt }. */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class MovementResponse(
    val id: String,
    val productId: String,
    val productName: String,
    val type: StockMovementType,
    val quantity: Int,
    val note: String?,
    val createdAt: Instant,
)

data class StockResponse(
    val productId: String,
    val productName: String,
    val stockQuantity: Int,
    val lowStockThreshold: Int,
    val stockStatus: StockStatus,
    val unit: String,
)

data class InventoryDashboardResponse(
    val totalProducts: Long,
    val lowStockCount: Long,
    val outOfStockCount: Long,
    val totalStockUnits: Long,
    val stockCostValue: BigDecimal,
    val stockRetailValue: BigDecimal,
)

fun InventoryMovement.toResponse(): MovementResponse = MovementResponse(
    id = (id ?: 0).toString(),
    productId = (product.id ?: 0).toString(),
    productName = product.name,
    type = type,
    quantity = quantity,
    note = note,
    createdAt = createdAt,
)

fun Product.toStockResponse(): StockResponse = StockResponse(
    productId = (id ?: 0).toString(),
    productName = name,
    stockQuantity = stockQuantity,
    lowStockThreshold = lowStockThreshold,
    stockStatus = StockStatus.of(stockQuantity, lowStockThreshold),
    unit = unit,
)
