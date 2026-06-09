package org.example.construction_material_api.sales.controller

import org.example.construction_material_api.sales.model.*
import org.example.construction_material_api.sales.repository.*
import org.example.construction_material_api.sales.dto.*
import org.example.construction_material_api.sales.service.*

import jakarta.validation.Valid
import org.example.construction_material_api.common.ApiResponse
import org.example.construction_material_api.common.PageResponse
import org.example.construction_material_api.common.SaleStatus
import org.example.construction_material_api.common.toLongId
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/sales-orders")
class SaleController(private val saleService: SaleService) {

    @GetMapping
    fun list(
        @RequestParam(required = false) status: SaleStatus?,
        @RequestParam(required = false) q: String?,
        @RequestParam(required = false) page: Int?,
        @RequestParam(required = false) pageSize: Int?,
    ): ApiResponse<PageResponse<SaleResponse>> =
        ApiResponse.ok(saleService.list(status, q, page, pageSize), "Sales retrieved")

    @GetMapping("/{id}")
    fun get(@PathVariable id: String): ApiResponse<SaleResponse> =
        ApiResponse.ok(saleService.get(id.toLongId()), "Sale retrieved")

    /** Complete a sale: generate invoice, deduct stock transactionally. */
    @PostMapping
    @PreAuthorize("hasAnyRole('admin','cashier')")
    fun complete(@Valid @RequestBody request: SaleRequest, authentication: Authentication): ApiResponse<SaleResponse> {
        val cashier = saleService.cashierNameForEmail(authentication.name)
        return ApiResponse.ok(saleService.complete(request, cashier), "Sale completed")
    }

    /** Park a draft sale (held); no stock deduction. */
    @PostMapping("/hold")
    @PreAuthorize("hasAnyRole('admin','cashier')")
    fun hold(@Valid @RequestBody request: SaleRequest, authentication: Authentication): ApiResponse<SaleResponse> {
        val cashier = saleService.cashierNameForEmail(authentication.name)
        return ApiResponse.ok(saleService.hold(request, cashier), "Sale held")
    }

    @PostMapping("/{id}/refund")
    @PreAuthorize("hasAnyRole('admin','cashier')")
    fun refund(@PathVariable id: String): ApiResponse<SaleResponse> =
        ApiResponse.ok(saleService.refund(id.toLongId()), "Sale refunded")
}
