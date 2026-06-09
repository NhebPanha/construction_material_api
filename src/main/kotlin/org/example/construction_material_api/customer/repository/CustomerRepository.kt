package org.example.construction_material_api.customer.repository

import org.example.construction_material_api.customer.model.*
import org.example.construction_material_api.customer.dto.*
import org.example.construction_material_api.customer.service.*

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface CustomerRepository : JpaRepository<Customer, Long> {

    @Query(
        """
        SELECT c FROM Customer c
        WHERE (:q IS NULL OR :q = ''
            OR LOWER(c.name) LIKE LOWER(CONCAT('%', :q, '%'))
            OR LOWER(c.phone) LIKE LOWER(CONCAT('%', :q, '%')))
        """,
    )
    fun search(@Param("q") q: String?, pageable: Pageable): Page<Customer>
}
