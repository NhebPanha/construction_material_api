package org.example.construction_material_api.inventory

import jakarta.persistence.LockModeType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface StockLevelRepository : JpaRepository<StockLevel, Long> {

    /**
     * Loads the stock row with a pessimistic write lock so concurrent sales cannot
     * both read the same quantity and over-deduct.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM StockLevel s WHERE s.product.id = :productId AND s.warehouse.id = :warehouseId")
    fun findForUpdate(
        @Param("productId") productId: Long,
        @Param("warehouseId") warehouseId: Long,
    ): StockLevel?

    @Query("SELECT s FROM StockLevel s WHERE s.product.id = :productId AND s.warehouse.id = :warehouseId")
    fun find(
        @Param("productId") productId: Long,
        @Param("warehouseId") warehouseId: Long,
    ): StockLevel?

    @Query("SELECT COALESCE(SUM(s.quantity), 0) FROM StockLevel s WHERE s.product.id = :productId")
    fun totalQuantityForProduct(@Param("productId") productId: Long): Int

    fun findByProductId(productId: Long): List<StockLevel>
}

interface InventoryMovementRepository : JpaRepository<InventoryMovement, Long> {

    @Query(
        """
        SELECT m FROM InventoryMovement m
        WHERE (:productId IS NULL OR m.product.id = :productId)
          AND (:warehouseId IS NULL OR m.warehouse.id = :warehouseId)
        ORDER BY m.createdAt DESC
        """,
    )
    fun findFiltered(
        @Param("productId") productId: Long?,
        @Param("warehouseId") warehouseId: Long?,
        pageable: Pageable,
    ): Page<InventoryMovement>
}
