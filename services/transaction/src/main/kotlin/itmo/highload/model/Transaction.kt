package itmo.highload.model

import jakarta.persistence.*
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import java.time.LocalDateTime

@Entity
@Table(name = "transaction")
data class Transaction(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,

    @Column(nullable = false)
    @NotNull(message = "Date and time is mandatory")
    val dateTime: LocalDateTime,

    @Column(nullable = false)
    val userId: Int,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "balance_id", nullable = false)
    val balance: Balance,

    @Column(nullable = false)
    @Positive(message = "Money amount must be positive")
    val moneyAmount: Int,

    @Column(nullable = false)
    val isDonation: Boolean
)
