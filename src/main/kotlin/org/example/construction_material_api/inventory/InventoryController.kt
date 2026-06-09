package org.example.construction_material_api.inventory

import jakarta.validation.Valid
import org.example.construction_material_api.common.ApiResponse
import org.example.construction_material_api.common.PageResponse
import org.example.construction_material_api.common.Paging
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/inventory")
class InventoryController(
    private val inventoryService: InventoryService,
    private val movementRepository: InventoryMovementRepository,
) {

    @PostMapping("/receive")
    fun receive(@Valid @RequestBody request: ReceiveStockRequest): ApiResponse<StockLevelResponse> {
        val stock = inventoryService.receive(
            request.productId, request.warehouseId, request.quantity, request.reference, request.note,
        )
        return ApiResponse.ok(stock.toResponse(), "Stock received")
    }

    @PostMapping("/adjust")
    fun adjust(@Valid @RequestBody request: AdjustStockRequest): ApiResponse<StockLevelResponse> {
        val stock = inventoryService.adjust(
            request.productId, request.warehouseId, request.delta, request.reference, request.note,
        )
        return ApiResponse.ok(stock.toResponse(), "Stock adjusted")
    }

    @GetMapping("/stock/{productId}")
    @Transactional(readOnly = true)
    fun stockForProduct(@PathVariable productId: Long): ApiResponse<List<StockLevelResponse>> =
        ApiResponse.ok(
            inventoryService.stockLevelsForProduct(productId).map { it.toResponse() },
            "Stock levels retrieved",
        )

    @GetMapping("/movements")
    @Transactional(readOnly = true)
    fun movements(
        @RequestParam(required = false) productId: Long?,
        @RequestParam(required = false) warehouseId: Long?,
        @RequestParam(required = false) page: Int?,
        @RequestParam(required = false) size: Int?,
    ): ApiResponse<PageResponse<MovementResponse>> {
        val result = movementRepository.findFiltered(productId, warehouseId, Paging.of(page, size, null))
        return ApiResponse.ok(PageResponse.from(result) { it.toResponse() }, "Movements retrieved")
    }
}
