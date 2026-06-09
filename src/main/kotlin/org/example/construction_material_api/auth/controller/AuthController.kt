package org.example.construction_material_api.auth.controller

import org.example.construction_material_api.auth.dto.*
import org.example.construction_material_api.auth.service.*

import jakarta.validation.Valid
import org.example.construction_material_api.common.ApiResponse
import org.example.construction_material_api.user.dto.UserDto
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1")
class AuthController(private val authService: AuthService) {

    @PostMapping("/auth/login")
    fun login(@Valid @RequestBody request: LoginRequest): ApiResponse<TokenResponse> =
        ApiResponse.ok(authService.login(request), "Login successful")

    @PostMapping("/auth/refresh")
    fun refresh(@Valid @RequestBody request: RefreshRequest): ApiResponse<TokenResponse> =
        ApiResponse.ok(authService.refresh(request), "Token refreshed")

    /**
     * Stateless JWTs have no server session to invalidate; the client discards its tokens.
     * The endpoint completes the contract and is a hook for a future token blocklist.
     */
    @PostMapping("/auth/logout")
    fun logout(): ApiResponse<Unit> = ApiResponse.ok("Logged out")

    @GetMapping("/users/me")
    fun me(authentication: Authentication): ApiResponse<UserDto> =
        ApiResponse.ok(authService.currentUser(authentication.name), "Current user")
}
