package org.example.construction_material_api.report

import org.example.construction_material_api.delivery.DeliveryRepository
import org.example.construction_material_api.delivery.DeliveryStatus
import org.example.construction_material_api.product.ProductRepository
import org.example.construction_material_api.sales.SalesOrderRepository
import org.example.construction_material_api.sales.SalesOrderStatus
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
class ReportService(
    private val productRepository: ProductRepository,
    private val salesOrderRepository: SalesOrderRepository,
    private val deliveryRepository: DeliveryRepository,
) {

    @Transactional(readOnly = true)
    fun dashboard(): DashboardResponse {
        var lowStock = 0L
        var outOfStock = 0L
        for (row in productRepository.stockSummaries()) {
            val reorderLevel = (row[0] as Number).toInt()
            val onHand = (row[1] as Number).toInt()
            when {
                onHand <= 0 -> outOfStock++
                onHand <= reorderLevel -> lowStock++
            }
        }

        val since = Instant.now().minus(7, ChronoUnit.DAYS)
        val topProducts = salesOrderRepository.topSellingProducts(PageRequest.of(0, 5)).map { row ->
            TopProduct(
                productId = (row[0] as Number).toLong(),
                sku = row[1] as String,
                name = row[2] as String,
                quantitySold = (row[3] as Number).toLong(),
                revenue = row[4] as BigDecimal,
            )
        }

        return DashboardResponse(
            activeProducts = productRepository.countByActiveTrue(),
            lowStockProducts = lowStock,
            outOfStockProducts = outOfStock,
            heldOrders = salesOrderRepository.countByStatus(SalesOrderStatus.HELD),
            confirmedOrders = salesOrderRepository.countByStatus(SalesOrderStatus.CONFIRMED),
            pendingDeliveries = deliveryRepository.countByStatus(DeliveryStatus.PENDING),
            totalSales = salesOrderRepository.totalForStatus(SalesOrderStatus.CONFIRMED),
            salesLast7Days = salesOrderRepository.confirmedTotalSince(since),
            topSellingProducts = topProducts,
        )
    }
}
