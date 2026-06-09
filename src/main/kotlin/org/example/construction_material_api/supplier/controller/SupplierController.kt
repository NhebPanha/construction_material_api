package org.example.construction_material_api.supplier.controller

import org.example.construction_material_api.supplier.model.*
import org.example.construction_material_api.supplier.repository.*
import org.example.construction_material_api.supplier.dto.*
import org.example.construction_material_api.supplier.service.*

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
@RequestMapping("/api/v1/suppliers")
class SupplierController(private val supplierService: SupplierService) {

    @GetMapping
    fun list(
        @RequestParam(required = false) q: String?,
        @RequestParam(required = false) page: Int?,
        @RequestParam(required = false) pageSize: Int?,
    ): ApiResponse<PageResponse<SupplierResponse>> =
        ApiResponse.ok(supplierService.list(q, page, pageSize), "Suppliers retrieved")

    @GetMapping("/{id}")
    fun get(@PathVariable id: String): ApiResponse<SupplierResponse> =
        ApiResponse.ok(supplierService.get(id.toLongId()), "Supplier retrieved")

    @PostMapping
    @PreAuthorize("hasAnyRole('admin','warehouse')")
    fun create(@Valid @RequestBody request: SupplierRequest): ApiResponse<SupplierResponse> =
        ApiResponse.ok(supplierService.create(request), "Supplier created")

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('admin','warehouse')")
    fun update(@PathVariable id: String, @Valid @RequestBody request: SupplierRequest): ApiResponse<SupplierResponse> =
        ApiResponse.ok(supplierService.update(id.toLongId(), request), "Supplier updated")

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    fun delete(@PathVariable id: String): ApiResponse<Unit> {
        supplierService.delete(id.toLongId())
        return ApiResponse.ok("Supplier deleted")
    }
}
