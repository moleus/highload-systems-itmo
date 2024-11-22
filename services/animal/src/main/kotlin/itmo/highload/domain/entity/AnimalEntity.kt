package itmo.highload.domain.entity

import itmo.highload.api.dto.Gender
import itmo.highload.api.dto.HealthStatus

data class AnimalEntity(
    val id: Int = 0,
    var name: String,
    val typeOfAnimal: String,
    val gender: Gender,
    var isCastrated: Boolean,
    var healthStatus: HealthStatus
)
