package org.example.construction_material_api.supplier

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import org.example.construction_material_api.common.BaseEntity

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

    @Column
    var contactPerson: String? = null,

    @Column(nullable = false)
    var active: Boolean = true,
) : BaseEntity()
