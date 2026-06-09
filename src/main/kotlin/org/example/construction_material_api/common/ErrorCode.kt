package org.example.construction_material_api.common

import org.springframework.http.HttpStatus

/**
 * Stable, machine readable error codes returned in the response envelope. The string
 * [code] is part of the API contract and must remain stable.
 */
enum class ErrorCode(val code: String, val status: HttpStatus) {
    VALIDATION_ERROR("VALIDATION_ERROR", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED("UNAUTHORIZED", HttpStatus.UNAUTHORIZED),
    FORBIDDEN("FORBIDDEN", HttpStatus.FORBIDDEN),
    NOT_FOUND("NOT_FOUND", HttpStatus.NOT_FOUND),
    CONFLICT("CONFLICT", HttpStatus.CONFLICT),
    INTERNAL_ERROR("INTERNAL_ERROR", HttpStatus.INTERNAL_SERVER_ERROR),
}
