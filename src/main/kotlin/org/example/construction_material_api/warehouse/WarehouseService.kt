package org.example.construction_material_api.warehouse

import org.example.construction_material_api.common.DuplicateResourceException
import org.example.construction_material_api.common.NotFoundException
import org.example.construction_material_api.common.PageResponse
import org.example.construction_material_api.common.Paging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class WarehouseService(private val warehouseRepository: WarehouseRepository) {

    @Transactional(readOnly = true)
    fun list(search: String?, page: Int?, size: Int?, sort: String?): PageResponse<WarehouseResponse> =
        PageResponse.from(warehouseRepository.search(search, Paging.of(page, size, sort ?: "code,asc"))) { it.toResponse() }

    @Transactional(readOnly = true)
    fun get(id: Long): WarehouseResponse = findOrThrow(id).toResponse()

    @Transactional
    fun create(request: WarehouseRequest): WarehouseResponse {
        if (warehouseRepository.existsByCode(request.code)) {
            throw DuplicateResourceException("Warehouse with code '${request.code}' already exists")
        }
        val warehouse = Warehouse(
            code = request.code,
            name = request.name,
            location = request.location,
            active = request.active,
        )
        return warehouseRepository.save(warehouse).toResponse()
    }

    @Transactional
    fun update(id: Long, request: WarehouseRequest): WarehouseResponse {
        val warehouse = findOrThrow(id)
        if (request.code != warehouse.code && warehouseRepository.existsByCode(request.code)) {
            throw DuplicateResourceException("Warehouse with code '${request.code}' already exists")
        }
        warehouse.apply {
            code = request.code
            name = request.name
            location = request.location
            active = request.active
        }
        return warehouseRepository.save(warehouse).toResponse()
    }

    @Transactional
    fun delete(id: Long) {
        val warehouse = findOrThrow(id)
        warehouse.active = false
        warehouseRepository.save(warehouse)
    }

    private fun findOrThrow(id: Long): Warehouse =
        warehouseRepository.findById(id).orElseThrow { NotFoundException("Warehouse $id not found") }
}

fun Warehouse.toResponse(): WarehouseResponse = WarehouseResponse(
    id = id ?: 0,
    code = code,
    name = name,
    location = location,
    active = active,
)
