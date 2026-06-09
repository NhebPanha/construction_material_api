package org.example.construction_material_api.product.repository

import org.example.construction_material_api.product.model.*
import org.example.construction_material_api.product.dto.*
import org.example.construction_material_api.product.service.*

import jakarta.persistence.LockModeType
import org.example.construction_material_api.common.ProductCategory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface ProductRepository : JpaRepository<Product, Long> {

    fun existsByBarcode(barcode: String): Boolean

    /** Loads a product with a write lock so concurrent stock changes serialize. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id = :id")
    fun findForUpdate(@Param("id") id: Long): Product?

    @Query(
        """
        SELECT p FROM Product p
        WHERE (:q IS NULL OR :q = ''
            OR LOWER(p.name) LIKE LOWER(CONCAT('%', :q, '%'))
            OR LOWER(p.barcode) LIKE LOWER(CONCAT('%', :q, '%')))
          AND (:category IS NULL OR p.category = :category)
          AND (:lowStock = false OR p.stockQuantity <= p.lowStockThreshold)
        """,
    )
    fun search(
        @Param("q") q: String?,
        @Param("category") category: ProductCategory?,
        @Param("lowStock") lowStock: Boolean,
        pageable: Pageable,
    ): Page<Product>

    fun countByStockQuantityLessThanEqual(threshold: Int): Long

    @Query("SELECT p FROM Product p WHERE p.stockQuantity <= p.lowStockThreshold ORDER BY p.stockQuantity ASC")
    fun findLowStock(pageable: Pageable): List<Product>

    @Query("SELECT COUNT(p) FROM Product p WHERE p.stockQuantity <= p.lowStockThreshold")
    fun countLowStock(): Long

    /** Returns `[totalUnits, stockCostValue, stockRetailValue]` aggregated across all products. */
    @Query(
        """
        SELECT COALESCE(SUM(p.stockQuantity), 0),
               COALESCE(SUM(p.stockQuantity * p.costPrice), 0),
               COALESCE(SUM(p.stockQuantity * p.sellingPrice), 0)
        FROM Product p
        """,
    )
    fun stockAggregates(): List<Array<Any>>
}
