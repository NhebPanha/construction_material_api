package org.example.construction_material_api.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.example.construction_material_api.common.InvalidTokenException
import org.springframework.stereotype.Service
import java.nio.charset.StandardCharsets
import java.util.Date
import javax.crypto.SecretKey

enum class TokenType { ACCESS, REFRESH }

data class IssuedToken(val token: String, val expiresAt: Date)

/**
 * Issues and validates signed JWT access and refresh tokens.
 */
@Service
class JwtService(private val properties: JwtProperties) {

    private val key: SecretKey =
        Keys.hmacShaKeyFor(properties.secret.toByteArray(StandardCharsets.UTF_8))

    fun issueAccessToken(username: String, role: String, nowMillis: Long = System.currentTimeMillis()): IssuedToken =
        issue(username, role, TokenType.ACCESS, properties.accessTokenTtlMinutes * 60_000, nowMillis)

    fun issueRefreshToken(username: String, role: String, nowMillis: Long = System.currentTimeMillis()): IssuedToken =
        issue(username, role, TokenType.REFRESH, properties.refreshTokenTtlDays * 86_400_000, nowMillis)

    fun accessTokenTtlSeconds(): Long = properties.accessTokenTtlMinutes * 60

    private fun issue(
        username: String,
        role: String,
        type: TokenType,
        ttlMillis: Long,
        nowMillis: Long,
    ): IssuedToken {
        val now = Date(nowMillis)
        val expiry = Date(nowMillis + ttlMillis)
        val token = Jwts.builder()
            .subject(username)
            .claim("role", role)
            .claim("type", type.name)
            .issuedAt(now)
            .expiration(expiry)
            .signWith(key)
            .compact()
        return IssuedToken(token, expiry)
    }

    /** Parses and verifies the token signature/expiry, ensuring it is of [expectedType]. */
    fun parse(token: String, expectedType: TokenType): Claims {
        val claims = try {
            Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .payload
        } catch (ex: Exception) {
            throw InvalidTokenException()
        }
        if (claims["type"] != expectedType.name) {
            throw InvalidTokenException("Unexpected token type")
        }
        return claims
    }

    fun usernameFrom(claims: Claims): String = claims.subject

    fun roleFrom(claims: Claims): String = claims["role"]?.toString() ?: "CASHIER"
}
