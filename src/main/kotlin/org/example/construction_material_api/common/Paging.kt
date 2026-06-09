package org.example.construction_material_api.common

import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort

/**
 * Builds a Spring [Pageable] from the client's 1-based `page` and `pageSize` params,
 * clamping to sane bounds. Spring pages are 0-based, so we subtract one.
 */
object Paging {
    private const val MAX_SIZE = 200

    fun of(page: Int?, pageSize: Int?, sort: Sort = Sort.unsorted()): Pageable {
        val safePage = ((page ?: 1).coerceAtLeast(1)) - 1
        val safeSize = (pageSize ?: 20).coerceIn(1, MAX_SIZE)
        return PageRequest.of(safePage, safeSize, sort)
    }
}
