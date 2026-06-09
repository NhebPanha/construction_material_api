package org.example.construction_material_api.inventory

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.example.construction_material_api.common.BaseEntity
import org.example.construction_material_api.product.Product
import org.example.construction_material_api.warehouse.Warehouse

enum class MovementType {
    /** Stock received into a warehouse (positive). */
    IN,

    /** Stock issued out of a warehouse, e.g. for a sale (negative). */
    OUT,

    /** Manual correction; quantity sign indicates direction. */
    ADJUSTMENT,
}

/**
 * An immutable audit record of a change to stock. [quantity] is always positive; the
 * direction of the change is conveyed by [type].
 */
@Entity
@Table(name = "inventory_movements")
class InventoryMovement(
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    var product: Product,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    var warehouse: Warehouse,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var type: MovementType,

    @Column(nullable = false)
    var quantity: Int,

    /** Resulting on-hand quantity after this movement was applied. */
    @Column(nullable = false)
    var balanceAfter: Int,

    /** Optional external reference, e.g. a sales order number or GRN. */
    @Column
    var reference: String? = null,

    @Column(length = 500)
    var note: String? = null,
) : BaseEntity()
