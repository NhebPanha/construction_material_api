package org.example.construction_material_api.sales

import jakarta.validation.Valid
import org.example.construction_material_api.common.ApiResponse
import org.example.construction_material_api.common.PageResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/sales-orders")
class SalesController(private val salesService: SalesService) {

    @GetMapping
    fun list(
        @RequestParam(required = false) status: SalesOrderStatus?,
        @RequestParam(required = false) search: String?,
        @RequestParam(required = false) page: Int?,
        @RequestParam(required = false) size: Int?,
        @RequestParam(required = false) sort: String?,
    ): ApiResponse<PageResponse<SalesOrderResponse>> =
        ApiResponse.ok(salesService.list(status, search, page, size, sort), "Sales orders retrieved")

    /** Held orders ("holds"). */
    @GetMapping("/holds")
    fun holds(
        @RequestParam(required = false) search: String?,
        @RequestParam(required = false) page: Int?,
        @RequestParam(required = false) size: Int?,
    ): ApiResponse<PageResponse<SalesOrderResponse>> =
        ApiResponse.ok(salesService.listHolds(search, page, size), "Held orders retrieved")

    @GetMapping("/{id}")
    fun get(@PathVariable id: Long): ApiResponse<SalesOrderResponse> =
        ApiResponse.ok(salesService.get(id), "Sales order retrieved")

    @PostMapping
    fun create(@Valid @RequestBody request: SalesOrderRequest): ApiResponse<SalesOrderResponse> =
        ApiResponse.ok(salesService.create(request), "Sales order created")

    @PutMapping("/{id}")
    fun update(@PathVariable id: Long, @Valid @RequestBody request: SalesOrderRequest): ApiResponse<SalesOrderResponse> =
        ApiResponse.ok(salesService.update(id, request), "Sales order updated")

    @PostMapping("/{id}/confirm")
    fun confirm(@PathVariable id: Long): ApiResponse<SalesOrderResponse> =
        ApiResponse.ok(salesService.confirm(id), "Sales order confirmed")

    @PostMapping("/{id}/cancel")
    fun cancel(@PathVariable id: Long): ApiResponse<SalesOrderResponse> =
        ApiResponse.ok(salesService.cancel(id), "Sales order cancelled")
}
