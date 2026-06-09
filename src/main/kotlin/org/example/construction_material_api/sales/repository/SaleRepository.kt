package org.example.construction_material_api.sales.repository

import org.example.construction_material_api.sales.model.*
import org.example.construction_material_api.sales.dto.*
import org.example.construction_material_api.sales.service.*

import org.example.construction_material_api.common.SaleStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.math.BigDecimal
import java.time.Instant

interface SaleRepository : JpaRepository<Sale, Long> {

    fun countByStatus(status: SaleStatus): Long

    fun countByStatusAndCreatedAtGreaterThanEqual(status: SaleStatus, since: Instant): Long

    @Query(
        """
        SELECT s FROM Sale s
        WHERE (:status IS NULL OR s.status = :status)
          AND (:q IS NULL OR :q = ''
            OR LOWER(s.invoiceNumber) LIKE LOWER(CONCAT('%', :q, '%'))
            OR LOWER(s.customer.name) LIKE LOWER(CONCAT('%', :q, '%')))
        """,
    )
    fun search(
        @Param("status") status: SaleStatus?,
        @Param("q") q: String?,
        pageable: Pageable,
    ): Page<Sale>

    @Query("SELECT COALESCE(SUM(s.grandTotal), 0) FROM Sale s WHERE s.status = :status")
    fun totalForStatus(@Param("status") status: SaleStatus): BigDecimal

    @Query(
        "SELECT COALESCE(SUM(s.grandTotal), 0) FROM Sale s " +
            "WHERE s.status = org.example.construction_material_api.common.SaleStatus.completed " +
            "AND s.createdAt >= :since",
    )
    fun completedRevenueSince(@Param("since") since: Instant): BigDecimal

    @Query(
        "SELECT COALESCE(SUM(s.grandTotal), 0) FROM Sale s " +
            "WHERE s.status = org.example.construction_material_api.common.SaleStatus.completed " +
            "AND s.createdAt >= :start AND s.createdAt < :end",
    )
    fun completedRevenueBetween(@Param("start") start: Instant, @Param("end") end: Instant): BigDecimal

    @Query(
        "SELECT COUNT(s) FROM Sale s " +
            "WHERE s.status = org.example.construction_material_api.common.SaleStatus.completed " +
            "AND s.createdAt >= :start AND s.createdAt < :end",
    )
    fun completedCountBetween(@Param("start") start: Instant, @Param("end") end: Instant): Long

    fun findByStatusOrderByCreatedAtDesc(status: SaleStatus, pageable: Pageable): List<Sale>

    /**
     * Cost of goods + revenue for completed sales since [since]:
     * `[revenue, cost]` where cost = Î£(line.quantity * product.costPrice).
     */
    @Query(
        """
        SELECT COALESCE(SUM(l.quantity * l.unitPrice - l.lineDiscount), 0),
               COALESCE(SUM(l.quantity * l.product.costPrice), 0)
        FROM SaleLine l
        WHERE l.sale.status = org.example.construction_material_api.common.SaleStatus.completed
          AND l.sale.createdAt >= :since
        """,
    )
    fun revenueAndCostSince(@Param("since") since: Instant): List<Array<Any>>

    @Query(
        """
        SELECT l.product.id, l.product.name, SUM(l.quantity), SUM(l.quantity * l.unitPrice - l.lineDiscount)
        FROM SaleLine l
        WHERE l.sale.status = org.example.construction_material_api.common.SaleStatus.completed
          AND l.sale.createdAt >= :since
        GROUP BY l.product.id, l.product.name
        ORDER BY SUM(l.quantity) DESC
        """,
    )
    fun topProductsSince(@Param("since") since: Instant, pageable: Pageable): List<Array<Any>>
}
