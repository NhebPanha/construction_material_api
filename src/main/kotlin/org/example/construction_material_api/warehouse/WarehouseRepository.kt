package org.example.construction_material_api.warehouse

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
        WHERE (:search IS NULL OR :search = ''
            OR LOWER(w.name) LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(w.code) LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(w.location) LIKE LOWER(CONCAT('%', :search, '%')))
        """,
    )
    fun search(@Param("search") search: String?, pageable: Pageable): Page<Warehouse>
}
