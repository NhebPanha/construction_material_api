package org.example.construction_material_api.supplier.model

import org.example.construction_material_api.supplier.repository.*
import org.example.construction_material_api.supplier.dto.*
import org.example.construction_material_api.supplier.service.*

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import org.example.construction_material_api.common.BaseEntity
import java.math.BigDecimal

@Entity
@Table(name = "suppliers")
class Supplier(
    @Column(nullable = false)
    var name: String,

    @Column
    var phone: String? = null,

    @Column
    var email: String? = null,

    @Column(length = 500)
    var address: String? = null,

    /** Outstanding amount payable to this supplier. */
    @Column(nullable = false, precision = 19, scale = 2)
    var payable: BigDecimal = BigDecimal.ZERO,
) : BaseEntity()
