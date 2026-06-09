package org.example.construction_material_api.common

import org.springframework.data.domain.Page

/**
 * Pagination wrapper returned inside [ApiResponse.data] for list endpoints.
 * `page` is 1-based to match the BuildPOS client contract.
 */
data class PageResponse<T>(
    val items: List<T>,
    val page: Int,
    val pageSize: Int,
    val total: Long,
    val totalPages: Int,
) {
    companion object {
        fun <S : Any, T : Any> from(page: Page<S>, mapper: (S) -> T): PageResponse<T> = PageResponse(
            items = page.content.map(mapper),
            page = page.number + 1,
            pageSize = page.size,
            total = page.totalElements,
            totalPages = page.totalPages,
        )
    }
}
