package itmo.highload.security.dto

import itmo.highload.security.Role
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

class JwtResponse(
    @NotBlank
    val accessToken: String,
    @NotNull
    val role: Role
)
