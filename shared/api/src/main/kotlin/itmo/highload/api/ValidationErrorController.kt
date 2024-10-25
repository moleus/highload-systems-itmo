package itmo.highload.api

import jakarta.validation.ConstraintViolation
import jakarta.validation.ConstraintViolationException
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus

@ControllerAdvice
class ValidationErrorController {

    @ExceptionHandler(ConstraintViolationException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    fun onConstraintValidationException(e: ConstraintViolationException): Map<String, String> {
        return e.constraintViolations.associate { violation: ConstraintViolation<*> ->
            violation.propertyPath.toString() to violation.message
        }
    }

    @ExceptionHandler(DataIntegrityViolationException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    fun onDataIntegrityViolationException(e: DataIntegrityViolationException): String {
        if (e.cause is org.hibernate.exception.ConstraintViolationException) {

            val constraintEx = e.cause as org.hibernate.exception.ConstraintViolationException
            val constraintName = constraintEx.constraintName

            if (constraintName != null && constraintName.contains("fk")) {
                val id = constraintEx.message?.let { extractKeyIdFromErrorMessage(it) }
                return "Entity with id = $id doesn't exist"
            }
        }
        return "Data integrity exception occurred"
    }

    fun extractKeyIdFromErrorMessage(message: String): String? {
        val regex = Regex("""\((\d+)\)""")
        val matchResult = regex.find(message)
        return matchResult?.groups?.get(1)?.value
    }


    @ExceptionHandler(MethodArgumentNotValidException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    fun onMethodArgumentNotValidException(e: MethodArgumentNotValidException): Map<String, String?> {
        return e.bindingResult.fieldErrors.associate { fieldError: FieldError ->
            fieldError.field to fieldError.defaultMessage
        }
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    fun onHttpMessageNotReadableException(e: HttpMessageNotReadableException): String? {
        return e.message
    }
}
