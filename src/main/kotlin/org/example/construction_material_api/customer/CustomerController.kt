package org.example.construction_material_api.customer

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
@RequestMapping("/api/v1/customers")
class CustomerController(private val customerService: CustomerService) {

    @GetMapping
    fun list(
        @RequestParam(required = false) search: String?,
        @RequestParam(required = false) page: Int?,
        @RequestParam(required = false) size: Int?,
        @RequestParam(required = false) sort: String?,
    ): ApiResponse<PageResponse<CustomerResponse>> =
        ApiResponse.ok(customerService.list(search, page, size, sort), "Customers retrieved")

    @GetMapping("/{id}")
    fun get(@PathVariable id: Long): ApiResponse<CustomerResponse> =
        ApiResponse.ok(customerService.get(id), "Customer retrieved")

    @PostMapping
    fun create(@Valid @RequestBody request: CustomerRequest): ApiResponse<CustomerResponse> =
        ApiResponse.ok(customerService.create(request), "Customer created")

    @PutMapping("/{id}")
    fun update(@PathVariable id: Long, @Valid @RequestBody request: CustomerRequest): ApiResponse<CustomerResponse> =
        ApiResponse.ok(customerService.update(id, request), "Customer updated")

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): ApiResponse<Unit> {
        customerService.delete(id)
        return ApiResponse.ok("Customer deactivated")
    }
}
