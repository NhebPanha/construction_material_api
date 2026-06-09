package org.example.construction_material_api.product

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
@RequestMapping("/api/v1/products")
class ProductController(private val productService: ProductService) {

    @GetMapping
    fun list(
        @RequestParam(required = false) search: String?,
        @RequestParam(required = false) page: Int?,
        @RequestParam(required = false) size: Int?,
        @RequestParam(required = false) sort: String?,
    ): ApiResponse<PageResponse<ProductResponse>> =
        ApiResponse.ok(productService.list(search, page, size, sort), "Products retrieved")

    @GetMapping("/{id}")
    fun get(@PathVariable id: Long): ApiResponse<ProductResponse> =
        ApiResponse.ok(productService.get(id), "Product retrieved")

    @PostMapping
    fun create(@Valid @RequestBody request: ProductRequest): ApiResponse<ProductResponse> =
        ApiResponse.ok(productService.create(request), "Product created")

    @PutMapping("/{id}")
    fun update(@PathVariable id: Long, @Valid @RequestBody request: ProductRequest): ApiResponse<ProductResponse> =
        ApiResponse.ok(productService.update(id, request), "Product updated")

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): ApiResponse<Unit> {
        productService.delete(id)
        return ApiResponse.ok("Product deactivated")
    }
}
