package org.example.construction_material_api.config

import org.example.construction_material_api.common.ProductCategory
import org.example.construction_material_api.common.UserRole
import org.example.construction_material_api.customer.model.Customer
import org.example.construction_material_api.customer.repository.CustomerRepository
import org.example.construction_material_api.product.model.Product
import org.example.construction_material_api.product.repository.ProductRepository
import org.example.construction_material_api.supplier.model.Supplier
import org.example.construction_material_api.supplier.repository.SupplierRepository
import org.example.construction_material_api.user.model.User
import org.example.construction_material_api.user.repository.UserRepository
import org.example.construction_material_api.warehouse.model.Warehouse
import org.example.construction_material_api.warehouse.repository.WarehouseRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

/**
 * Seeds default users and demo data on first start when `app.seed.enabled` is true and
 * the database is empty.
 */
@Component
@ConditionalOnProperty(prefix = "app.seed", name = ["enabled"], havingValue = "true", matchIfMissing = true)
class DataSeeder(
    private val userRepository: UserRepository,
    private val productRepository: ProductRepository,
    private val customerRepository: CustomerRepository,
    private val supplierRepository: SupplierRepository,
    private val warehouseRepository: WarehouseRepository,
    private val passwordEncoder: PasswordEncoder,
) : CommandLineRunner {

    @Transactional
    override fun run(vararg args: String) {
        if (userRepository.count() == 0L) {
            userRepository.saveAll(
                listOf(
                    User("System Administrator", "admin@buildpos.local", passwordEncoder.encode("admin123")!!, UserRole.admin),
                    User("Front Desk Cashier", "cashier@buildpos.local", passwordEncoder.encode("cashier123")!!, UserRole.cashier),
                    User("Warehouse Keeper", "warehouse@buildpos.local", passwordEncoder.encode("warehouse123")!!, UserRole.warehouse),
                ),
            )
        }

        if (productRepository.count() == 0L) {
            productRepository.saveAll(
                listOf(
                    Product("Portland Cement 50kg", ProductCategory.cement, BigDecimal("6.00"), BigDecimal("8.50"), 200, 50, "bag", barcode = "CEM-50"),
                    Product("Steel Rebar 12mm x 12m", ProductCategory.steel, BigDecimal("9.50"), BigDecimal("12.75"), 500, 100, "pcs", barcode = "REBAR-12"),
                    Product("River Sand", ProductCategory.sand, BigDecimal("18.00"), BigDecimal("25.00"), 40, 20, "m3", barcode = "SAND-M3"),
                    Product("Crushed Gravel 20mm", ProductCategory.gravel, BigDecimal("20.00"), BigDecimal("28.00"), 8, 15, "m3", barcode = "GRAVEL-20"),
                    Product("Standard Clay Brick", ProductCategory.brick, BigDecimal("0.22"), BigDecimal("0.35"), 8000, 1000, "pcs", barcode = "BRICK-STD"),
                    Product("Ceramic Floor Tile 60x60", ProductCategory.tile, BigDecimal("3.10"), BigDecimal("4.50"), 0, 50, "box", barcode = "TILE-6060"),
                ),
            )
        }

        if (customerRepository.count() == 0L) {
            customerRepository.saveAll(
                listOf(
                    Customer("Acme Builders Ltd", "+85512345678", "orders@acme.example", "Phnom Penh", 120, BigDecimal("250.00"), BigDecimal("0.0500")),
                    Customer("Walk-in Customer"),
                ),
            )
        }

        if (supplierRepository.count() == 0L) {
            supplierRepository.saveAll(
                listOf(
                    Supplier("National Cement Co", "+85598765432", "sales@natcement.example", "Kampot", BigDecimal("1500.00")),
                    Supplier("Mekong Steel Supply", "+85591112222", "info@mekongsteel.example", "Phnom Penh", BigDecimal("0.00")),
                ),
            )
        }

        if (warehouseRepository.count() == 0L) {
            warehouseRepository.saveAll(
                listOf(
                    Warehouse("MAIN", "Main Warehouse", "HQ Yard", capacity = 10000, used = 6500, incoming = 800, outgoing = 400, isPrimary = true),
                    Warehouse("NORTH", "North Depot", "Siem Reap", capacity = 4000, used = 1200, incoming = 200, outgoing = 150),
                    Warehouse("SOUTH", "South Depot", "Sihanoukville", capacity = 3000, used = 900, incoming = 100, outgoing = 80),
                ),
            )
        }
    }
}
