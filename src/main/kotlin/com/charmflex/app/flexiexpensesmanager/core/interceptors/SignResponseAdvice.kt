package com.charmflex.app.flexiexpensesmanager.core.interceptors

import com.charmflex.app.flexiexpensesmanager.core.crypto.SignatureService
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.core.MethodParameter
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice


@RestControllerAdvice
class SignResponseAdvice<T>(
    private val signatureService: SignatureService,
    private val objectMapper: ObjectMapper
) : ResponseBodyAdvice<T> {
    override fun supports(returnType: MethodParameter, converterType: Class<out HttpMessageConverter<*>>): Boolean {
        val methodHasAnnotation = returnType.hasMethodAnnotation(SignedResponse::class.java)
        val controllerHasAnnotation = returnType.containingClass.isAnnotationPresent(SignedResponse::class.java)

        return methodHasAnnotation || controllerHasAnnotation
    }

    override fun beforeBodyWrite(
        body: T?,
        returnType: MethodParameter,
        selectedContentType: MediaType,
        selectedConverterType: Class<out HttpMessageConverter<*>>,
        request: ServerHttpRequest,
        response: ServerHttpResponse
    ): T? {
        val content = objectMapper.writeValueAsString(body)
        val payload = String(content.toByteArray(), Charsets.UTF_8)
        val signature = signatureService.sign(payload)
        response.headers.add("X-Signature", signature)

        return body
    }

}