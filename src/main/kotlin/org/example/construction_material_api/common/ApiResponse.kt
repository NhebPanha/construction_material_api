package org.example.construction_material_api.common

import com.fasterxml.jackson.annotation.JsonInclude

/**
 * Standardized response envelope used by every endpoint.
 *
 * `success` indicates whether the request succeeded, `message` is a human readable
 * description, `data` carries the payload (null on errors), `errorCode` is a stable
 * machine readable code (null on success) and `traceId` correlates the response with
 * server logs.
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T?,
    val errorCode: String?,
    val traceId: String?,
) {
    companion object {
        fun <T> ok(data: T, message: String = "OK"): ApiResponse<T> =
            ApiResponse(true, message, data, null, TraceIdContext.current())

        fun ok(message: String = "OK"): ApiResponse<Unit> =
            ApiResponse(true, message, Unit, null, TraceIdContext.current())

        fun <T> error(message: String, errorCode: String): ApiResponse<T> =
            ApiResponse(false, message, null, errorCode, TraceIdContext.current())
    }
}
