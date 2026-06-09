package org.example.construction_material_api.sales

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.example.construction_material_api.common.BaseEntity
import org.example.construction_material_api.customer.Customer
import org.example.construction_material_api.product.Product
import org.example.construction_material_api.warehouse.Warehouse
import java.math.BigDecimal
import java.time.Instant

enum class SalesOrderStatus {
    /** Editable, not yet committed; no stock impact. */
    DRAFT,

    /** Parked order (a "hold"); editable, no stock impact. */
    HELD,

    /** Committed; stock has been deducted. */
    CONFIRMED,

    /** Voided before confirmation. */
    CANCELLED,
}

@Entity
@Table(name = "sales_orders")
class SalesOrder(
    @Column(nullable = false, unique = true)
    var orderNumber: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    var customer: Customer? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "warehouse_id")
    var warehouse: Warehouse,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var status: SalesOrderStatus = SalesOrderStatus.DRAFT,

    @Column(nullable = false, precision = 19, scale = 2)
    var discount: BigDecimal = BigDecimal.ZERO,

    /** Tax rate as a percentage, e.g. 10.00 for 10%. */
    @Column(nullable = false, precision = 5, scale = 2)
    var taxRate: BigDecimal = BigDecimal.ZERO,

    @Column(length = 500)
    var note: String? = null,
) : BaseEntity() {

    @OneToMany(mappedBy = "order", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    var lines: MutableList<SalesOrderLine> = mutableListOf()

    // Server-authoritative monetary totals, recomputed whenever lines change.
    @Column(nullable = false, precision = 19, scale = 2)
    var subtotal: BigDecimal = BigDecimal.ZERO

    @Column(nullable = false, precision = 19, scale = 2)
    var taxAmount: BigDecimal = BigDecimal.ZERO

    @Column(nullable = false, precision = 19, scale = 2)
    var total: BigDecimal = BigDecimal.ZERO

    @Column
    var confirmedAt: Instant? = null
}

@Entity
@Table(name = "sales_order_lines")
class SalesOrderLine(
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id")
    var order: SalesOrder,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id")
    var product: Product,

    @Column(nullable = false)
    var quantity: Int,

    /** Unit price snapshot taken from the product at the time the line was added. */
    @Column(nullable = false, precision = 19, scale = 2)
    var unitPrice: BigDecimal,

    @Column(nullable = false, precision = 19, scale = 2)
    var lineTotal: BigDecimal,
) : BaseEntity()
