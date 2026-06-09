package org.example.construction_material_api.security

import org.example.construction_material_api.user.UserRole
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableConfigurationProperties(JwtProperties::class)
class SecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter,
    private val authenticationEntryPoint: RestAuthenticationEntryPoint,
    private val accessDeniedHandler: RestAccessDeniedHandler,
) {

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun authenticationProvider(
        userDetailsService: AppUserDetailsService,
        passwordEncoder: PasswordEncoder,
    ): DaoAuthenticationProvider {
        val provider = DaoAuthenticationProvider(userDetailsService)
        provider.setPasswordEncoder(passwordEncoder)
        return provider
    }

    @Bean
    fun authenticationManager(provider: DaoAuthenticationProvider): AuthenticationManager =
        AuthenticationManager { authentication -> provider.authenticate(authentication) }

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .cors { }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .headers { headers -> headers.frameOptions { it.disable() } } // allow H2 console
            .authorizeHttpRequests { auth ->
                auth.requestMatchers(
                    "/api/v1/auth/login",
                    "/api/v1/auth/refresh",
                    "/api/v1/auth/logout",
                ).permitAll()
                auth.requestMatchers("/h2-console/**").permitAll()
                auth.requestMatchers("/actuator/health", "/error").permitAll()
                // Mutations on master data and inventory require elevated roles.
                auth.requestMatchers(HttpMethod.POST, "/api/v1/products/**", "/api/v1/suppliers/**", "/api/v1/warehouses/**")
                    .hasAnyRole(UserRole.ADMIN.name, UserRole.MANAGER.name)
                auth.requestMatchers(HttpMethod.PUT, "/api/v1/products/**", "/api/v1/suppliers/**", "/api/v1/warehouses/**")
                    .hasAnyRole(UserRole.ADMIN.name, UserRole.MANAGER.name)
                auth.requestMatchers(HttpMethod.DELETE, "/api/v1/**")
                    .hasAnyRole(UserRole.ADMIN.name, UserRole.MANAGER.name)
                auth.anyRequest().authenticated()
            }
            .exceptionHandling { ex ->
                ex.authenticationEntryPoint(authenticationEntryPoint)
                ex.accessDeniedHandler(accessDeniedHandler)
            }
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
        return http.build()
    }
}
