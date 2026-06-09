package org.example.construction_material_api.customer

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface CustomerRepository : JpaRepository<Customer, Long> {

    @Query(
        """
        SELECT c FROM Customer c
        WHERE (:search IS NULL OR :search = ''
            OR LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(c.phone) LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(c.email) LIKE LOWER(CONCAT('%', :search, '%')))
        """,
    )
    fun search(@Param("search") search: String?, pageable: Pageable): Page<Customer>
}
