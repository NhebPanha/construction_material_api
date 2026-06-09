package org.example.construction_material_api.common

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.UUID

/**
 * Assigns a trace id to every request (reusing an incoming `X-Trace-Id` header when
 * present), exposes it via [TraceIdContext] and echoes it back as a response header.
 */
@Component
@Order(1)
class TraceIdFilter : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val traceId = request.getHeader(HEADER)?.takeIf { it.isNotBlank() }
            ?: UUID.randomUUID().toString()
        TraceIdContext.set(traceId)
        response.setHeader(HEADER, traceId)
        try {
            filterChain.doFilter(request, response)
        } finally {
            TraceIdContext.clear()
        }
    }

    companion object {
        const val HEADER = "X-Trace-Id"
    }
}
