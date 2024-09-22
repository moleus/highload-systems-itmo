package itmo.highload.dto

import itmo.highload.model.enum.UserRole
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class RegisterDto(
    @NotBlank(message = "Login can't be blank")
    @Size(min = 4, max = 50)
    @Pattern(regexp = "^[a-zA-Z0-9_]*$", message = "Login can contain [a-zA-Z0-9_]")
    val login: String,

    @NotBlank(message = "Password can't be blank")
    @Size(min = 3, max = 50)
    @Pattern(regexp = "^[a-zA-Z0-9_]*$", message = "Password can contain [a-zA-Z0-9_]")
    val password: String,

    @NotNull(message = "Role can't be null")
    val role: UserRole
)
