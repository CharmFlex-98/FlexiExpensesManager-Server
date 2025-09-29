package com.charmflex.app.flexiexpensesmanager.core.exceptions

import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
internal class GlobalExceptionHandler {
    @ExceptionHandler(ExceptionBase::class)
    fun handleException(exceptionBase: ExceptionBase): ResponseEntity<Map<String, Any>> {
        val body = mapOf(
            "errorCode" to exceptionBase.errorCode,
            "errorMessage" to exceptionBase.errorMessage
        )
        return ResponseEntity
            .status(exceptionBase.statusCode)
            .body(body)
    }
}