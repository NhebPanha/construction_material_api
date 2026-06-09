package org.example.construction_material_api.common

/**
 * Base exception carrying a stable [ErrorCode]. The [GlobalExceptionHandler] translates
 * these into the standardized error envelope.
 */
open class ApiException(
    val errorCode: ErrorCode,
    override val message: String,
) : RuntimeException(message)

class NotFoundException(message: String) : ApiException(ErrorCode.NOT_FOUND, message)

/** Used for all 409 conflicts: duplicates, insufficient stock, invalid state transitions. */
class ConflictException(message: String) : ApiException(ErrorCode.CONFLICT, message)

class InsufficientStockException(message: String) : ApiException(ErrorCode.CONFLICT, message)

class UnauthorizedException(message: String = "Invalid credentials or token") :
    ApiException(ErrorCode.UNAUTHORIZED, message)
