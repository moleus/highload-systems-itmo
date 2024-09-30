package itmo.highload.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import itmo.highload.model.enum.Role
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class RegisterDto(

    @field:NotBlank
    @field:Size(min = 4, max = 50)
    @field:Pattern(regexp = "^[a-zA-Z0-9_]*$")
    val login: String,

    @field:NotBlank
    @field:Size(min = 3, max = 50)
    @field:Pattern(regexp = "^[a-zA-Z0-9_]*$")
    val password: String,

    @field:NotNull
    val role: Role
)
