package org.example.construction_material_api.supplier.service

import org.example.construction_material_api.supplier.model.*
import org.example.construction_material_api.supplier.repository.*
import org.example.construction_material_api.supplier.dto.*

import org.example.construction_material_api.common.NotFoundException
import org.example.construction_material_api.common.PageResponse
import org.example.construction_material_api.common.Paging
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SupplierService(private val supplierRepository: SupplierRepository) {

    @Transactional(readOnly = true)
    fun list(q: String?, page: Int?, pageSize: Int?): PageResponse<SupplierResponse> =
        PageResponse.from(
            supplierRepository.search(q, Paging.of(page, pageSize, Sort.by(Sort.Direction.ASC, "name"))),
        ) { it.toResponse() }

    @Transactional(readOnly = true)
    fun get(id: Long): SupplierResponse = findOrThrow(id).toResponse()

    @Transactional
    fun create(request: SupplierRequest): SupplierResponse {
        val supplier = Supplier(
            name = request.name,
            phone = request.phone,
            email = request.email,
            address = request.address,
            payable = request.payable,
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
            payable = request.payable
        }
        return supplierRepository.save(supplier).toResponse()
    }

    @Transactional
    fun delete(id: Long) = supplierRepository.delete(findOrThrow(id))

    private fun findOrThrow(id: Long): Supplier =
        supplierRepository.findById(id).orElseThrow { NotFoundException("Supplier $id not found") }
}
