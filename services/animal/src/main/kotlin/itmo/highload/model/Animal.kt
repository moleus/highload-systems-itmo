package itmo.highload.model

import itmo.highload.api.dto.Gender
import itmo.highload.api.dto.HealthStatus
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table


@Table(name = "animal")
data class Animal(
    @Id
    val id: Int = 0,

    @Column("name")
    var name: String,

    @Column("type_of_animal")
    val typeOfAnimal: String,

    val gender: Gender,

    @Column("is_castrated")
    var isCastrated: Boolean,

    @Column("health_status")
    var healthStatus: HealthStatus
)
