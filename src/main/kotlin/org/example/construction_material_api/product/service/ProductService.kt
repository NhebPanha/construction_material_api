package org.example.construction_material_api.product.service

import org.example.construction_material_api.product.model.*
import org.example.construction_material_api.product.repository.*
import org.example.construction_material_api.product.dto.*

import org.example.construction_material_api.common.ConflictException
import org.example.construction_material_api.common.NotFoundException
import org.example.construction_material_api.common.PageResponse
import org.example.construction_material_api.common.Paging
import org.example.construction_material_api.common.ProductCategory
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProductService(private val productRepository: ProductRepository) {

    @Transactional(readOnly = true)
    fun list(q: String?, category: ProductCategory?, lowStock: Boolean, page: Int?, pageSize: Int?): PageResponse<ProductResponse> {
        val pageable = Paging.of(page, pageSize, Sort.by(Sort.Direction.ASC, "name"))
        return PageResponse.from(productRepository.search(q, category, lowStock, pageable)) { it.toResponse() }
    }

    @Transactional(readOnly = true)
    fun get(id: Long): ProductResponse = findOrThrow(id).toResponse()

    @Transactional
    fun create(request: ProductRequest): ProductResponse {
        if (!request.barcode.isNullOrBlank() && productRepository.existsByBarcode(request.barcode)) {
            throw ConflictException("Product with barcode '${request.barcode}' already exists")
        }
        val product = Product(
            name = request.name,
            category = request.category,
            costPrice = request.costPrice,
            sellingPrice = request.sellingPrice,
            stockQuantity = request.stockQuantity,
            lowStockThreshold = request.lowStockThreshold,
            unit = request.unit,
            description = request.description,
            imageUrl = request.imageUrl,
            barcode = request.barcode,
        )
        return productRepository.save(product).toResponse()
    }

    @Transactional
    fun update(id: Long, request: ProductRequest): ProductResponse {
        val product = findOrThrow(id)
        if (!request.barcode.isNullOrBlank() && request.barcode != product.barcode &&
            productRepository.existsByBarcode(request.barcode)
        ) {
            throw ConflictException("Product with barcode '${request.barcode}' already exists")
        }
        product.apply {
            name = request.name
            category = request.category
            costPrice = request.costPrice
            sellingPrice = request.sellingPrice
            stockQuantity = request.stockQuantity
            lowStockThreshold = request.lowStockThreshold
            unit = request.unit
            description = request.description
            imageUrl = request.imageUrl
            barcode = request.barcode
        }
        return productRepository.save(product).toResponse()
    }

    @Transactional
    fun delete(id: Long) {
        val product = findOrThrow(id)
        productRepository.delete(product)
    }

    private fun findOrThrow(id: Long): Product =
        productRepository.findById(id).orElseThrow { NotFoundException("Product $id not found") }
}
