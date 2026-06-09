package org.example.construction_material_api.user.repository

import org.example.construction_material_api.user.model.*
import org.example.construction_material_api.user.dto.*

import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<User, Long> {
    fun findByEmail(email: String): User?
    fun existsByEmail(email: String): Boolean
}
