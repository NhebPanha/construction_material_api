package org.example.construction_material_api.inventory.controller

import org.example.construction_material_api.inventory.model.*
import org.example.construction_material_api.inventory.repository.*
import org.example.construction_material_api.inventory.dto.*
import org.example.construction_material_api.inventory.service.*

import jakarta.validation.Valid
import org.example.construction_material_api.common.ApiResponse
import org.example.construction_material_api.common.PageResponse
import org.example.construction_material_api.common.toLongId
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/inventory")
class InventoryController(private val inventoryService: InventoryService) {

    @GetMapping("/stocks")
    fun stocks(
        @RequestParam(required = false) q: String?,
        @RequestParam(required = false) page: Int?,
        @RequestParam(required = false) pageSize: Int?,
    ): ApiResponse<PageResponse<StockResponse>> =
        ApiResponse.ok(inventoryService.stocks(q, page, pageSize), "Stock levels retrieved")

    @GetMapping("/movements")
    fun movements(
        @RequestParam(required = false) productId: String?,
        @RequestParam(required = false) page: Int?,
        @RequestParam(required = false) pageSize: Int?,
    ): ApiResponse<PageResponse<MovementResponse>> =
        ApiResponse.ok(inventoryService.movements(productId?.toLongId(), page, pageSize), "Movements retrieved")

    @PostMapping("/movements")
    @PreAuthorize("hasAnyRole('admin','warehouse')")
    fun createMovement(@Valid @RequestBody request: CreateMovementRequest): ApiResponse<MovementResponse> {
        val movement = inventoryService.postMovement(
            request.productId.toLongId(), request.type, request.quantity, request.note,
        )
        return ApiResponse.ok(movement.toResponse(), "Movement recorded")
    }

    @GetMapping("/dashboard")
    fun dashboard(): ApiResponse<InventoryDashboardResponse> =
        ApiResponse.ok(inventoryService.dashboard(), "Inventory dashboard")
}
