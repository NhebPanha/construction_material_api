package org.example.construction_material_api.product.model

import org.example.construction_material_api.product.repository.*
import org.example.construction_material_api.product.dto.*
import org.example.construction_material_api.product.service.*

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table
import org.example.construction_material_api.common.BaseEntity
import org.example.construction_material_api.common.ProductCategory
import java.math.BigDecimal

/**
 * A sellable construction material. `stockQuantity` is the authoritative on-hand quantity,
 * adjusted atomically by inventory movements and sales. Stock status is derived, never stored.
 */
@Entity
@Table(name = "products")
class Product(
    @Column(nullable = false)
    var name: String,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var category: ProductCategory,

    @Column(nullable = false, precision = 19, scale = 2)
    var costPrice: BigDecimal = BigDecimal.ZERO,

    @Column(nullable = false, precision = 19, scale = 2)
    var sellingPrice: BigDecimal = BigDecimal.ZERO,

    @Column(nullable = false)
    var stockQuantity: Int = 0,

    @Column(nullable = false)
    var lowStockThreshold: Int = 0,

    @Column(nullable = false)
    var unit: String = "pcs",

    @Column(length = 1000)
    var description: String? = null,

    @Column
    var imageUrl: String? = null,

    @Column
    var barcode: String? = null,
) : BaseEntity()

enum class StockStatus {
    inStock, lowStock, outOfStock;

    companion object {
        fun of(quantity: Int, lowStockThreshold: Int): StockStatus = when {
            quantity <= 0 -> outOfStock
            quantity <= lowStockThreshold -> lowStock
            else -> inStock
        }
    }
}
