package itmo.highload.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import itmo.highload.security.Role
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class RegisterDto(

    @field:NotBlank(message = "must not be empty")
    @field:Size(min = 4, max = 50, message = "size must be between 4 and 50")
    @field:Pattern(regexp = "^[a-zA-Z0-9_]*$", message = "must match \"^[a-zA-Z0-9_]*$\"")
    val login: String,

    @field:NotBlank(message = "must not be empty")
    @field:Size(min = 3, max = 50, message = "size must be between 3 and 50")
    @field:Pattern(regexp = "^[a-zA-Z0-9_]*$", message = "must match \"^[a-zA-Z0-9_]*$\"")
    val password: String,

    @field:NotNull(message = "must not be null")
    val role: Role?
)
