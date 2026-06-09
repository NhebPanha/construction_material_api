package org.example.construction_material_api.sales.service

import org.example.construction_material_api.sales.model.*
import org.example.construction_material_api.sales.repository.*
import org.example.construction_material_api.sales.dto.*

import org.example.construction_material_api.common.ConflictException
import org.example.construction_material_api.common.NotFoundException
import org.example.construction_material_api.common.PageResponse
import org.example.construction_material_api.common.Paging
import org.example.construction_material_api.common.SaleStatus
import org.example.construction_material_api.common.toLongId
import org.example.construction_material_api.customer.model.Customer
import org.example.construction_material_api.customer.repository.CustomerRepository
import org.example.construction_material_api.inventory.service.InventoryService
import org.example.construction_material_api.product.model.Product
import org.example.construction_material_api.product.repository.ProductRepository
import org.example.construction_material_api.user.repository.UserRepository
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.UUID

@Service
class SaleService(
    private val saleRepository: SaleRepository,
    private val productRepository: ProductRepository,
    private val customerRepository: CustomerRepository,
    private val inventoryService: InventoryService,
    private val userRepository: UserRepository,
) {

    /** Resolves the cashier's display name from their login email, falling back to the email. */
    fun cashierNameForEmail(email: String): String =
        userRepository.findByEmail(email)?.name ?: email

    @Transactional(readOnly = true)
    fun list(status: SaleStatus?, q: String?, page: Int?, pageSize: Int?): PageResponse<SaleResponse> =
        PageResponse.from(
            saleRepository.search(status, q, Paging.of(page, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"))),
        ) { it.toResponse() }

    @Transactional(readOnly = true)
    fun get(id: Long): SaleResponse = findOrThrow(id).toResponse()

    /** Completes a sale: builds it, deducts stock per line, and persists â€” all in one transaction. */
    @Transactional
    fun complete(request: SaleRequest, cashierName: String): SaleResponse {
        val sale = buildSale(request, cashierName, SaleStatus.completed)
        // Deduct stock for each line; insufficient stock rolls back the whole sale.
        for (line in sale.lines) {
            val productId = line.product.id ?: throw NotFoundException("Line product not found")
            inventoryService.deductForSale(productId, line.quantity, "Sale ${sale.invoiceNumber}")
        }
        return persistWithInvoice(sale).toResponse()
    }

    /** Parks a draft sale (status=held). No stock is deducted. */
    @Transactional
    fun hold(request: SaleRequest, cashierName: String): SaleResponse {
        val sale = buildSale(request, cashierName, SaleStatus.held)
        return persistWithInvoice(sale).toResponse()
    }

    @Transactional
    fun refund(id: Long): SaleResponse {
        val sale = findOrThrow(id)
        if (sale.status != SaleStatus.completed) {
            throw ConflictException("Only completed sales can be refunded (current: ${sale.status})")
        }
        // Restock each line, then mark refunded.
        for (line in sale.lines) {
            val productId = line.product.id ?: continue
            inventoryService.restock(productId, line.quantity, "Refund ${sale.invoiceNumber}")
        }
        sale.status = SaleStatus.refunded
        return saleRepository.save(sale).toResponse()
    }

    private fun buildSale(request: SaleRequest, cashierName: String, status: SaleStatus): Sale {
        val customer: Customer? = request.customerId?.takeIf { it.isNotBlank() }?.let {
            customerRepository.findById(it.toLongId())
                .orElseThrow { NotFoundException("Customer $it not found") }
        }
        val sale = Sale(
            invoiceNumber = "PENDING-${UUID.randomUUID()}",
            customer = customer,
            discount = request.discount,
            taxRate = request.taxRate,
            paymentMethod = request.paymentMethod,
            amountReceived = request.amountReceived,
            status = status,
            cashierName = cashierName,
        )
        for (lineReq in request.lines) {
            val product = resolveProduct(lineReq.productId.toLongId())
            sale.lines.add(
                SaleLine(
                    sale = sale,
                    product = product,
                    productName = product.name,
                    quantity = lineReq.quantity,
                    unitPrice = product.sellingPrice,
                    lineDiscount = lineReq.lineDiscount,
                ),
            )
        }
        recomputeTotals(sale)
        return sale
    }

    /** Persists, then assigns a monotonic invoice number derived from the generated id. */
    private fun persistWithInvoice(sale: Sale): Sale {
        val saved = saleRepository.save(sale)
        saved.invoiceNumber = "INV-${100000 + (saved.id ?: 0)}"
        return saleRepository.save(saved)
    }

    /** Server-authoritative totals: client-supplied totals are never trusted. */
    private fun recomputeTotals(sale: Sale) {
        val subtotal = sale.lines.fold(BigDecimal.ZERO) { acc, line ->
            acc.add(line.unitPrice.multiply(BigDecimal(line.quantity)).subtract(line.lineDiscount))
        }.setScale(2, RoundingMode.HALF_UP).max(BigDecimal.ZERO)
        val taxable = subtotal.subtract(sale.discount).max(BigDecimal.ZERO)
        val tax = taxable.multiply(sale.taxRate).setScale(2, RoundingMode.HALF_UP)
        val grandTotal = taxable.add(tax).setScale(2, RoundingMode.HALF_UP)
        sale.subtotal = subtotal
        sale.tax = tax
        sale.grandTotal = grandTotal
        sale.changeDue = sale.amountReceived.subtract(grandTotal).setScale(2, RoundingMode.HALF_UP)
    }

    private fun resolveProduct(id: Long): Product =
        productRepository.findById(id).orElseThrow { NotFoundException("Product $id not found") }

    private fun findOrThrow(id: Long): Sale =
        saleRepository.findById(id).orElseThrow { NotFoundException("Sale $id not found") }
}
