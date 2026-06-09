package org.example.construction_material_api.customer.dto

import org.example.construction_material_api.customer.model.*
import org.example.construction_material_api.customer.repository.*
import org.example.construction_material_api.customer.service.*

import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import java.math.BigDecimal

data class CustomerRequest(
    @field:NotBlank val name: String,
    val phone: String? = null,
    @field:Email val email: String? = null,
    val address: String? = null,
    @field:Min(0) val loyaltyPoints: Int = 0,
    val balance: BigDecimal = BigDecimal.ZERO,
    @field:DecimalMin("0.0") val discountRate: BigDecimal = BigDecimal.ZERO,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class CustomerResponse(
    val id: String,
    val name: String,
    val phone: String?,
    val email: String?,
    val address: String?,
    val loyaltyPoints: Int,
    val balance: BigDecimal,
    val discountRate: BigDecimal,
)

fun Customer.toResponse(): CustomerResponse = CustomerResponse(
    id = (id ?: 0).toString(),
    name = name,
    phone = phone,
    email = email,
    address = address,
    loyaltyPoints = loyaltyPoints,
    balance = balance,
    discountRate = discountRate,
)
