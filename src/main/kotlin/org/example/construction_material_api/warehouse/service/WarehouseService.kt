package org.example.construction_material_api.warehouse.service

import org.example.construction_material_api.warehouse.model.*
import org.example.construction_material_api.warehouse.repository.*
import org.example.construction_material_api.warehouse.dto.*

import org.example.construction_material_api.common.ConflictException
import org.example.construction_material_api.common.NotFoundException
import org.example.construction_material_api.common.PageResponse
import org.example.construction_material_api.common.Paging
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class WarehouseService(private val warehouseRepository: WarehouseRepository) {

    @Transactional(readOnly = true)
    fun list(q: String?, page: Int?, pageSize: Int?): PageResponse<WarehouseResponse> =
        PageResponse.from(
            warehouseRepository.search(q, Paging.of(page, pageSize, Sort.by(Sort.Direction.ASC, "code"))),
        ) { it.toResponse() }

    @Transactional(readOnly = true)
    fun get(id: Long): WarehouseResponse = findOrThrow(id).toResponse()

    @Transactional
    fun create(request: WarehouseRequest): WarehouseResponse {
        if (warehouseRepository.existsByCode(request.code)) {
            throw ConflictException("Warehouse with code '${request.code}' already exists")
        }
        val warehouse = Warehouse(
            code = request.code,
            name = request.name,
            location = request.location,
            capacity = request.capacity,
            used = request.used,
            incoming = request.incoming,
            outgoing = request.outgoing,
            isPrimary = request.isPrimary,
        )
        return warehouseRepository.save(warehouse).toResponse()
    }

    @Transactional
    fun update(id: Long, request: WarehouseRequest): WarehouseResponse {
        val warehouse = findOrThrow(id)
        if (request.code != warehouse.code && warehouseRepository.existsByCode(request.code)) {
            throw ConflictException("Warehouse with code '${request.code}' already exists")
        }
        warehouse.apply {
            code = request.code
            name = request.name
            location = request.location
            capacity = request.capacity
            used = request.used
            incoming = request.incoming
            outgoing = request.outgoing
            isPrimary = request.isPrimary
        }
        return warehouseRepository.save(warehouse).toResponse()
    }

    @Transactional
    fun delete(id: Long) = warehouseRepository.delete(findOrThrow(id))

    private fun findOrThrow(id: Long): Warehouse =
        warehouseRepository.findById(id).orElseThrow { NotFoundException("Warehouse $id not found") }
}
