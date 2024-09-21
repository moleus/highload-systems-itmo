package itmo.highload.model

import itmo.highload.model.enums.Gender
import itmo.highload.model.enums.HealthStatus
import jakarta.persistence.*
import jakarta.validation.constraints.*

@Entity
@Table(name = "animals")
data class Animal(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,

    @Column(nullable = false, length = 50)
    @NotBlank(message = "Name is mandatory")
    @Size(max = 50, message = "Name cannot exceed 50 characters")
    val name: String,

    @Column(name = "type_of_animal", nullable = false, length = 50)
    @NotBlank(message = "Type of animal is mandatory")
    @Size(max = 50, message = "Type of animal cannot exceed 50 characters")
    val typeOfAnimal: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val gender: Gender,

    @Column(name = "is_castrated", nullable = false)
    val isCastrated: Boolean,

    @Enumerated(EnumType.STRING)
    @Column(name = "health_status", nullable = false)
    val healthStatus: HealthStatus
)
