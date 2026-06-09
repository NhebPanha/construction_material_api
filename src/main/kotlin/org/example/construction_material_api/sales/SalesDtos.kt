package org.example.construction_material_api.sales

import jakarta.validation.Valid
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal
import java.time.Instant

data class SalesOrderLineRequest(
    @field:NotNull val productId: Long,
    @field:Min(1) val quantity: Int,
)

/**
 * Request to create or replace a sales order. Note that line prices are NOT accepted
 * from the client — the server snapshots them from the product to keep totals authoritative.
 */
data class SalesOrderRequest(
    val customerId: Long? = null,
    /** Optional warehouse code; falls back to the configured default warehouse. */
    val warehouseCode: String? = null,
    @field:NotEmpty @field:Valid val lines: List<SalesOrderLineRequest>,
    @field:DecimalMin("0.0") val discount: BigDecimal = BigDecimal.ZERO,
    @field:DecimalMin("0.0") val taxRate: BigDecimal = BigDecimal.ZERO,
    val note: String? = null,
    /** When true the order is parked as HELD instead of DRAFT. */
    val hold: Boolean = false,
)

data class SalesOrderLineResponse(
    val productId: Long,
    val productSku: String,
    val productName: String,
    val quantity: Int,
    val unitPrice: BigDecimal,
    val lineTotal: BigDecimal,
)

data class SalesOrderResponse(
    val id: Long,
    val orderNumber: String,
    val customerId: Long?,
    val customerName: String?,
    val warehouseId: Long,
    val warehouseCode: String,
    val status: SalesOrderStatus,
    val lines: List<SalesOrderLineResponse>,
    val subtotal: BigDecimal,
    val discount: BigDecimal,
    val taxRate: BigDecimal,
    val taxAmount: BigDecimal,
    val total: BigDecimal,
    val note: String?,
    val confirmedAt: Instant?,
    val createdAt: Instant,
)
