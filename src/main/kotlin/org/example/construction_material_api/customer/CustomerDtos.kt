package org.example.construction_material_api.customer

import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import java.math.BigDecimal

data class CustomerRequest(
    @field:NotBlank val name: String,
    val phone: String? = null,
    @field:Email val email: String? = null,
    val address: String? = null,
    @field:DecimalMin("0.0") val creditLimit: BigDecimal = BigDecimal.ZERO,
    val active: Boolean = true,
)

data class CustomerResponse(
    val id: Long,
    val name: String,
    val phone: String?,
    val email: String?,
    val address: String?,
    val creditLimit: BigDecimal,
    val active: Boolean,
)
