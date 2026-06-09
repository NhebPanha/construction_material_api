package org.example.construction_material_api.supplier.dto

import org.example.construction_material_api.supplier.model.*
import org.example.construction_material_api.supplier.repository.*
import org.example.construction_material_api.supplier.service.*

import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import java.math.BigDecimal

data class SupplierRequest(
    @field:NotBlank val name: String,
    val phone: String? = null,
    @field:Email val email: String? = null,
    val address: String? = null,
    val payable: BigDecimal = BigDecimal.ZERO,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class SupplierResponse(
    val id: String,
    val name: String,
    val phone: String?,
    val email: String?,
    val address: String?,
    val payable: BigDecimal,
)

fun Supplier.toResponse(): SupplierResponse = SupplierResponse(
    id = (id ?: 0).toString(),
    name = name,
    phone = phone,
    email = email,
    address = address,
    payable = payable,
)
