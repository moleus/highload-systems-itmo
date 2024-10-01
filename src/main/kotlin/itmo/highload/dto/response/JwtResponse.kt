package itmo.highload.dto.response

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import itmo.highload.model.enum.Role
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class JwtResponse(
    @NotBlank
    val accessToken: String,
    @NotBlank
    val refreshToken: String,
    @NotNull
    val role: Role
)
