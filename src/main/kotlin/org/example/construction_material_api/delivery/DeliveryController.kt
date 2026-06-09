package org.example.construction_material_api.delivery

import jakarta.validation.Valid
import org.example.construction_material_api.common.ApiResponse
import org.example.construction_material_api.common.PageResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/deliveries")
class DeliveryController(private val deliveryService: DeliveryService) {

    @GetMapping
    fun list(
        @RequestParam(required = false) status: DeliveryStatus?,
        @RequestParam(required = false) search: String?,
        @RequestParam(required = false) page: Int?,
        @RequestParam(required = false) size: Int?,
        @RequestParam(required = false) sort: String?,
    ): ApiResponse<PageResponse<DeliveryResponse>> =
        ApiResponse.ok(deliveryService.list(status, search, page, size, sort), "Deliveries retrieved")

    @GetMapping("/{id}")
    fun get(@PathVariable id: Long): ApiResponse<DeliveryResponse> =
        ApiResponse.ok(deliveryService.get(id), "Delivery retrieved")

    @PostMapping
    fun create(@Valid @RequestBody request: CreateDeliveryRequest): ApiResponse<DeliveryResponse> =
        ApiResponse.ok(deliveryService.create(request), "Delivery created")

    @PatchMapping("/{id}/status")
    fun updateStatus(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateDeliveryStatusRequest,
    ): ApiResponse<DeliveryResponse> =
        ApiResponse.ok(deliveryService.updateStatus(id, request), "Delivery status updated")
}
