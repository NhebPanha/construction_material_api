package org.example.construction_material_api.product

import org.example.construction_material_api.common.DuplicateResourceException
import org.example.construction_material_api.common.NotFoundException
import org.example.construction_material_api.common.PageResponse
import org.example.construction_material_api.common.Paging
import org.example.construction_material_api.inventory.InventoryService
import org.example.construction_material_api.inventory.StockStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProductService(
    private val productRepository: ProductRepository,
    private val inventoryService: InventoryService,
) {

    @Transactional(readOnly = true)
    fun list(search: String?, page: Int?, size: Int?, sort: String?): PageResponse<ProductResponse> {
        val result = productRepository.search(search, Paging.of(page, size, sort ?: "name,asc"))
        return PageResponse.from(result) { toResponse(it) }
    }

    @Transactional(readOnly = true)
    fun get(id: Long): ProductResponse = toResponse(findOrThrow(id))

    @Transactional
    fun create(request: ProductRequest): ProductResponse {
        if (productRepository.existsBySku(request.sku)) {
            throw DuplicateResourceException("Product with SKU '${request.sku}' already exists")
        }
        val product = Product(
            sku = request.sku,
            name = request.name,
            description = request.description,
            unit = request.unit,
            category = request.category,
            price = request.price,
            costPrice = request.costPrice,
            reorderLevel = request.reorderLevel,
            active = request.active,
        )
        return toResponse(productRepository.save(product))
    }

    @Transactional
    fun update(id: Long, request: ProductRequest): ProductResponse {
        val product = findOrThrow(id)
        if (request.sku != product.sku && productRepository.existsBySku(request.sku)) {
            throw DuplicateResourceException("Product with SKU '${request.sku}' already exists")
        }
        product.apply {
            sku = request.sku
            name = request.name
            description = request.description
            unit = request.unit
            category = request.category
            price = request.price
            costPrice = request.costPrice
            reorderLevel = request.reorderLevel
            active = request.active
        }
        return toResponse(productRepository.save(product))
    }

    @Transactional
    fun delete(id: Long) {
        val product = findOrThrow(id)
        // Soft delete to preserve historical references from sales and movements.
        product.active = false
        productRepository.save(product)
    }

    private fun findOrThrow(id: Long): Product =
        productRepository.findById(id).orElseThrow { NotFoundException("Product $id not found") }

    private fun toResponse(product: Product): ProductResponse {
        val onHand = inventoryService.totalOnHand(product.id ?: 0)
        return ProductResponse(
            id = product.id ?: 0,
            sku = product.sku,
            name = product.name,
            description = product.description,
            unit = product.unit,
            category = product.category,
            price = product.price,
            costPrice = product.costPrice,
            reorderLevel = product.reorderLevel,
            active = product.active,
            stockOnHand = onHand,
            stockStatus = StockStatus.of(onHand, product.reorderLevel),
        )
    }
}
