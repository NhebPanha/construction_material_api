package org.example.construction_material_api.product.dto

import org.example.construction_material_api.product.model.*
import org.example.construction_material_api.product.repository.*
import org.example.construction_material_api.product.service.*

import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import org.example.construction_material_api.common.ProductCategory
import java.math.BigDecimal

data class ProductRequest(
    @field:NotBlank val name: String,
    @field:NotNull val category: ProductCategory,
    @field:DecimalMin("0.0") val costPrice: BigDecimal = BigDecimal.ZERO,
    @field:DecimalMin("0.0") val sellingPrice: BigDecimal = BigDecimal.ZERO,
    @field:Min(0) val stockQuantity: Int = 0,
    @field:Min(0) val lowStockThreshold: Int = 0,
    @field:NotBlank val unit: String = "pcs",
    val description: String? = null,
    val imageUrl: String? = null,
    val barcode: String? = null,
)

/**
 * Client product shape. `stockStatus` is derived (inStock/lowStock/outOfStock) and not stored.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class ProductResponse(
    val id: String,
    val name: String,
    val category: ProductCategory,
    val costPrice: BigDecimal,
    val sellingPrice: BigDecimal,
    val stockQuantity: Int,
    val lowStockThreshold: Int,
    val unit: String,
    val description: String?,
    val imageUrl: String?,
    val barcode: String?,
    val stockStatus: StockStatus,
)

fun Product.toResponse(): ProductResponse = ProductResponse(
    id = (id ?: 0).toString(),
    name = name,
    category = category,
    costPrice = costPrice,
    sellingPrice = sellingPrice,
    stockQuantity = stockQuantity,
    lowStockThreshold = lowStockThreshold,
    unit = unit,
    description = description,
    imageUrl = imageUrl,
    barcode = barcode,
    stockStatus = StockStatus.of(stockQuantity, lowStockThreshold),
)
