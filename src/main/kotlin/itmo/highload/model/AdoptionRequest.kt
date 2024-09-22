package itmo.highload.model

import itmo.highload.model.enum.AdoptionStatus
import jakarta.persistence.Entity
import jakarta.persistence.Table
import jakarta.persistence.Id
import jakarta.persistence.GenerationType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Column
import jakarta.persistence.Enumerated
import jakarta.persistence.EnumType
import jakarta.persistence.ManyToOne
import jakarta.persistence.JoinColumn
import jakarta.persistence.FetchType
import jakarta.validation.constraints.NotNull
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
