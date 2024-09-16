package itmo.highload.controller.response

import itmo.highload.model.enum.UserRole
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

class JwtResponse(@NotBlank val accessToken: String,
                  @NotBlank val refreshToken: String?,
                  @NotNull val role: UserRole)
