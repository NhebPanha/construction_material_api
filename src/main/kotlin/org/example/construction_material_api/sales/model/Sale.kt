package org.example.construction_material_api.sales.model

import org.example.construction_material_api.sales.repository.*
import org.example.construction_material_api.sales.dto.*
import org.example.construction_material_api.sales.service.*

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.example.construction_material_api.common.BaseEntity
import org.example.construction_material_api.common.PaymentMethod
import org.example.construction_material_api.common.SaleStatus
import org.example.construction_material_api.customer.model.Customer
import org.example.construction_material_api.product.model.Product
import java.math.BigDecimal

@Entity
@Table(name = "sales")
class Sale(
    @Column(nullable = false, unique = true)
    var invoiceNumber: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    var customer: Customer? = null,

    @Column(nullable = false, precision = 19, scale = 2)
    var discount: BigDecimal = BigDecimal.ZERO,

    /** Tax rate as a fraction, e.g. 0.10 for 10%. */
    @Column(nullable = false, precision = 6, scale = 4)
    var taxRate: BigDecimal = BigDecimal.ZERO,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var paymentMethod: PaymentMethod = PaymentMethod.cash,

    @Column(nullable = false, precision = 19, scale = 2)
    var amountReceived: BigDecimal = BigDecimal.ZERO,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var status: SaleStatus = SaleStatus.held,

    @Column(nullable = false)
    var cashierName: String = "",
) : BaseEntity() {

    @OneToMany(mappedBy = "sale", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    var lines: MutableList<SaleLine> = mutableListOf()

    // Server-authoritative monetary totals.
    @Column(nullable = false, precision = 19, scale = 2)
    var subtotal: BigDecimal = BigDecimal.ZERO

    @Column(nullable = false, precision = 19, scale = 2)
    var tax: BigDecimal = BigDecimal.ZERO

    @Column(nullable = false, precision = 19, scale = 2)
    var grandTotal: BigDecimal = BigDecimal.ZERO

    @Column(nullable = false, precision = 19, scale = 2)
    var changeDue: BigDecimal = BigDecimal.ZERO
}

@Entity
@Table(name = "sale_lines")
class SaleLine(
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sale_id")
    var sale: Sale,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id")
    var product: Product,

    @Column(nullable = false)
    var productName: String,

    @Column(nullable = false)
    var quantity: Int,

    @Column(nullable = false, precision = 19, scale = 2)
    var unitPrice: BigDecimal,

    @Column(nullable = false, precision = 19, scale = 2)
    var lineDiscount: BigDecimal = BigDecimal.ZERO,
) : BaseEntity()
