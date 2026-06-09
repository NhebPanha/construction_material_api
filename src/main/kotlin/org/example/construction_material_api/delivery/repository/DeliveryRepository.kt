package org.example.construction_material_api.delivery.repository

import org.example.construction_material_api.delivery.model.*
import org.example.construction_material_api.delivery.dto.*
import org.example.construction_material_api.delivery.service.*

import org.example.construction_material_api.common.DeliveryStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface DeliveryRepository : JpaRepository<Delivery, Long> {

    fun countByStatus(status: DeliveryStatus): Long

    @Query(
        """
        SELECT d FROM Delivery d
        WHERE (:status IS NULL OR d.status = :status)
        ORDER BY d.createdAt DESC
        """,
    )
    fun findFiltered(@Param("status") status: DeliveryStatus?, pageable: Pageable): Page<Delivery>
}
