package org.example.construction_material_api.common

/**
 * Holds the current request's trace id in a thread-local so the response envelope
 * builders can attach it without threading it through every method signature.
 */
object TraceIdContext {
    private val holder = ThreadLocal<String?>()

    fun set(traceId: String) = holder.set(traceId)

    fun current(): String? = holder.get()

    fun clear() = holder.remove()
}
