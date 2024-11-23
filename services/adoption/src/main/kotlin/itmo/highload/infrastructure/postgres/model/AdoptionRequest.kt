package itmo.highload.infrastructure.postgres.model

import itmo.highload.api.dto.AdoptionStatus
import jakarta.persistence.*
import jakarta.validation.constraints.NotNull
import org.hibernate.annotations.JdbcType
import org.hibernate.dialect.PostgreSQLEnumJdbcType
import java.time.LocalDateTime

@Entity
@Table(name = "adoption_request")
data class AdoptionRequest(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,

    @Column(nullable = false)
    @NotNull(message = "Date and time is mandatory")
    val dateTime: LocalDateTime,

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType::class)
    @Column(nullable = false)
    var status: AdoptionStatus,

    @Column(nullable = false)
    val customerId: Int,

    @Column(nullable = true)
    var managerId: Int? = null,

    @Column(nullable = true)
    val animalId: Int
)
