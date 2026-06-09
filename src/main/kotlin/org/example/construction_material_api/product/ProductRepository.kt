package org.example.construction_material_api.product

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface ProductRepository : JpaRepository<Product, Long> {

    fun existsBySku(sku: String): Boolean

    @Query(
        """
        SELECT p FROM Product p
        WHERE (:search IS NULL OR :search = ''
            OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(p.category) LIKE LOWER(CONCAT('%', :search, '%')))
        """,
    )
    fun search(@Param("search") search: String?, pageable: Pageable): Page<Product>

    fun countByActiveTrue(): Long

    /**
     * Returns one row per active product as `[reorderLevel, totalQuantityOnHand]`, used
     * by the dashboard to count low/out-of-stock products without loading every entity.
     */
    @Query(
        """
        SELECT p.reorderLevel,
               COALESCE((SELECT SUM(s.quantity) FROM StockLevel s WHERE s.product = p), 0)
        FROM Product p
        WHERE p.active = true
        """,
    )
    fun stockSummaries(): List<Array<Any>>
}
