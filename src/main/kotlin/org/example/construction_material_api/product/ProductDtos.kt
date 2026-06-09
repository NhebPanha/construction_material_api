package org.example.construction_material_api.product

import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import org.example.construction_material_api.inventory.StockStatus
import java.math.BigDecimal

data class ProductRequest(
    @field:NotBlank val sku: String,
    @field:NotBlank val name: String,
    val description: String? = null,
    @field:NotBlank val unit: String = "EA",
    @field:NotBlank val category: String = "GENERAL",
    @field:DecimalMin("0.0") val price: BigDecimal = BigDecimal.ZERO,
    @field:DecimalMin("0.0") val costPrice: BigDecimal = BigDecimal.ZERO,
    @field:Min(0) val reorderLevel: Int = 0,
    val active: Boolean = true,
)

data class ProductResponse(
    val id: Long,
    val sku: String,
    val name: String,
    val description: String?,
    val unit: String,
    val category: String,
    val price: BigDecimal,
    val costPrice: BigDecimal,
    val reorderLevel: Int,
    val active: Boolean,
    val stockOnHand: Int,
    val stockStatus: StockStatus,
)
