package org.example.construction_material_api.common

import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.AuthenticationException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.HandlerMethodValidationException

/**
 * Translates exceptions into the standardized error envelope with stable error codes.
 */
@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(ApiException::class)
    fun handleApi(ex: ApiException): ResponseEntity<ApiResponse<Unit>> =
        build(ex.errorCode, ex.message)

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleBeanValidation(ex: MethodArgumentNotValidException): ResponseEntity<ApiResponse<Unit>> {
        val details = ex.bindingResult.fieldErrors.joinToString("; ") {
            "${it.field}: ${it.defaultMessage}"
        }
        return build(ErrorCode.VALIDATION_ERROR, details.ifBlank { "Validation failed" })
    }

    @ExceptionHandler(HandlerMethodValidationException::class)
    fun handleHandlerValidation(ex: HandlerMethodValidationException): ResponseEntity<ApiResponse<Unit>> =
        build(ErrorCode.VALIDATION_ERROR, "Validation failed")

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleUnreadable(ex: HttpMessageNotReadableException): ResponseEntity<ApiResponse<Unit>> =
        build(ErrorCode.VALIDATION_ERROR, "Malformed or missing request body")

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(ex: IllegalArgumentException): ResponseEntity<ApiResponse<Unit>> =
        build(ErrorCode.VALIDATION_ERROR, ex.message ?: "Invalid argument")

    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDenied(ex: AccessDeniedException): ResponseEntity<ApiResponse<Unit>> =
        build(ErrorCode.FORBIDDEN, "You do not have permission to access this resource")

    @ExceptionHandler(AuthenticationException::class)
    fun handleAuthentication(ex: AuthenticationException): ResponseEntity<ApiResponse<Unit>> =
        build(ErrorCode.UNAUTHORIZED, "Authentication is required")

    @ExceptionHandler(Exception::class)
    fun handleUnexpected(ex: Exception): ResponseEntity<ApiResponse<Unit>> =
        build(ErrorCode.INTERNAL_ERROR, ex.message ?: "Unexpected error")

    private fun build(code: ErrorCode, message: String): ResponseEntity<ApiResponse<Unit>> =
        ResponseEntity.status(code.status).body(ApiResponse.error(message, code.code))
}
