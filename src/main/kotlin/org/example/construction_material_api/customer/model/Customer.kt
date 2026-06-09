package org.example.construction_material_api.customer.model

import org.example.construction_material_api.customer.repository.*
import org.example.construction_material_api.customer.dto.*
import org.example.construction_material_api.customer.service.*

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import org.example.construction_material_api.common.BaseEntity
import java.math.BigDecimal

@Entity
@Table(name = "customers")
class Customer(
    @Column(nullable = false)
    var name: String,

    @Column
    var phone: String? = null,

    @Column
    var email: String? = null,

    @Column(length = 500)
    var address: String? = null,

    @Column(nullable = false)
    var loyaltyPoints: Int = 0,

    @Column(nullable = false, precision = 19, scale = 2)
    var balance: BigDecimal = BigDecimal.ZERO,

    @Column(nullable = false, precision = 6, scale = 4)
    var discountRate: BigDecimal = BigDecimal.ZERO,
) : BaseEntity()
