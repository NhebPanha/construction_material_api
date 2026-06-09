package org.example.construction_material_api.security

import org.example.construction_material_api.user.User
import org.example.construction_material_api.user.UserRepository
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.core.userdetails.User as SpringUser
import org.springframework.stereotype.Service

/**
 * Loads users for Spring Security by email (the login identifier).
 */
@Service
class AppUserDetailsService(private val userRepository: UserRepository) : UserDetailsService {

    override fun loadUserByUsername(username: String): UserDetails {
        val user = userRepository.findByEmail(username)
            ?: throw UsernameNotFoundException("User not found: $username")
        return toUserDetails(user)
    }

    private fun toUserDetails(user: User): UserDetails =
        SpringUser.builder()
            .username(user.email)
            .password(user.passwordHash)
            .authorities(SimpleGrantedAuthority("ROLE_${user.role.name}"))
            .disabled(!user.active)
            .build()
}
