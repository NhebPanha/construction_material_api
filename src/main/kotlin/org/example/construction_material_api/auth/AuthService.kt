package org.example.construction_material_api.auth

import org.example.construction_material_api.common.InvalidCredentialsException
import org.example.construction_material_api.common.InvalidTokenException
import org.example.construction_material_api.security.JwtService
import org.example.construction_material_api.security.TokenType
import org.example.construction_material_api.user.User
import org.example.construction_material_api.user.UserRepository
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
                UsernamePasswordAuthenticationToken(request.username, request.password),
            )
        } catch (ex: AuthenticationException) {
            throw InvalidCredentialsException()
        }
        val user = userRepository.findByUsername(request.username)
            ?: throw InvalidCredentialsException()
        return issueTokens(user)
    }

    fun refresh(request: RefreshRequest): TokenResponse {
        val claims = jwtService.parse(request.refreshToken, TokenType.REFRESH)
        val username = jwtService.usernameFrom(claims)
        val user = userRepository.findByUsername(username)
            ?: throw InvalidTokenException("User no longer exists")
        if (!user.active) throw InvalidTokenException("User is disabled")
        return issueTokens(user)
    }

    fun currentUser(username: String): UserResponse {
        val user = userRepository.findByUsername(username)
            ?: throw InvalidTokenException("User no longer exists")
        return user.toResponse()
    }

    private fun issueTokens(user: User): TokenResponse {
        val access = jwtService.issueAccessToken(user.username, user.role.name)
        val refresh = jwtService.issueRefreshToken(user.username, user.role.name)
        return TokenResponse(
            accessToken = access.token,
            refreshToken = refresh.token,
            expiresInSeconds = jwtService.accessTokenTtlSeconds(),
        )
    }
}

fun User.toResponse(): UserResponse = UserResponse(
    id = id ?: 0,
    username = username,
    fullName = fullName,
    role = role.name,
    active = active,
)
