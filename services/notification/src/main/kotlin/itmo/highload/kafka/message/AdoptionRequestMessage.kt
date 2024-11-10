package itmo.highload.kafka.message

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.time.LocalDateTime

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class AdoptionRequestMessage(
    val id: Int,
    @field:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val dateTime: LocalDateTime,
    val status: AdoptionStatus,
    val customerId: Int,
    val managerId: Int?,
    val animalId: Int
)
