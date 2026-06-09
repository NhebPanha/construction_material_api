package org.example.construction_material_api.warehouse.repository

import org.example.construction_material_api.warehouse.model.*
import org.example.construction_material_api.warehouse.dto.*
import org.example.construction_material_api.warehouse.service.*

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface WarehouseRepository : JpaRepository<Warehouse, Long> {

    fun existsByCode(code: String): Boolean

    fun findByCode(code: String): Warehouse?

    @Query(
        """
        SELECT w FROM Warehouse w
        WHERE (:q IS NULL OR :q = ''
            OR LOWER(w.name) LIKE LOWER(CONCAT('%', :q, '%'))
            OR LOWER(w.code) LIKE LOWER(CONCAT('%', :q, '%'))
            OR LOWER(w.location) LIKE LOWER(CONCAT('%', :q, '%')))
        """,
    )
    fun search(@Param("q") q: String?, pageable: Pageable): Page<Warehouse>
}
