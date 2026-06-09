package org.example.construction_material_api.product

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import org.example.construction_material_api.common.BaseEntity
import java.math.BigDecimal

/**
 * A sellable construction material. Stock levels are tracked separately per warehouse
 * in the inventory module; stock status is derived, never stored.
 */
@Entity
@Table(name = "products")
class Product(
    @Column(nullable = false, unique = true)
    var sku: String,

    @Column(nullable = false)
    var name: String,

    @Column(length = 1000)
    var description: String? = null,

    @Column(nullable = false)
    var unit: String = "EA",

    @Column(nullable = false)
    var category: String = "GENERAL",

    @Column(nullable = false, precision = 19, scale = 2)
    var price: BigDecimal = BigDecimal.ZERO,

    @Column(nullable = false, precision = 19, scale = 2)
    var costPrice: BigDecimal = BigDecimal.ZERO,

    /** Quantity at or below which the product is considered low stock. */
    @Column(nullable = false)
    var reorderLevel: Int = 0,

    @Column(nullable = false)
    var active: Boolean = true,
) : BaseEntity()
