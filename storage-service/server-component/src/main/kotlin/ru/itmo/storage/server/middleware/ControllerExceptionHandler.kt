package ru.itmo.storage.server.middleware

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.validation.ConstraintViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import ru.itmo.storage.server.response.ErrorResponse
import ru.itmo.storage.storage.exception.KeyNotFoundException
import ru.itmo.storage.storage.exception.StorageComponentException

@RestControllerAdvice
class ControllerExceptionHandler {

    private val log = KotlinLogging.logger { }

    @ExceptionHandler(value = [KeyNotFoundException::class])
    fun handleKeyNotFoundException(exception: KeyNotFoundException): ResponseEntity<ErrorResponse> {
        return doHandleException(exception, HttpStatus.NOT_FOUND)
    }

    @ExceptionHandler(value = [ConstraintViolationException::class])
    fun handleConstraintViolationException(exception: ConstraintViolationException): ResponseEntity<ErrorResponse> {
        return doHandleException(exception, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(value = [StorageComponentException::class])
    fun handleStorageComponentException(exception: StorageComponentException): ResponseEntity<ErrorResponse> {
        return doHandleException(exception, HttpStatus.UNPROCESSABLE_ENTITY)
    }

    @ExceptionHandler(value = [UninitializedPropertyAccessException::class])
    fun handleUninitializedPropertyAccessException(
        exception: UninitializedPropertyAccessException
    ): ResponseEntity<ErrorResponse> {
        return doHandleException(exception, HttpStatus.SERVICE_UNAVAILABLE, "Service unavailable")
    }

    private fun doHandleException(
        exception: Exception,
        statusCode: HttpStatusCode,
        message: String? = exception.message
    ): ResponseEntity<ErrorResponse> {
        log.error(exception) {
            "Exception caught"
        }
        return ResponseEntity(ErrorResponse(message), statusCode)
    }
}
