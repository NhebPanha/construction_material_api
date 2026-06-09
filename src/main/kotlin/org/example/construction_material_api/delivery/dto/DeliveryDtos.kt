package org.example.construction_material_api.delivery.dto

import org.example.construction_material_api.delivery.model.*
import org.example.construction_material_api.delivery.repository.*
import org.example.construction_material_api.delivery.service.*

import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import org.example.construction_material_api.common.DeliveryStatus
import java.time.Instant

data class CreateDeliveryRequest(
    @field:NotBlank val customerName: String,
    @field:NotBlank val address: String,
    val driverName: String? = null,
    val vehicle: String? = null,
    val eta: Instant? = null,
    val itemsSummary: String? = null,
    val note: String? = null,
)

data class UpdateDeliveryStatusRequest(
    @field:NotNull val status: DeliveryStatus,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class DeliveryResponse(
    val id: String,
    val reference: String,
    val customerName: String,
    val address: String,
    val driverName: String?,
    val vehicle: String?,
    val status: DeliveryStatus,
    val createdAt: Instant,
    val eta: Instant?,
    val itemsSummary: String?,
    val note: String?,
)

fun Delivery.toResponse(): DeliveryResponse = DeliveryResponse(
    id = (id ?: 0).toString(),
    reference = reference,
    customerName = customerName,
    address = address,
    driverName = driverName,
    vehicle = vehicle,
    status = status,
    createdAt = createdAt,
    eta = eta,
    itemsSummary = itemsSummary,
    note = note,
)
