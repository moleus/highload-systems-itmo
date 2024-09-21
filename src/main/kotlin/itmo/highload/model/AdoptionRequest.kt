package itmo.highload.model

import itmo.highload.model.enums.AdoptionStatus
import jakarta.persistence.*
import jakarta.validation.constraints.*
import java.time.LocalDateTime

@Entity
@Table(name = "adoption_requests")
data class AdoptionRequest(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,

    @Column(name = "date_time", nullable = false)
    @NotNull(message = "Date and time is mandatory")
    val dateTime: LocalDateTime,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: AdoptionStatus,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    val customer: Customer,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id", nullable = true)
    val manager: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "animal_id", nullable = false)
    val animal: Animal
)
