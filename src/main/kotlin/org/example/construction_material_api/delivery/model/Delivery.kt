package org.example.construction_material_api.delivery.model

import org.example.construction_material_api.delivery.repository.*
import org.example.construction_material_api.delivery.dto.*
import org.example.construction_material_api.delivery.service.*

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table
import org.example.construction_material_api.common.BaseEntity
import org.example.construction_material_api.common.DeliveryStatus
import java.time.Instant

@Entity
@Table(name = "deliveries")
class Delivery(
    @Column(nullable = false, unique = true)
    var reference: String,

    @Column(nullable = false)
    var customerName: String,

    @Column(nullable = false, length = 500)
    var address: String,

    @Column
    var driverName: String? = null,

    @Column
    var vehicle: String? = null,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var status: DeliveryStatus = DeliveryStatus.pending,

    @Column
    var eta: Instant? = null,

    @Column(length = 1000)
    var itemsSummary: String? = null,

    @Column(length = 500)
    var note: String? = null,
) : BaseEntity()
