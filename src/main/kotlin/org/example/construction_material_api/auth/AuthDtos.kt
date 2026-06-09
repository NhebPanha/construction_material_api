package org.example.construction_material_api.auth

import jakarta.validation.constraints.NotBlank

data class LoginRequest(
    @field:NotBlank val username: String,
    @field:NotBlank val password: String,
)

data class RefreshRequest(
    @field:NotBlank val refreshToken: String,
)

data class TokenResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer",
    val expiresInSeconds: Long,
)

data class UserResponse(
    val id: Long,
    val username: String,
    val fullName: String,
    val role: String,
    val active: Boolean,
)
