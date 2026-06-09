package org.example.construction_material_api.sales

import org.example.construction_material_api.common.ApiException
import org.example.construction_material_api.common.ErrorCode
import org.example.construction_material_api.common.PaymentMethod
import org.example.construction_material_api.common.ProductCategory
import org.example.construction_material_api.common.SaleStatus
import org.example.construction_material_api.product.model.Product
import org.example.construction_material_api.product.repository.ProductRepository
import org.example.construction_material_api.sales.dto.SaleLineRequest
import org.example.construction_material_api.sales.dto.SaleRequest
import org.example.construction_material_api.sales.service.SaleService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.math.BigDecimal

@SpringBootTest
@ActiveProfiles("test")
class SaleServiceTest(
    @Autowired private val saleService: SaleService,
    @Autowired private val productRepository: ProductRepository,
) {

    private fun newProduct(stock: Int, price: String): Product =
        productRepository.save(
            Product(
                name = "Test Product",
                category = ProductCategory.cement,
                costPrice = BigDecimal("1.00"),
                sellingPrice = BigDecimal(price),
                stockQuantity = stock,
                lowStockThreshold = 5,
                unit = "bag",
            ),
        )

    @Test
    fun `completing a sale computes server-authoritative totals and deducts stock`() {
        val product = newProduct(stock = 100, price = "10.00")

        val request = SaleRequest(
            lines = listOf(SaleLineRequest(productId = product.id.toString(), quantity = 5)),
            discount = BigDecimal("3.00"),
            taxRate = BigDecimal("0.10"),
            paymentMethod = PaymentMethod.cash,
            amountReceived = BigDecimal("100.00"),
        )

        val sale = saleService.complete(request, "Tester")

        // subtotal = 5*10 = 50; taxable = 50-3 = 47; tax = 4.70; grandTotal = 51.70; change = 48.30
        assertEquals(SaleStatus.completed, sale.status)
        assertEquals(0, BigDecimal("50.00").compareTo(sale.subtotal))
        assertEquals(0, BigDecimal("4.70").compareTo(sale.tax))
        assertEquals(0, BigDecimal("51.70").compareTo(sale.grandTotal))
        assertEquals(0, BigDecimal("48.30").compareTo(sale.change))
        assertTrue(sale.invoiceNumber.startsWith("INV-"))

        // Stock deducted 100 -> 95.
        assertEquals(95, productRepository.findById(product.id!!).get().stockQuantity)
    }

    @Test
    fun `completing a sale with insufficient stock fails and leaves stock unchanged`() {
        val product = newProduct(stock = 4, price = "10.00")

        val request = SaleRequest(
            lines = listOf(SaleLineRequest(productId = product.id.toString(), quantity = 10)),
            paymentMethod = PaymentMethod.cash,
            amountReceived = BigDecimal.ZERO,
        )

        val ex = org.junit.jupiter.api.assertThrows<ApiException> {
            saleService.complete(request, "Tester")
        }
        assertEquals(ErrorCode.CONFLICT, ex.errorCode)

        // The whole sale rolled back: stock is unchanged.
        assertEquals(4, productRepository.findById(product.id!!).get().stockQuantity)
    }
}
