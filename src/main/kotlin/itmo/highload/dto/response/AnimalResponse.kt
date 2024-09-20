package itmo.highload.dto.response

class AnimalResponse(
    val id: Int,
    val name: String,
    val type: String,
    val gender: Gender,
    val isCastrated: Boolean,
    val healthStatus: HealthStatus
)
