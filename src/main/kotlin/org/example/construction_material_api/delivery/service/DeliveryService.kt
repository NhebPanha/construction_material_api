package org.example.construction_material_api.delivery.service

import org.example.construction_material_api.delivery.model.*
import org.example.construction_material_api.delivery.repository.*
import org.example.construction_material_api.delivery.dto.*

import org.example.construction_material_api.common.DeliveryStatus
import org.example.construction_material_api.common.NotFoundException
import org.example.construction_material_api.common.PageResponse
import org.example.construction_material_api.common.Paging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DeliveryService(private val deliveryRepository: DeliveryRepository) {

    @Transactional(readOnly = true)
    fun list(status: DeliveryStatus?, page: Int?, pageSize: Int?): PageResponse<DeliveryResponse> =
        PageResponse.from(deliveryRepository.findFiltered(status, Paging.of(page, pageSize))) { it.toResponse() }

    @Transactional(readOnly = true)
    fun get(id: Long): DeliveryResponse = findOrThrow(id).toResponse()

    @Transactional
    fun create(request: CreateDeliveryRequest): DeliveryResponse {
        val delivery = Delivery(
            reference = "PENDING",
            customerName = request.customerName,
            address = request.address,
            driverName = request.driverName,
            vehicle = request.vehicle,
            eta = request.eta,
            itemsSummary = request.itemsSummary,
            note = request.note,
        )
        val saved = deliveryRepository.save(delivery)
        saved.reference = "DLV-%06d".format(saved.id)
        return deliveryRepository.save(saved).toResponse()
    }

    @Transactional
    fun updateStatus(id: Long, request: UpdateDeliveryStatusRequest): DeliveryResponse {
        val delivery = findOrThrow(id)
        delivery.status = request.status
        return deliveryRepository.save(delivery).toResponse()
    }

    private fun findOrThrow(id: Long): Delivery =
        deliveryRepository.findById(id).orElseThrow { NotFoundException("Delivery $id not found") }
}
