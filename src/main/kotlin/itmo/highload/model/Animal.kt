package itmo.highload.model

import itmo.highload.model.enum.Gender
import itmo.highload.model.enum.HealthStatus
import jakarta.persistence.Entity
import jakarta.persistence.Table
import jakarta.persistence.Id
import jakarta.persistence.GenerationType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Column
import jakarta.persistence.Enumerated
import jakarta.persistence.EnumType
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

@Entity
@Table(name = "animals")
data class Animal(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,

    @Column(nullable = false, length = 50)
    @NotBlank(message = "Name is mandatory")
    @Size(max = 50, message = "Name cannot exceed 50 characters")
    var name: String,

    @Column(name = "type_of_animal", nullable = false, length = 50)
    @NotBlank(message = "Type of animal is mandatory")
    @Size(max = 50, message = "Type of animal cannot exceed 50 characters")
    val typeOfAnimal: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val gender: Gender,

    @Column(name = "is_castrated", nullable = false)
    var isCastrated: Boolean,

    @Enumerated(EnumType.STRING)
    @Column(name = "health_status", nullable = false)
    var healthStatus: HealthStatus
)
