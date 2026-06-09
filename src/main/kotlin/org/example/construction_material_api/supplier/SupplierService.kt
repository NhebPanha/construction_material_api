package org.example.construction_material_api.supplier

import org.example.construction_material_api.common.NotFoundException
import org.example.construction_material_api.common.PageResponse
import org.example.construction_material_api.common.Paging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SupplierService(private val supplierRepository: SupplierRepository) {

    @Transactional(readOnly = true)
    fun list(search: String?, page: Int?, size: Int?, sort: String?): PageResponse<SupplierResponse> =
        PageResponse.from(supplierRepository.search(search, Paging.of(page, size, sort ?: "name,asc"))) { it.toResponse() }

    @Transactional(readOnly = true)
    fun get(id: Long): SupplierResponse = findOrThrow(id).toResponse()

    @Transactional
    fun create(request: SupplierRequest): SupplierResponse {
        val supplier = Supplier(
            name = request.name,
            phone = request.phone,
            email = request.email,
            address = request.address,
            contactPerson = request.contactPerson,
            active = request.active,
        )
        return supplierRepository.save(supplier).toResponse()
    }

    @Transactional
    fun update(id: Long, request: SupplierRequest): SupplierResponse {
        val supplier = findOrThrow(id).apply {
            name = request.name
            phone = request.phone
            email = request.email
            address = request.address
            contactPerson = request.contactPerson
            active = request.active
        }
        return supplierRepository.save(supplier).toResponse()
    }

    @Transactional
    fun delete(id: Long) {
        val supplier = findOrThrow(id)
        supplier.active = false
        supplierRepository.save(supplier)
    }

    private fun findOrThrow(id: Long): Supplier =
        supplierRepository.findById(id).orElseThrow { NotFoundException("Supplier $id not found") }
}

fun Supplier.toResponse(): SupplierResponse = SupplierResponse(
    id = id ?: 0,
    name = name,
    phone = phone,
    email = email,
    address = address,
    contactPerson = contactPerson,
    active = active,
)
