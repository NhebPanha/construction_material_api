package org.example.construction_material_api.config

import org.example.construction_material_api.customer.Customer
import org.example.construction_material_api.customer.CustomerRepository
import org.example.construction_material_api.inventory.InventoryService
import org.example.construction_material_api.product.Product
import org.example.construction_material_api.product.ProductRepository
import org.example.construction_material_api.supplier.Supplier
import org.example.construction_material_api.supplier.SupplierRepository
import org.example.construction_material_api.user.User
import org.example.construction_material_api.user.UserRepository
import org.example.construction_material_api.user.UserRole
import org.example.construction_material_api.warehouse.Warehouse
import org.example.construction_material_api.warehouse.WarehouseRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Profile
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

/**
 * Seeds a default admin user and a small set of sample data on first start so the API
 * is immediately usable. Skipped if data already exists. Disabled under the "test" profile.
 */
@Component
@Profile("!test")
class DataSeeder(
    private val userRepository: UserRepository,
    private val productRepository: ProductRepository,
    private val customerRepository: CustomerRepository,
    private val supplierRepository: SupplierRepository,
    private val warehouseRepository: WarehouseRepository,
    private val inventoryService: InventoryService,
    private val passwordEncoder: PasswordEncoder,
) : CommandLineRunner {

    @Transactional
    override fun run(vararg args: String) {
        if (userRepository.count() == 0L) {
            userRepository.save(
                User(
                    username = "admin",
                    passwordHash = passwordEncoder.encode("admin123")!!,
                    fullName = "System Administrator",
                    role = UserRole.ADMIN,
                ),
            )
            userRepository.save(
                User(
                    username = "cashier",
                    passwordHash = passwordEncoder.encode("cashier123")!!,
                    fullName = "Front Desk Cashier",
                    role = UserRole.CASHIER,
                ),
            )
        }

        val warehouse = warehouseRepository.findByCode("MAIN")
            ?: warehouseRepository.save(Warehouse(code = "MAIN", name = "Main Warehouse", location = "HQ"))

        if (productRepository.count() == 0L) {
            val products = listOf(
                Product(sku = "CEM-50", name = "Portland Cement 50kg", unit = "BAG", category = "CEMENT",
                    price = BigDecimal("8.50"), costPrice = BigDecimal("6.00"), reorderLevel = 50),
                Product(sku = "REBAR-12", name = "Steel Rebar 12mm x 12m", unit = "PCS", category = "STEEL",
                    price = BigDecimal("12.75"), costPrice = BigDecimal("9.50"), reorderLevel = 100),
                Product(sku = "SAND-M3", name = "River Sand", unit = "M3", category = "AGGREGATE",
                    price = BigDecimal("25.00"), costPrice = BigDecimal("18.00"), reorderLevel = 20),
                Product(sku = "BRICK-STD", name = "Standard Clay Brick", unit = "PCS", category = "MASONRY",
                    price = BigDecimal("0.35"), costPrice = BigDecimal("0.22"), reorderLevel = 1000),
            )
            val saved = productRepository.saveAll(products)
            val quantities = listOf(200, 500, 40, 8000)
            saved.forEachIndexed { index, product ->
                inventoryService.receive(
                    product.id!!, warehouse.id!!, quantities[index], "SEED", "Opening stock",
                )
            }
        }

        if (customerRepository.count() == 0L) {
            customerRepository.saveAll(
                listOf(
                    Customer(name = "Acme Builders Ltd", phone = "+85512345678", email = "orders@acme.example",
                        creditLimit = BigDecimal("10000.00")),
                    Customer(name = "Walk-in Customer"),
                ),
            )
        }

        if (supplierRepository.count() == 0L) {
            supplierRepository.save(
                Supplier(name = "National Cement Co", phone = "+85598765432", contactPerson = "Sok Dara"),
            )
        }
    }
}
