package org.example.construction_material_api.common

/** Parses a string path/body id into a Long, treating anything non-numeric as "not found". */
fun String.toLongId(): Long = toLongOrNull() ?: throw NotFoundException("Resource '$this' not found")
