package itmo.highload.model

import itmo.highload.api.dto.Gender
import itmo.highload.api.dto.HealthStatus
import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.hibernate.annotations.JdbcType
import org.hibernate.dialect.PostgreSQLEnumJdbcType

@Entity
@Table(name = "animal")
data class Animal(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,

    @Column(nullable = false, length = 50)
    @NotBlank(message = "Name is mandatory")
    @Size(max = 50, message = "Name cannot exceed 50 characters")
    var name: String,

    @Column(nullable = false, length = 50)
    @NotBlank(message = "Type of animal is mandatory")
    @Size(max = 50, message = "Type of animal cannot exceed 50 characters")
    val typeOfAnimal: String,

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType::class)
    @Column(nullable = false)
    val gender: Gender,

    @Column(nullable = false)
    var isCastrated: Boolean,

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType::class)
    @Column(nullable = false)
    var healthStatus: HealthStatus
)
