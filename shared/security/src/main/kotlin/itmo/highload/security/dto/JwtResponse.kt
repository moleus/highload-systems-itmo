package itmo.highload.security.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import itmo.highload.security.Role
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
class JwtResponse(
    @Suppress("unused")
    @NotBlank
    val accessToken: String,
    @NotNull
    val role: Role
)
