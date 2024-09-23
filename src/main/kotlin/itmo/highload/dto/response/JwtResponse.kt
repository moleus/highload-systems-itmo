package itmo.highload.dto.response

import itmo.highload.model.enum.Role
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

class JwtResponse(
    @NotBlank
    val accessToken: String,
    @NotBlank
    val refreshToken: String,
    @NotNull
    val role: Role
)
