package org.example.construction_material_api.product.controller

import org.example.construction_material_api.product.model.*
import org.example.construction_material_api.product.repository.*
import org.example.construction_material_api.product.dto.*
import org.example.construction_material_api.product.service.*

import jakarta.validation.Valid
import org.example.construction_material_api.common.ApiResponse
import org.example.construction_material_api.common.PageResponse
import org.example.construction_material_api.common.ProductCategory
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
@RequestMapping("/api/v1/products")
class ProductController(private val productService: ProductService) {

    @GetMapping
    fun list(
        @RequestParam(required = false) q: String?,
        @RequestParam(required = false) category: ProductCategory?,
        @RequestParam(required = false, defaultValue = "false") lowStock: Boolean,
        @RequestParam(required = false) page: Int?,
        @RequestParam(required = false) pageSize: Int?,
    ): ApiResponse<PageResponse<ProductResponse>> =
        ApiResponse.ok(productService.list(q, category, lowStock, page, pageSize), "Products retrieved")

    @GetMapping("/{id}")
    fun get(@PathVariable id: String): ApiResponse<ProductResponse> =
        ApiResponse.ok(productService.get(id.toLongId()), "Product retrieved")

    @PostMapping
    @PreAuthorize("hasAnyRole('admin','warehouse')")
    fun create(@Valid @RequestBody request: ProductRequest): ApiResponse<ProductResponse> =
        ApiResponse.ok(productService.create(request), "Product created")

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('admin','warehouse')")
    fun update(@PathVariable id: String, @Valid @RequestBody request: ProductRequest): ApiResponse<ProductResponse> =
        ApiResponse.ok(productService.update(id.toLongId(), request), "Product updated")

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    fun delete(@PathVariable id: String): ApiResponse<Unit> {
        productService.delete(id.toLongId())
        return ApiResponse.ok("Product deleted")
    }
}
