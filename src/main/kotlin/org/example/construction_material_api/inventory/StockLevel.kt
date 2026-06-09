package org.example.construction_material_api.inventory

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import jakarta.persistence.Version
import org.example.construction_material_api.common.BaseEntity
import org.example.construction_material_api.product.Product
import org.example.construction_material_api.warehouse.Warehouse

/**
 * Current on-hand quantity of a product in a specific warehouse. The optimistic
 * [version] guards against lost updates under concurrent stock changes.
 */
@Entity
@Table(
    name = "stock_levels",
    uniqueConstraints = [UniqueConstraint(columnNames = ["product_id", "warehouse_id"])],
)
class StockLevel(
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    var product: Product,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    var warehouse: Warehouse,

    @Column(nullable = false)
    var quantity: Int = 0,
) : BaseEntity() {

    @Version
    var version: Long = 0
}
