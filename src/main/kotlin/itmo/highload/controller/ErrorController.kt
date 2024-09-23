package itmo.highload.controller

import itmo.highload.service.exception.NegativeBalanceException
import jakarta.persistence.EntityNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus

@ControllerAdvice
class ErrorController {
    @ExceptionHandler(NegativeBalanceException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    fun onNegativeBalanceException(e: NegativeBalanceException): String? {
        return e.message
    }

    @ExceptionHandler(EntityNotFoundException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    fun onEntityNotFoundException(e: EntityNotFoundException): String? {
        return e.message
    }
}
