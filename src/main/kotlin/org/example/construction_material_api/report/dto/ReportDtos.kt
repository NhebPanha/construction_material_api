package org.example.construction_material_api.report.dto

import org.example.construction_material_api.report.service.*

import org.example.construction_material_api.product.dto.ProductResponse
import org.example.construction_material_api.sales.dto.SaleResponse
import java.math.BigDecimal

data class TopProduct(
    val name: String,
    val quantitySold: Long,
    val revenue: BigDecimal,
)

/** GET /reports?range=... */
data class RangeReportResponse(
    val range: String,
    val revenue: BigDecimal,
    val cost: BigDecimal,
    val profit: BigDecimal,
    val orders: Long,
    val topProducts: List<TopProduct>,
)

data class DaySales(
    val label: String,
    val total: BigDecimal,
)

/** GET /reports/dashboard */
data class ReportDashboardResponse(
    val todaysSales: BigDecimal,
    val totalOrders: Long,
    val totalRevenue: BigDecimal,
    val lowStockCount: Long,
    val weeklySales: List<DaySales>,
    val recentSales: List<SaleResponse>,
    val lowStockProducts: List<ProductResponse>,
)
