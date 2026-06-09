package org.example.construction_material_api.user.model

import org.example.construction_material_api.user.repository.*
import org.example.construction_material_api.user.dto.*

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table
import org.example.construction_material_api.common.BaseEntity
import org.example.construction_material_api.common.UserRole

@Entity
@Table(name = "users")
class User(
    @Column(nullable = false)
    var name: String,

    @Column(nullable = false, unique = true)
    var email: String,

    @Column(nullable = false)
    var passwordHash: String,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var role: UserRole = UserRole.cashier,

    @Column
    var avatarUrl: String? = null,

    @Column(nullable = false)
    var active: Boolean = true,
) : BaseEntity()
