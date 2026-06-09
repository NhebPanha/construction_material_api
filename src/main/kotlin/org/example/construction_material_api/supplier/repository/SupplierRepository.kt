package org.example.construction_material_api.supplier.repository

import org.example.construction_material_api.supplier.model.*
import org.example.construction_material_api.supplier.dto.*
import org.example.construction_material_api.supplier.service.*

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface SupplierRepository : JpaRepository<Supplier, Long> {

    @Query(
        """
        SELECT s FROM Supplier s
        WHERE (:q IS NULL OR :q = ''
            OR LOWER(s.name) LIKE LOWER(CONCAT('%', :q, '%'))
            OR LOWER(s.phone) LIKE LOWER(CONCAT('%', :q, '%'))
            OR LOWER(s.email) LIKE LOWER(CONCAT('%', :q, '%')))
        """,
    )
    fun search(@Param("q") q: String?, pageable: Pageable): Page<Supplier>
}
