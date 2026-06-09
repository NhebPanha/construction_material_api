package org.example.construction_material_api.auth.service

import org.example.construction_material_api.auth.dto.*

import org.example.construction_material_api.common.UnauthorizedException
import org.example.construction_material_api.security.JwtService
import org.example.construction_material_api.security.TokenType
import org.example.construction_material_api.user.model.User
import org.example.construction_material_api.user.dto.UserDto
import org.example.construction_material_api.user.repository.UserRepository
import org.example.construction_material_api.user.dto.toDto
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.AuthenticationException
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val authenticationManager: AuthenticationManager,
    private val jwtService: JwtService,
    private val userRepository: UserRepository,
) {

    fun login(request: LoginRequest): TokenResponse {
        try {
            authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(request.email, request.password),
            )
        } catch (ex: AuthenticationException) {
            throw UnauthorizedException("Invalid email or password")
        }
        val user = userRepository.findByEmail(request.email)
            ?: throw UnauthorizedException("Invalid email or password")
        return issueTokens(user)
    }

    fun refresh(request: RefreshRequest): TokenResponse {
        val claims = jwtService.parse(request.refreshToken, TokenType.REFRESH)
        val email = jwtService.usernameFrom(claims)
        val user = userRepository.findByEmail(email)
            ?: throw UnauthorizedException("User no longer exists")
        if (!user.active) throw UnauthorizedException("User is disabled")
        return issueTokens(user)
    }

    fun currentUser(email: String): UserDto {
        val user = userRepository.findByEmail(email)
            ?: throw UnauthorizedException("User no longer exists")
        return user.toDto()
    }

    private fun issueTokens(user: User): TokenResponse {
        val access = jwtService.issueAccessToken(user.email, user.role.name)
        val refresh = jwtService.issueRefreshToken(user.email, user.role.name)
        return TokenResponse(
            accessToken = access.token,
            refreshToken = refresh.token,
            user = user.toDto(),
        )
    }
}
