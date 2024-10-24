package itmo.highload.api.dto.response

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import itmo.highload.api.dto.Gender

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class CustomerResponse(
    val id: Int,
    val phone: String,
    val gender: Gender,
    val address: String
)
