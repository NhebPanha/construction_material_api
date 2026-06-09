package org.example.construction_material_api.delivery

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.Instant
import java.time.LocalDate

data class CreateDeliveryRequest(
    @field:NotNull val salesOrderId: Long,
    @field:NotBlank val address: String,
    val scheduledDate: LocalDate? = null,
    val driver: String? = null,
    val note: String? = null,
)

data class UpdateDeliveryStatusRequest(
    @field:NotNull val status: DeliveryStatus,
    val note: String? = null,
)

data class DeliveryResponse(
    val id: Long,
    val deliveryNumber: String,
    val salesOrderId: Long,
    val salesOrderNumber: String,
    val address: String,
    val scheduledDate: LocalDate?,
    val driver: String?,
    val status: DeliveryStatus,
    val dispatchedAt: Instant?,
    val deliveredAt: Instant?,
    val note: String?,
    val createdAt: Instant,
)

fun Delivery.toResponse(): DeliveryResponse = DeliveryResponse(
    id = id ?: 0,
    deliveryNumber = deliveryNumber,
    salesOrderId = salesOrder.id ?: 0,
    salesOrderNumber = salesOrder.orderNumber,
    address = address,
    scheduledDate = scheduledDate,
    driver = driver,
    status = status,
    dispatchedAt = dispatchedAt,
    deliveredAt = deliveredAt,
    note = note,
    createdAt = createdAt,
)
