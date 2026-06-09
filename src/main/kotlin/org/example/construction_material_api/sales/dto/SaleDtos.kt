package org.example.construction_material_api.sales.dto

import org.example.construction_material_api.sales.model.*
import org.example.construction_material_api.sales.repository.*
import org.example.construction_material_api.sales.service.*

import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.validation.Valid
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import org.example.construction_material_api.common.PaymentMethod
import org.example.construction_material_api.common.SaleStatus
import java.math.BigDecimal
import java.time.Instant

data class SaleLineRequest(
    @field:NotBlank val productId: String,
    @field:Min(1) val quantity: Int,
    @field:DecimalMin("0.0") val lineDiscount: BigDecimal = BigDecimal.ZERO,
)

/**
 * Request to create a sale (complete or hold). Unit prices are NOT accepted from the
 * client â€” the server snapshots them from the product to keep totals authoritative.
 */
data class SaleRequest(
    val customerId: String? = null,
    @field:NotEmpty @field:Valid val lines: List<SaleLineRequest>,
    @field:DecimalMin("0.0") val discount: BigDecimal = BigDecimal.ZERO,
    @field:DecimalMin("0.0") val taxRate: BigDecimal = BigDecimal.ZERO,
    @field:NotNull val paymentMethod: PaymentMethod = PaymentMethod.cash,
    @field:DecimalMin("0.0") val amountReceived: BigDecimal = BigDecimal.ZERO,
)

data class SaleLineResponse(
    val productId: String,
    val productName: String,
    val quantity: Int,
    val unitPrice: BigDecimal,
    val lineDiscount: BigDecimal,
)

/**
 * Client Sale shape plus server-authoritative computed totals (subtotal, tax, grandTotal,
 * change) which the client uses to render the receipt.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class SaleResponse(
    val id: String,
    val invoiceNumber: String,
    val createdAt: Instant,
    val lines: List<SaleLineResponse>,
    val discount: BigDecimal,
    val taxRate: BigDecimal,
    val paymentMethod: PaymentMethod,
    val amountReceived: BigDecimal,
    val status: SaleStatus,
    val cashierName: String,
    val customerId: String?,
    val customerName: String?,
    val subtotal: BigDecimal,
    val tax: BigDecimal,
    val grandTotal: BigDecimal,
    val change: BigDecimal,
)

fun Sale.toResponse(): SaleResponse = SaleResponse(
    id = (id ?: 0).toString(),
    invoiceNumber = invoiceNumber,
    createdAt = createdAt,
    lines = lines.map {
        SaleLineResponse(
            productId = (it.product.id ?: 0).toString(),
            productName = it.productName,
            quantity = it.quantity,
            unitPrice = it.unitPrice,
            lineDiscount = it.lineDiscount,
        )
    },
    discount = discount,
    taxRate = taxRate,
    paymentMethod = paymentMethod,
    amountReceived = amountReceived,
    status = status,
    cashierName = cashierName,
    customerId = customer?.id?.toString(),
    customerName = customer?.name,
    subtotal = subtotal,
    tax = tax,
    grandTotal = grandTotal,
    change = changeDue,
)
