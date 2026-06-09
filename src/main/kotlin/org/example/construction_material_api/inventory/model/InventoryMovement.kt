package org.example.construction_material_api.inventory.model

import org.example.construction_material_api.inventory.repository.*
import org.example.construction_material_api.inventory.dto.*
import org.example.construction_material_api.inventory.service.*

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.example.construction_material_api.common.BaseEntity
import org.example.construction_material_api.common.StockMovementType
import org.example.construction_material_api.product.model.Product

/**
 * An immutable audit record of a change to a product's stock. The sign of the effect on
 * stock is derived from [type]; [quantity] is the magnitude (signed only for adjustments).
 */
@Entity
@Table(name = "inventory_movements")
class InventoryMovement(
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id")
    var product: Product,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var type: StockMovementType,

    @Column(nullable = false)
    var quantity: Int,

    @Column(length = 500)
    var note: String? = null,
) : BaseEntity()
