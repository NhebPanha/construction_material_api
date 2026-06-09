package org.example.construction_material_api.customer.controller

import org.example.construction_material_api.customer.model.*
import org.example.construction_material_api.customer.repository.*
import org.example.construction_material_api.customer.dto.*
import org.example.construction_material_api.customer.service.*

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
@RequestMapping("/api/v1/customers")
class CustomerController(private val customerService: CustomerService) {

    @GetMapping
    fun list(
        @RequestParam(required = false) q: String?,
        @RequestParam(required = false) page: Int?,
        @RequestParam(required = false) pageSize: Int?,
    ): ApiResponse<PageResponse<CustomerResponse>> =
        ApiResponse.ok(customerService.list(q, page, pageSize), "Customers retrieved")

    @GetMapping("/{id}")
    fun get(@PathVariable id: String): ApiResponse<CustomerResponse> =
        ApiResponse.ok(customerService.get(id.toLongId()), "Customer retrieved")

    @PostMapping
    @PreAuthorize("hasAnyRole('admin','cashier')")
    fun create(@Valid @RequestBody request: CustomerRequest): ApiResponse<CustomerResponse> =
        ApiResponse.ok(customerService.create(request), "Customer created")

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('admin','cashier')")
    fun update(@PathVariable id: String, @Valid @RequestBody request: CustomerRequest): ApiResponse<CustomerResponse> =
        ApiResponse.ok(customerService.update(id.toLongId(), request), "Customer updated")

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    fun delete(@PathVariable id: String): ApiResponse<Unit> {
        customerService.delete(id.toLongId())
        return ApiResponse.ok("Customer deleted")
    }
}
