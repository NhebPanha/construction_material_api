package org.example.construction_material_api.supplier

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface SupplierRepository : JpaRepository<Supplier, Long> {

    @Query(
        """
        SELECT s FROM Supplier s
        WHERE (:search IS NULL OR :search = ''
            OR LOWER(s.name) LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(s.phone) LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(s.email) LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(s.contactPerson) LIKE LOWER(CONCAT('%', :search, '%')))
        """,
    )
    fun search(@Param("search") search: String?, pageable: Pageable): Page<Supplier>
}
