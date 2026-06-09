package org.example.construction_material_api.delivery

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.example.construction_material_api.common.BaseEntity
import org.example.construction_material_api.sales.SalesOrder
import java.time.Instant
import java.time.LocalDate

enum class DeliveryStatus {
    PENDING,
    DISPATCHED,
    DELIVERED,
    CANCELLED,
}

@Entity
@Table(name = "deliveries")
class Delivery(
    @Column(nullable = false, unique = true)
    var deliveryNumber: String,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sales_order_id")
    var salesOrder: SalesOrder,

    @Column(nullable = false, length = 500)
    var address: String,

    @Column
    var scheduledDate: LocalDate? = null,

    @Column
    var driver: String? = null,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var status: DeliveryStatus = DeliveryStatus.PENDING,

    @Column
    var dispatchedAt: Instant? = null,

    @Column
    var deliveredAt: Instant? = null,

    @Column(length = 500)
    var note: String? = null,
) : BaseEntity()
