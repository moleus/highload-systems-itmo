package itmo.highload.api.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class PurposeRequestDto(
    @field:NotBlank(message = "must not be empty")
    @field:Size(min = 1, max = 50, message = "size must be between 1 and 50")
    val name: String
)
