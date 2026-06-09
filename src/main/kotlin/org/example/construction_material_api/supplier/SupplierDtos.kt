package org.example.construction_material_api.supplier

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class SupplierRequest(
    @field:NotBlank val name: String,
    val phone: String? = null,
    @field:Email val email: String? = null,
    val address: String? = null,
    val contactPerson: String? = null,
    val active: Boolean = true,
)

data class SupplierResponse(
    val id: Long,
    val name: String,
    val phone: String?,
    val email: String?,
    val address: String?,
    val contactPerson: String?,
    val active: Boolean,
)
