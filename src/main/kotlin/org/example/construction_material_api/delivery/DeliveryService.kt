package org.example.construction_material_api.delivery

import org.example.construction_material_api.common.InvalidOrderStateException
import org.example.construction_material_api.common.NotFoundException
import org.example.construction_material_api.common.PageResponse
import org.example.construction_material_api.common.Paging
import org.example.construction_material_api.sales.SalesOrderRepository
import org.example.construction_material_api.sales.SalesOrderStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class DeliveryService(
    private val deliveryRepository: DeliveryRepository,
    private val salesOrderRepository: SalesOrderRepository,
) {

    @Transactional(readOnly = true)
    fun list(status: DeliveryStatus?, search: String?, page: Int?, size: Int?, sort: String?): PageResponse<DeliveryResponse> =
        PageResponse.from(
            deliveryRepository.search(status, search, Paging.of(page, size, sort ?: "createdAt,desc")),
        ) { it.toResponse() }

    @Transactional(readOnly = true)
    fun get(id: Long): DeliveryResponse = findOrThrow(id).toResponse()

    @Transactional
    fun create(request: CreateDeliveryRequest): DeliveryResponse {
        val order = salesOrderRepository.findById(request.salesOrderId)
            .orElseThrow { NotFoundException("Sales order ${request.salesOrderId} not found") }
        if (order.status != SalesOrderStatus.CONFIRMED) {
            throw InvalidOrderStateException("Deliveries can only be created for confirmed orders (current: ${order.status})")
        }
        val delivery = Delivery(
            deliveryNumber = "PENDING",
            salesOrder = order,
            address = request.address,
            scheduledDate = request.scheduledDate,
            driver = request.driver,
            note = request.note,
        )
        val saved = deliveryRepository.save(delivery)
        saved.deliveryNumber = "DLV-%06d".format(saved.id)
        return deliveryRepository.save(saved).toResponse()
    }

    @Transactional
    fun updateStatus(id: Long, request: UpdateDeliveryStatusRequest): DeliveryResponse {
        val delivery = findOrThrow(id)
        transition(delivery, request.status)
        if (request.note != null) delivery.note = request.note
        return deliveryRepository.save(delivery).toResponse()
    }

    private fun transition(delivery: Delivery, target: DeliveryStatus) {
        val current = delivery.status
        val allowed = when (current) {
            DeliveryStatus.PENDING -> target == DeliveryStatus.DISPATCHED || target == DeliveryStatus.CANCELLED
            DeliveryStatus.DISPATCHED -> target == DeliveryStatus.DELIVERED || target == DeliveryStatus.CANCELLED
            DeliveryStatus.DELIVERED, DeliveryStatus.CANCELLED -> false
        }
        if (!allowed) {
            throw InvalidOrderStateException("Cannot move delivery from $current to $target")
        }
        delivery.status = target
        when (target) {
            DeliveryStatus.DISPATCHED -> delivery.dispatchedAt = Instant.now()
            DeliveryStatus.DELIVERED -> delivery.deliveredAt = Instant.now()
            else -> Unit
        }
    }

    private fun findOrThrow(id: Long): Delivery =
        deliveryRepository.findById(id).orElseThrow { NotFoundException("Delivery $id not found") }
}
