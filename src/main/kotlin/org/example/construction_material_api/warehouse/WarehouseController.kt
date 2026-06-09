package org.example.construction_material_api.warehouse

import jakarta.validation.Valid
import org.example.construction_material_api.common.ApiResponse
import org.example.construction_material_api.common.PageResponse
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
        @RequestParam(required = false) search: String?,
        @RequestParam(required = false) page: Int?,
        @RequestParam(required = false) size: Int?,
        @RequestParam(required = false) sort: String?,
    ): ApiResponse<PageResponse<WarehouseResponse>> =
        ApiResponse.ok(warehouseService.list(search, page, size, sort), "Warehouses retrieved")

    @GetMapping("/{id}")
    fun get(@PathVariable id: Long): ApiResponse<WarehouseResponse> =
        ApiResponse.ok(warehouseService.get(id), "Warehouse retrieved")

    @PostMapping
    fun create(@Valid @RequestBody request: WarehouseRequest): ApiResponse<WarehouseResponse> =
        ApiResponse.ok(warehouseService.create(request), "Warehouse created")

    @PutMapping("/{id}")
    fun update(@PathVariable id: Long, @Valid @RequestBody request: WarehouseRequest): ApiResponse<WarehouseResponse> =
        ApiResponse.ok(warehouseService.update(id, request), "Warehouse updated")

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): ApiResponse<Unit> {
        warehouseService.delete(id)
        return ApiResponse.ok("Warehouse deactivated")
    }
}
