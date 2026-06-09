package org.example.construction_material_api.sales

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.math.BigDecimal
import java.time.Instant

interface SalesOrderRepository : JpaRepository<SalesOrder, Long> {

    fun countByStatus(status: SalesOrderStatus): Long

    @Query("SELECT COALESCE(SUM(o.total), 0) FROM SalesOrder o WHERE o.status = :status")
    fun totalForStatus(@Param("status") status: SalesOrderStatus): BigDecimal

    @Query(
        "SELECT COALESCE(SUM(o.total), 0) FROM SalesOrder o " +
            "WHERE o.status = org.example.construction_material_api.sales.SalesOrderStatus.CONFIRMED " +
            "AND o.confirmedAt >= :since",
    )
    fun confirmedTotalSince(@Param("since") since: Instant): BigDecimal

    @Query(
        """
        SELECT l.product.id, l.product.sku, l.product.name, SUM(l.quantity), SUM(l.lineTotal)
        FROM SalesOrderLine l
        WHERE l.order.status = org.example.construction_material_api.sales.SalesOrderStatus.CONFIRMED
        GROUP BY l.product.id, l.product.sku, l.product.name
        ORDER BY SUM(l.quantity) DESC
        """,
    )
    fun topSellingProducts(pageable: Pageable): List<Array<Any>>

    @Query(
        """
        SELECT o FROM SalesOrder o
        WHERE (:status IS NULL OR o.status = :status)
          AND (:search IS NULL OR :search = ''
            OR LOWER(o.orderNumber) LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(o.customer.name) LIKE LOWER(CONCAT('%', :search, '%')))
        """,
    )
    fun search(
        @Param("status") status: SalesOrderStatus?,
        @Param("search") search: String?,
        pageable: Pageable,
    ): Page<SalesOrder>
}
