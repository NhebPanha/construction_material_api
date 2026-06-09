package org.example.construction_material_api.warehouse.controller

import org.example.construction_material_api.warehouse.model.*
import org.example.construction_material_api.warehouse.repository.*
import org.example.construction_material_api.warehouse.dto.*
import org.example.construction_material_api.warehouse.service.*

import jakarta.validation.Valid
import org.example.construction_material_api.common.ApiResponse
import org.example.construction_material_api.common.PageResponse
import org.example.construction_material_api.common.toLongId
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/warehouses")
class WarehouseController(private val warehouseService: WarehouseService) {

    @GetMapping
    fun list(
        @RequestParam(required = false) q: String?,
        @RequestParam(required = false) page: Int?,
        @RequestParam(required = false) pageSize: Int?,
    ): ApiResponse<PageResponse<WarehouseResponse>> =
        ApiResponse.ok(warehouseService.list(q, page, pageSize), "Warehouses retrieved")

    @GetMapping("/{id}")
    fun get(@PathVariable id: String): ApiResponse<WarehouseResponse> =
        ApiResponse.ok(warehouseService.get(id.toLongId()), "Warehouse retrieved")

    @PostMapping
    @PreAuthorize("hasAnyRole('admin','warehouse')")
    fun create(@Valid @RequestBody request: WarehouseRequest): ApiResponse<WarehouseResponse> =
        ApiResponse.ok(warehouseService.create(request), "Warehouse created")

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('admin','warehouse')")
    fun update(@PathVariable id: String, @Valid @RequestBody request: WarehouseRequest): ApiResponse<WarehouseResponse> =
        ApiResponse.ok(warehouseService.update(id.toLongId(), request), "Warehouse updated")

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    fun delete(@PathVariable id: String): ApiResponse<Unit> {
        warehouseService.delete(id.toLongId())
        return ApiResponse.ok("Warehouse deleted")
    }
}
