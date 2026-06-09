package org.example.construction_material_api.report.service

import org.example.construction_material_api.report.dto.*

import org.example.construction_material_api.common.SaleStatus
import org.example.construction_material_api.product.repository.ProductRepository
import org.example.construction_material_api.product.dto.toResponse
import org.example.construction_material_api.sales.repository.SaleRepository
import org.example.construction_material_api.sales.dto.toResponse
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale

@Service
class ReportService(
    private val saleRepository: SaleRepository,
    private val productRepository: ProductRepository,
) {

    private val zone: ZoneId = ZoneId.systemDefault()

    @Transactional(readOnly = true)
    fun rangeReport(range: String?): RangeReportResponse {
        val normalized = (range ?: "daily").lowercase()
        val since = startInstantFor(normalized)
        val agg = saleRepository.revenueAndCostSince(since).firstOrNull()
        val revenue = (agg?.get(0) as? BigDecimal) ?: BigDecimal.ZERO
        val cost = (agg?.get(1) as? BigDecimal) ?: BigDecimal.ZERO
        val orders = saleRepository.countByStatusAndCreatedAtGreaterThanEqual(SaleStatus.completed, since)
        val topProducts = saleRepository.topProductsSince(since, PageRequest.of(0, 5)).map { row ->
            TopProduct(
                name = row[1] as String,
                quantitySold = (row[2] as Number).toLong(),
                revenue = row[3] as BigDecimal,
            )
        }
        return RangeReportResponse(
            range = normalized,
            revenue = revenue,
            cost = cost,
            profit = revenue.subtract(cost),
            orders = orders,
            topProducts = topProducts,
        )
    }

    @Transactional(readOnly = true)
    fun dashboard(): ReportDashboardResponse {
        val startOfToday = LocalDate.now(zone).atStartOfDay(zone).toInstant()
        val recentSales = saleRepository
            .findByStatusOrderByCreatedAtDesc(SaleStatus.completed, PageRequest.of(0, 5))
            .map { it.toResponse() }
        val lowStockProducts = productRepository.findLowStock(PageRequest.of(0, 10)).map { it.toResponse() }
        return ReportDashboardResponse(
            todaysSales = saleRepository.completedRevenueSince(startOfToday),
            totalOrders = saleRepository.countByStatus(SaleStatus.completed),
            totalRevenue = saleRepository.totalForStatus(SaleStatus.completed),
            lowStockCount = productRepository.countLowStock(),
            weeklySales = weeklySales(),
            recentSales = recentSales,
            lowStockProducts = lowStockProducts,
        )
    }

    /** Revenue per day for the last 7 days (oldest first), labelled by weekday. */
    private fun weeklySales(): List<DaySales> {
        val today = LocalDate.now(zone)
        return (6 downTo 0).map { offset ->
            val day = today.minusDays(offset.toLong())
            val start = day.atStartOfDay(zone).toInstant()
            val end = day.plusDays(1).atStartOfDay(zone).toInstant()
            DaySales(
                label = day.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.ENGLISH),
                total = saleRepository.completedRevenueBetween(start, end),
            )
        }
    }

    private fun startInstantFor(range: String): Instant {
        val today = LocalDate.now(zone)
        val startDate = when (range) {
            "daily" -> today
            "weekly" -> today.minusDays(6)
            "monthly" -> today.minusDays(29)
            "yearly" -> today.minusDays(364)
            else -> today
        }
        return startDate.atStartOfDay(zone).toInstant()
    }
}
