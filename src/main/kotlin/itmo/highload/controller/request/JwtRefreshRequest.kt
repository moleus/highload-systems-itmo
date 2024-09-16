package itmo.highload.controller.request

import jakarta.validation.constraints.NotBlank

data class JwtRefreshRequest(@NotBlank val refreshToken: String)
