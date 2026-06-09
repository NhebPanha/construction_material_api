package org.example.construction_material_api.delivery

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
          AND (:search IS NULL OR :search = ''
            OR LOWER(d.deliveryNumber) LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(d.salesOrder.orderNumber) LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(d.address) LIKE LOWER(CONCAT('%', :search, '%')))
        """,
    )
    fun search(
        @Param("status") status: DeliveryStatus?,
        @Param("search") search: String?,
        pageable: Pageable,
    ): Page<Delivery>
}
