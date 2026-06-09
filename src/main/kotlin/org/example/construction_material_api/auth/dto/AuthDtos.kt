package org.example.construction_material_api.auth.dto

import org.example.construction_material_api.auth.service.*

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import org.example.construction_material_api.user.dto.UserDto

data class LoginRequest(
    @field:NotBlank @field:Email val email: String,
    @field:NotBlank val password: String,
)

data class RefreshRequest(
    @field:NotBlank val refreshToken: String,
)

data class TokenResponse(
    val accessToken: String,
    val refreshToken: String,
    val user: UserDto,
)
