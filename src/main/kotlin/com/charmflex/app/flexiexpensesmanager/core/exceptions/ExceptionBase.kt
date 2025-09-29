package com.charmflex.app.flexiexpensesmanager.core.exceptions

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletResponse

abstract class ExceptionBase(
    val statusCode: Int,
    val errorCode: String,
    val errorMessage: String
) : Throwable()

object GenericException : ExceptionBase(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "GENERIC_ERROR", "generic error occurred.")