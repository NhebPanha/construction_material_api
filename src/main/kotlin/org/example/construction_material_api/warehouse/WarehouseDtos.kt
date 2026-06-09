package org.example.construction_material_api.warehouse

import jakarta.validation.constraints.NotBlank

data class WarehouseRequest(
    @field:NotBlank val code: String,
    @field:NotBlank val name: String,
    val location: String? = null,
    val active: Boolean = true,
)

data class WarehouseResponse(
    val id: Long,
    val code: String,
    val name: String,
    val location: String?,
    val active: Boolean,
)
