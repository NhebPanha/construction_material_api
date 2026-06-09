package org.example.construction_material_api.sales

import org.example.construction_material_api.common.InvalidOrderStateException
import org.example.construction_material_api.common.NotFoundException
import org.example.construction_material_api.common.PageResponse
import org.example.construction_material_api.common.Paging
import org.example.construction_material_api.customer.Customer
import org.example.construction_material_api.customer.CustomerRepository
import org.example.construction_material_api.inventory.InventoryService
import org.example.construction_material_api.product.Product
import org.example.construction_material_api.product.ProductRepository
import org.example.construction_material_api.warehouse.Warehouse
import org.example.construction_material_api.warehouse.WarehouseRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.UUID

@Service
class SalesService(
    private val salesOrderRepository: SalesOrderRepository,
    private val productRepository: ProductRepository,
    private val customerRepository: CustomerRepository,
    private val warehouseRepository: WarehouseRepository,
    private val inventoryService: InventoryService,
    @param:Value("\${app.inventory.default-warehouse-code:MAIN}") private val defaultWarehouseCode: String,
) {

    @Transactional(readOnly = true)
    fun list(status: SalesOrderStatus?, search: String?, page: Int?, size: Int?, sort: String?): PageResponse<SalesOrderResponse> =
        PageResponse.from(
            salesOrderRepository.search(status, search, Paging.of(page, size, sort ?: "createdAt,desc")),
        ) { it.toResponse() }

    @Transactional(readOnly = true)
    fun get(id: Long): SalesOrderResponse = findOrThrow(id).toResponse()

    @Transactional(readOnly = true)
    fun listHolds(search: String?, page: Int?, size: Int?): PageResponse<SalesOrderResponse> =
        list(SalesOrderStatus.HELD, search, page, size, null)

    @Transactional
    fun create(request: SalesOrderRequest): SalesOrderResponse {
        val warehouse = resolveWarehouse(request.warehouseCode)
        val customer = request.customerId?.let { resolveCustomer(it) }

        val order = SalesOrder(
            orderNumber = "PENDING-${UUID.randomUUID()}",
            customer = customer,
            warehouse = warehouse,
            status = if (request.hold) SalesOrderStatus.HELD else SalesOrderStatus.DRAFT,
            discount = request.discount,
            taxRate = request.taxRate,
            note = request.note,
        )
        applyLines(order, request.lines)
        recomputeTotals(order)

        // Persist once to obtain an id, then derive a human-friendly order number from it.
        val saved = salesOrderRepository.save(order)
        saved.orderNumber = "SO-%06d".format(saved.id)
        return salesOrderRepository.save(saved).toResponse()
    }

    @Transactional
    fun update(id: Long, request: SalesOrderRequest): SalesOrderResponse {
        val order = findOrThrow(id)
        if (order.status != SalesOrderStatus.DRAFT && order.status != SalesOrderStatus.HELD) {
            throw InvalidOrderStateException("Only draft or held orders can be edited (current: ${order.status})")
        }
        order.customer = request.customerId?.let { resolveCustomer(it) }
        order.warehouse = resolveWarehouse(request.warehouseCode)
        order.discount = request.discount
        order.taxRate = request.taxRate
        order.note = request.note
        order.status = if (request.hold) SalesOrderStatus.HELD else SalesOrderStatus.DRAFT
        order.lines.clear()
        applyLines(order, request.lines)
        recomputeTotals(order)
        return salesOrderRepository.save(order).toResponse()
    }

    /**
     * Commits an order: deducts stock for every line from the order's warehouse within a
     * single transaction. If any line lacks sufficient stock the whole operation rolls
     * back and no inventory is changed.
     */
    @Transactional
    fun confirm(id: Long): SalesOrderResponse {
        val order = findOrThrow(id)
        if (order.status != SalesOrderStatus.DRAFT && order.status != SalesOrderStatus.HELD) {
            throw InvalidOrderStateException("Only draft or held orders can be confirmed (current: ${order.status})")
        }
        val warehouseId = order.warehouse.id ?: throw NotFoundException("Order warehouse not found")
        for (line in order.lines) {
            val productId = line.product.id ?: throw NotFoundException("Line product not found")
            inventoryService.deduct(productId, warehouseId, line.quantity, order.orderNumber, "Sale ${order.orderNumber}")
        }
        order.status = SalesOrderStatus.CONFIRMED
        order.confirmedAt = java.time.Instant.now()
        return salesOrderRepository.save(order).toResponse()
    }

    @Transactional
    fun cancel(id: Long): SalesOrderResponse {
        val order = findOrThrow(id)
        if (order.status == SalesOrderStatus.CONFIRMED) {
            throw InvalidOrderStateException("Confirmed orders cannot be cancelled")
        }
        if (order.status == SalesOrderStatus.CANCELLED) {
            throw InvalidOrderStateException("Order is already cancelled")
        }
        order.status = SalesOrderStatus.CANCELLED
        return salesOrderRepository.save(order).toResponse()
    }

    private fun applyLines(order: SalesOrder, lineRequests: List<SalesOrderLineRequest>) {
        for (req in lineRequests) {
            val product = resolveProduct(req.productId)
            val unitPrice = product.price
            val lineTotal = unitPrice.multiply(BigDecimal(req.quantity)).setScale(2, RoundingMode.HALF_UP)
            order.lines.add(
                SalesOrderLine(
                    order = order,
                    product = product,
                    quantity = req.quantity,
                    unitPrice = unitPrice,
                    lineTotal = lineTotal,
                ),
            )
        }
    }

    /** Recomputes all monetary totals on the server; client-supplied totals are never trusted. */
    private fun recomputeTotals(order: SalesOrder) {
        val subtotal = order.lines.fold(BigDecimal.ZERO) { acc, line -> acc.add(line.lineTotal) }
            .setScale(2, RoundingMode.HALF_UP)
        val discount = order.discount.coerceAtMost(subtotal).setScale(2, RoundingMode.HALF_UP)
        val taxable = subtotal.subtract(discount).max(BigDecimal.ZERO)
        val taxAmount = taxable.multiply(order.taxRate)
            .divide(BigDecimal(100), 2, RoundingMode.HALF_UP)
        order.subtotal = subtotal
        order.discount = discount
        order.taxAmount = taxAmount
        order.total = taxable.add(taxAmount).setScale(2, RoundingMode.HALF_UP)
    }

    private fun resolveProduct(id: Long): Product =
        productRepository.findById(id).orElseThrow { NotFoundException("Product $id not found") }

    private fun resolveCustomer(id: Long): Customer =
        customerRepository.findById(id).orElseThrow { NotFoundException("Customer $id not found") }

    private fun resolveWarehouse(code: String?): Warehouse {
        val target = code ?: defaultWarehouseCode
        return warehouseRepository.findByCode(target)
            ?: throw NotFoundException("Warehouse '$target' not found")
    }

    private fun findOrThrow(id: Long): SalesOrder =
        salesOrderRepository.findById(id).orElseThrow { NotFoundException("Sales order $id not found") }
}

fun SalesOrder.toResponse(): SalesOrderResponse = SalesOrderResponse(
    id = id ?: 0,
    orderNumber = orderNumber,
    customerId = customer?.id,
    customerName = customer?.name,
    warehouseId = warehouse.id ?: 0,
    warehouseCode = warehouse.code,
    status = status,
    lines = lines.map {
        SalesOrderLineResponse(
            productId = it.product.id ?: 0,
            productSku = it.product.sku,
            productName = it.product.name,
            quantity = it.quantity,
            unitPrice = it.unitPrice,
            lineTotal = it.lineTotal,
        )
    },
    subtotal = subtotal,
    discount = discount,
    taxRate = taxRate,
    taxAmount = taxAmount,
    total = total,
    note = note,
    confirmedAt = confirmedAt,
    createdAt = createdAt,
)
