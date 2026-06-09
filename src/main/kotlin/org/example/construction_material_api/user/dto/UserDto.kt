package org.example.construction_material_api.user.dto

import org.example.construction_material_api.user.model.*
import org.example.construction_material_api.user.repository.*

import com.fasterxml.jackson.annotation.JsonInclude
import org.example.construction_material_api.common.UserRole

/** Client-facing user shape: { id, name, email, role, avatarUrl? }. Id is a string. */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class UserDto(
    val id: String,
    val name: String,
    val email: String,
    val role: UserRole,
    val avatarUrl: String?,
)

fun User.toDto(): UserDto = UserDto(
    id = (id ?: 0).toString(),
    name = name,
    email = email,
    role = role,
    avatarUrl = avatarUrl,
)
