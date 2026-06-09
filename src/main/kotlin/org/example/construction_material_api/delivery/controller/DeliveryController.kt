package org.example.construction_material_api.delivery.controller

import org.example.construction_material_api.delivery.model.*
import org.example.construction_material_api.delivery.repository.*
import org.example.construction_material_api.delivery.dto.*
import org.example.construction_material_api.delivery.service.*

import jakarta.validation.Valid
import org.example.construction_material_api.common.ApiResponse
import org.example.construction_material_api.common.DeliveryStatus
import org.example.construction_material_api.common.PageResponse
import org.example.construction_material_api.common.toLongId
import org.springframework.security.access.prepost.PreAuthorize
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
        @RequestParam(required = false) page: Int?,
        @RequestParam(required = false) pageSize: Int?,
    ): ApiResponse<PageResponse<DeliveryResponse>> =
        ApiResponse.ok(deliveryService.list(status, page, pageSize), "Deliveries retrieved")

    @GetMapping("/{id}")
    fun get(@PathVariable id: String): ApiResponse<DeliveryResponse> =
        ApiResponse.ok(deliveryService.get(id.toLongId()), "Delivery retrieved")

    @PostMapping
    @PreAuthorize("hasAnyRole('admin','warehouse')")
    fun create(@Valid @RequestBody request: CreateDeliveryRequest): ApiResponse<DeliveryResponse> =
        ApiResponse.ok(deliveryService.create(request), "Delivery created")

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('admin','warehouse')")
    fun updateStatus(
        @PathVariable id: String,
        @Valid @RequestBody request: UpdateDeliveryStatusRequest,
    ): ApiResponse<DeliveryResponse> =
        ApiResponse.ok(deliveryService.updateStatus(id.toLongId(), request), "Delivery status updated")
}
