package org.example.construction_material_api.warehouse.dto

import org.example.construction_material_api.warehouse.model.*
import org.example.construction_material_api.warehouse.repository.*
import org.example.construction_material_api.warehouse.service.*

import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank

data class WarehouseRequest(
    @field:NotBlank val code: String,
    @field:NotBlank val name: String,
    val location: String? = null,
    @field:Min(0) val capacity: Int = 0,
    @field:Min(0) val used: Int = 0,
    @field:Min(0) val incoming: Int = 0,
    @field:Min(0) val outgoing: Int = 0,
    val isPrimary: Boolean = false,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class WarehouseResponse(
    val id: String,
    val code: String,
    val name: String,
    val location: String?,
    val capacity: Int,
    val used: Int,
    val incoming: Int,
    val outgoing: Int,
    val isPrimary: Boolean,
)

fun Warehouse.toResponse(): WarehouseResponse = WarehouseResponse(
    id = (id ?: 0).toString(),
    code = code,
    name = name,
    location = location,
    capacity = capacity,
    used = used,
    incoming = incoming,
    outgoing = outgoing,
    isPrimary = isPrimary,
)
