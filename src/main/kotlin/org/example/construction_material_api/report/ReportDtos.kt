package org.example.construction_material_api.report

import java.math.BigDecimal

data class DashboardResponse(
    val activeProducts: Long,
    val lowStockProducts: Long,
    val outOfStockProducts: Long,
    val heldOrders: Long,
    val confirmedOrders: Long,
    val pendingDeliveries: Long,
    val totalSales: BigDecimal,
    val salesLast7Days: BigDecimal,
    val topSellingProducts: List<TopProduct>,
)

data class TopProduct(
    val productId: Long,
    val sku: String,
    val name: String,
    val quantitySold: Long,
    val revenue: BigDecimal,
)
