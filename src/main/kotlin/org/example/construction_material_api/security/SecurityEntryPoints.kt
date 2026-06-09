package org.example.construction_material_api.security

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.example.construction_material_api.common.ErrorCode
import org.example.construction_material_api.common.TraceIdContext
import org.springframework.http.MediaType
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.stereotype.Component

/**
 * Emits the standardized error envelope for authentication and authorization failures
 * that occur before a controller is reached.
 */
@Component
class RestAuthenticationEntryPoint : AuthenticationEntryPoint {
    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException,
    ) = writeEnvelope(response, ErrorCode.UNAUTHORIZED, "Authentication is required")
}

@Component
class RestAccessDeniedHandler : AccessDeniedHandler {
    override fun handle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        accessDeniedException: AccessDeniedException,
    ) = writeEnvelope(response, ErrorCode.FORBIDDEN, "You do not have permission to access this resource")
}

private fun writeEnvelope(response: HttpServletResponse, code: ErrorCode, message: String) {
    response.status = code.status.value()
    response.contentType = MediaType.APPLICATION_JSON_VALUE
    response.characterEncoding = "UTF-8"
    val traceId = TraceIdContext.current()
    val body = """{"success":false,"message":${quote(message)},"data":null,""" +
        """"errorCode":${quote(code.code)},"traceId":${if (traceId == null) "null" else quote(traceId)}}"""
    response.writer.write(body)
}

private fun quote(value: String): String {
    val escaped = value.replace("\\", "\\\\").replace("\"", "\\\"")
    return "\"$escaped\""
}
