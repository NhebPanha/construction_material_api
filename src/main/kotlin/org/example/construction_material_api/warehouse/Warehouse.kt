package org.example.construction_material_api.warehouse

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import org.example.construction_material_api.common.BaseEntity

@Entity
@Table(name = "warehouses")
class Warehouse(
    @Column(nullable = false, unique = true)
    var code: String,

    @Column(nullable = false)
    var name: String,

    @Column
    var location: String? = null,

    @Column(nullable = false)
    var active: Boolean = true,
) : BaseEntity()
