package com.potatonetwork.happymemories.common

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.multipart.MaxUploadSizeExceededException
import org.springframework.web.server.ResponseStatusException

/**
 * 전역 예외 핸들러.
 */
@RestControllerAdvice
class GlobalExceptionHandler {

    /**
     * ResponseStatusException을 잡아 status와 메시지를 그대로 반환합니다.
     */
    @ExceptionHandler(ResponseStatusException::class)
    fun handleResponseStatus(ex: ResponseStatusException): ResponseEntity<Map<String, String>> =
        ResponseEntity.status(ex.statusCode).body(mapOf("message" to (ex.reason ?: "오류가 발생했습니다.")))

    /**
     * 파일 업로드 크기 초과 시 413 응답을 반환합니다.
     */
    @ExceptionHandler(MaxUploadSizeExceededException::class)
    @ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
    fun handleMaxUploadSizeExceeded(): Map<String, String> =
        mapOf("message" to "파일 크기가 허용 용량을 초과했습니다. (파일당 최대 20MB, 요청당 최대 100MB)")
}
