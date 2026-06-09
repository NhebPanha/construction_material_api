package org.example.construction_material_api.inventory.repository

import org.example.construction_material_api.inventory.model.*
import org.example.construction_material_api.inventory.dto.*
import org.example.construction_material_api.inventory.service.*

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface InventoryMovementRepository : JpaRepository<InventoryMovement, Long> {

    @Query(
        """
        SELECT m FROM InventoryMovement m
        WHERE (:productId IS NULL OR m.product.id = :productId)
        ORDER BY m.createdAt DESC
        """,
    )
    fun findFiltered(@Param("productId") productId: Long?, pageable: Pageable): Page<InventoryMovement>
}
