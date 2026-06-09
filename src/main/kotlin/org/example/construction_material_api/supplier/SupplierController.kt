package org.example.construction_material_api.supplier

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
@RequestMapping("/api/v1/suppliers")
class SupplierController(private val supplierService: SupplierService) {

    @GetMapping
    fun list(
        @RequestParam(required = false) search: String?,
        @RequestParam(required = false) page: Int?,
        @RequestParam(required = false) size: Int?,
        @RequestParam(required = false) sort: String?,
    ): ApiResponse<PageResponse<SupplierResponse>> =
        ApiResponse.ok(supplierService.list(search, page, size, sort), "Suppliers retrieved")

    @GetMapping("/{id}")
    fun get(@PathVariable id: Long): ApiResponse<SupplierResponse> =
        ApiResponse.ok(supplierService.get(id), "Supplier retrieved")

    @PostMapping
    fun create(@Valid @RequestBody request: SupplierRequest): ApiResponse<SupplierResponse> =
        ApiResponse.ok(supplierService.create(request), "Supplier created")

    @PutMapping("/{id}")
    fun update(@PathVariable id: Long, @Valid @RequestBody request: SupplierRequest): ApiResponse<SupplierResponse> =
        ApiResponse.ok(supplierService.update(id, request), "Supplier updated")

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): ApiResponse<Unit> {
        supplierService.delete(id)
        return ApiResponse.ok("Supplier deactivated")
    }
}
