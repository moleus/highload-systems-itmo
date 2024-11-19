package itmo.highload.infrastructure.kafka.message

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class PurposeMessage(
    val id: Int,
    val name: String
)
