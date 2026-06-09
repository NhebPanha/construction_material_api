package org.example.construction_material_api.security

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.jwt")
data class JwtProperties(
    val secret: String,
    val accessTokenTtlMinutes: Long = 30,
    val refreshTokenTtlDays: Long = 7,
)
