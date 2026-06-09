package org.example.construction_material_api.warehouse.model

import org.example.construction_material_api.warehouse.repository.*
import org.example.construction_material_api.warehouse.dto.*
import org.example.construction_material_api.warehouse.service.*

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
    var capacity: Int = 0,

    @Column(nullable = false)
    var used: Int = 0,

    @Column(nullable = false)
    var incoming: Int = 0,

    @Column(nullable = false)
    var outgoing: Int = 0,

    @Column(nullable = false)
    var isPrimary: Boolean = false,
) : BaseEntity()
