package org.example.construction_material_api.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

/**
 * Reads the bearer access token, validates it and populates the security context.
 * Invalid tokens are simply ignored here so the entry point can produce a consistent
 * 401 envelope for protected endpoints.
 */
@Component
class JwtAuthenticationFilter(
    private val jwtService: JwtService,
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val header = request.getHeader(HttpHeaders.AUTHORIZATION)
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            val token = header.substring(BEARER_PREFIX.length).trim()
            runCatching {
                val claims = jwtService.parse(token, TokenType.ACCESS)
                val username = jwtService.usernameFrom(claims)
                val role = jwtService.roleFrom(claims)
                if (SecurityContextHolder.getContext().authentication == null) {
                    val authority = SimpleGrantedAuthority("ROLE_$role")
                    val auth = UsernamePasswordAuthenticationToken(username, null, listOf(authority))
                    auth.details = WebAuthenticationDetailsSource().buildDetails(request)
                    SecurityContextHolder.getContext().authentication = auth
                }
            }
        }
        filterChain.doFilter(request, response)
    }

    companion object {
        private const val BEARER_PREFIX = "Bearer "
    }
}
